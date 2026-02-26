package org.example.Service.UserService;

import org.example.Model.Kyc.Kyc;
import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Model.User.UserStatus;
import org.example.Repository.KycRepository;
import org.example.Repository.PasswordResetRepository;
import org.example.Repository.UserRepository;
import org.example.Service.AuditService.AuditService;
import org.example.Service.EmailService;
import org.example.Service.NotificationService.NotificationService;
import org.example.Service.Security.BCryptPasswordHasher;
import org.example.Service.Security.PasswordHasher;
import org.example.Utils.MaConnexion;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class UserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-\\s]{8,20}$");

    private static final int OTP_EXPIRATION_SECONDS = 600;
    private static final int MAX_RESET_ATTEMPTS = 5;
    public static final String RESET_BY_EMAIL = "EMAIL";

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final KycRepository kycRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final PasswordHasher passwordHasher;
    private final EmailService emailService;

    public UserService() {
        this(new UserRepository(), new KycRepository(), new BCryptPasswordHasher());
    }

    public UserService(UserRepository userRepository, KycRepository kycRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordResetRepository = new PasswordResetRepository();
        this.kycRepository = kycRepository;
        this.notificationService = new NotificationService();
        this.auditService = new AuditService();
        this.passwordHasher = passwordHasher;
        this.emailService = new EmailService();
    }

    public PasswordResetResult requestPasswordResetCode(String emailRaw) {
        return requestPasswordResetCode(emailRaw, RESET_BY_EMAIL);
    }

    public PasswordResetResult requestPasswordResetCode(String emailRaw, String channelRaw) {
        String email = normalizeEmail(emailRaw);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return PasswordResetResult.failure("Email invalide.");
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        String channel = normalize(channelRaw).toUpperCase(Locale.ROOT);
        if (channel.isBlank()) {
            channel = RESET_BY_EMAIL;
        }
        if (!RESET_BY_EMAIL.equals(channel)) {
            return PasswordResetResult.failure("Methode d'envoi invalide.");
        }

        String requestId = UUID.randomUUID().toString();
        if (optionalUser.isEmpty()) {
            safeAuditOtpRequest(null, email, RESET_BY_EMAIL, requestId, true, "neutral_response");
            return PasswordResetResult.success("Si un compte existe, un code a ete envoye par email.");
        }

        User user = optionalUser.get();
        String code = emailService.generateVerificationCode();
        String codeHash = BCrypt.hashpw(code, BCrypt.gensalt(10));
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(OTP_EXPIRATION_SECONDS);

        try {
            passwordResetRepository.invalidateActiveByUserId(user.getId());
            passwordResetRepository.createResetCode(user.getId(), codeHash, expiresAt);
            emailService.sendPasswordResetCode(email, code);
            safeAuditOtpRequest(user.getId(), email, RESET_BY_EMAIL, requestId, true, "sent");
        } catch (Exception e) {
            safeAuditOtpRequest(user.getId(), email, RESET_BY_EMAIL, requestId, false, shortMsg(e.getMessage()));
        }
        return PasswordResetResult.success("Si un compte existe, un code a ete envoye par email.");
    }

    public PasswordResetResult resetPassword(String emailRaw, String codeRaw, String newPassword, String confirmPassword) {
        String email = normalizeEmail(emailRaw);
        String code = normalize(codeRaw);
        String pass = normalize(newPassword);
        String confirm = normalize(confirmPassword);

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return PasswordResetResult.failure("Email invalide.");
        }

        if (!PASSWORD_PATTERN.matcher(pass).matches()) {
            return PasswordResetResult.failure("Le mot de passe doit contenir au moins 8 caracteres, avec lettres et chiffres.");
        }
        if (!pass.equals(confirm)) {
            return PasswordResetResult.failure("La confirmation du mot de passe ne correspond pas.");
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return PasswordResetResult.failure("Utilisateur introuvable.");
        }

        User user = optionalUser.get();
        String requestId = UUID.randomUUID().toString();

        Optional<PasswordResetRepository.ResetCodeRow> rowOpt = passwordResetRepository.findLatestActiveByUserId(user.getId());
        if (rowOpt.isEmpty()) {
            safeAuditOtpValidation(user.getId(), email, RESET_BY_EMAIL, requestId, false, "missing_code", null);
            return PasswordResetResult.failure("Code invalide ou expire. Demandez un nouveau code.");
        }

        PasswordResetRepository.ResetCodeRow row = rowOpt.get();
        if (LocalDateTime.now().isAfter(row.expiresAt())) {
            passwordResetRepository.markUsed(row.id());
            safeAuditOtpValidation(user.getId(), email, RESET_BY_EMAIL, requestId, false, "expired_code", null);
            return PasswordResetResult.failure("Code invalide ou expire. Demandez un nouveau code.");
        }
        if (row.attempts() >= MAX_RESET_ATTEMPTS) {
            passwordResetRepository.markUsed(row.id());
            safeAuditOtpValidation(user.getId(), email, RESET_BY_EMAIL, requestId, false, "max_attempts", null);
            return PasswordResetResult.failure("Trop de tentatives. Demandez un nouveau code.");
        }

        boolean validCode;
        try {
            validCode = BCrypt.checkpw(code, row.codeHash());
        } catch (Exception e) {
            validCode = false;
        }
        if (!validCode) {
            passwordResetRepository.incrementAttempts(row.id());
            safeAuditOtpValidation(user.getId(), email, RESET_BY_EMAIL, requestId, false, "invalid_code", null);
            return PasswordResetResult.failure("Code invalide ou expire. Demandez un nouveau code.");
        }

        Connection cnx = MaConnexion.getInstance().getCnx();
        boolean initialAutoCommit = true;
        try {
            initialAutoCommit = cnx.getAutoCommit();
            cnx.setAutoCommit(false);

            String hash = passwordHasher.hash(pass);
            userRepository.updatePassword(user.getId(), hash);
            passwordResetRepository.markUsed(row.id());

            cnx.commit();
            safeAuditOtpValidation(user.getId(), email, RESET_BY_EMAIL, requestId, true, "ok", null);
            return PasswordResetResult.success("Mot de passe reinitialise avec succes.");
        } catch (Exception e) {
            try {
                cnx.rollback();
            } catch (Exception ignored) {
            }
            safeAuditOtpValidation(user.getId(), email, RESET_BY_EMAIL, requestId, false, "tx_error", null);
            return PasswordResetResult.failure("Erreur technique pendant la reinitialisation.");
        } finally {
            try {
                cnx.setAutoCommit(initialAutoCommit);
            } catch (Exception ignored) {
            }
        }
    }

    // Backward compatibility if called elsewhere.
    public PasswordResetResult resetPassword(String emailRaw, String newPassword, String confirmPassword) {
        return PasswordResetResult.failure("Code requis. Utilisez d'abord 'Envoyer le code'.");
    }

    public SignupResult signup(SignupRequest req) {
        if (req == null) return SignupResult.failure("Requete invalide.");

        String nom = normalize(req.getNom());
        String prenom = normalize(req.getPrenom());
        String email = normalizeEmail(req.getEmail());
        String numTel = normalize(req.getNumTel());
        String password = req.getPassword() == null ? "" : req.getPassword();
        String confirmPassword = req.getConfirmPassword() == null ? "" : req.getConfirmPassword();

        if (nom.length() < 2 || nom.length() > 120) return SignupResult.failure("Le nom doit contenir entre 2 et 120 caracteres.");
        if (prenom.length() < 2 || prenom.length() > 120) return SignupResult.failure("Le prenom doit contenir entre 2 et 120 caracteres.");
        if (!EMAIL_PATTERN.matcher(email).matches()) return SignupResult.failure("Veuillez saisir un email valide.");
        if (!PHONE_PATTERN.matcher(numTel).matches()) return SignupResult.failure("Veuillez saisir un numero de telephone valide.");
        if (!PASSWORD_PATTERN.matcher(password).matches()) return SignupResult.failure("Le mot de passe doit contenir au moins 8 caracteres, avec lettres et chiffres.");
        if (!password.equals(confirmPassword)) return SignupResult.failure("La confirmation du mot de passe ne correspond pas.");
        if (userRepository.existsByEmail(email)) return SignupResult.failure("Cet email est deja utilise.");

        User user = new User(
                nom, prenom, email, numTel,
                passwordHasher.hash(password),
                UserRole.CLIENT,
                UserStatus.EN_ATTENTE,
                LocalDateTime.now()
        );

        userRepository.save(user);

        try {
            String authCode = emailService.generateVerificationCode();
            emailService.sendAuthVerificationCode(user.getEmail(), authCode);

            // Mail de bienvenue non bloquant: on tente, mais l'inscription reste valide.
            try {
                emailService.sendWelcomeEmail(user.getEmail(), user.getNom());
            } catch (Exception ignored) {
            }

            return SignupResult.success(
                    "Inscription reussie. Un mail d'authentification a ete envoye a " + user.getEmail() + "."
            );
        } catch (Exception e) {
            e.printStackTrace();
            return SignupResult.success(
                    "Inscription reussie, mais le mail d'authentification n'a pas ete envoye. Cause: "
                            + shortMsg(e.getMessage())
            );
        }
    }

    public SignupResult createUserByAdmin(User actor,
                                          String nomRaw,
                                          String emailRaw,
                                          String passwordRaw,
                                          String confirmPasswordRaw,
                                          UserRole role,
                                          UserStatus status) {
        try {
            ensureAdmin(actor);
        } catch (Exception e) {
            return SignupResult.failure("Action reservee a un administrateur.");
        }

        String nom = normalize(nomRaw);
        String email = normalizeEmail(emailRaw);
        String password = passwordRaw == null ? "" : passwordRaw;
        String confirmPassword = confirmPasswordRaw == null ? "" : confirmPasswordRaw;

        if (nom.length() < 2 || nom.length() > 120) {
            return SignupResult.failure("Le nom doit contenir entre 2 et 120 caracteres.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return SignupResult.failure("Veuillez saisir un email valide.");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return SignupResult.failure("Le mot de passe doit contenir au moins 8 caracteres, avec lettres et chiffres.");
        }
        if (!password.equals(confirmPassword)) {
            return SignupResult.failure("La confirmation du mot de passe ne correspond pas.");
        }
        if (role == null) {
            return SignupResult.failure("Role obligatoire.");
        }
        if (status == null) {
            return SignupResult.failure("Statut obligatoire.");
        }
        if (userRepository.existsByEmail(email)) {
            return SignupResult.failure("Cet email est deja utilise.");
        }

        User user = new User(
                nom,
                "",
                email,
                "",
                passwordHasher.hash(password),
                role,
                status,
                LocalDateTime.now()
        );

        try {
            userRepository.save(user);
        } catch (Exception e) {
            return SignupResult.failure("Erreur creation utilisateur: " + e.getMessage());
        }

        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getNom());
            return SignupResult.success("Utilisateur cree avec succes. Email de bienvenue envoye a " + user.getEmail() + ".");
        } catch (Exception e) {
            return SignupResult.success("Utilisateur cree avec succes, mais email non envoye: " + shortMsg(e.getMessage()));
        }
    }

    public LoginResult login(String emailRaw, String rawPassword) {
        String emailInput = normalizeEmail(emailRaw);
        String email = emailInput;
        String password = normalize(rawPassword);

        // Alias pratique: "admin" => cherche d'abord admin@pidev.local puis admin
        if ("admin".equals(emailInput)) {
            Optional<User> defaultAdmin = userRepository.findByEmail("admin@pidev.local");
            if (defaultAdmin.isPresent()) {
                email = "admin@pidev.local";
            } else {
                email = "admin";
            }
        }

        if ("admin".equals(emailInput) && "admin".equals(password)) {
            // Compatibilite ancienne logique: creer un admin simple s'il n'existe aucun admin.
            userRepository.seedDefaultAdminIfMissing("Admin", "admin", passwordHasher.hash("admin"));
            Optional<User> adminOpt = userRepository.findByEmail("admin");
            if (adminOpt.isPresent()) {
                return LoginResult.success(adminOpt.get(), "Connexion admin reussie.");
            }
        }

        if (email.isBlank() || password.isBlank()) return LoginResult.failure("Email ou mot de passe invalide.");

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) return LoginResult.failure("Email ou mot de passe invalide.");

        User user = optionalUser.get();
        if (!passwordHasher.verify(password, user.getPasswordHash())) return LoginResult.failure("Email ou mot de passe invalide.");

        if (user.getStatus() == UserStatus.EN_ATTENTE) return LoginResult.failure("Votre compte est en attente de validation.");
        if (user.getStatus() == UserStatus.REFUSE) return LoginResult.failure("Votre compte a ete refuse.");

        if (user.getRole() == UserRole.CLIENT) {
            Kyc kyc = kycRepository.createIfMissing(user.getId());
            return LoginResult.success(user, "Connexion reussie.", kyc.getStatut(), kyc.getCommentaireAdmin());
        }

        return LoginResult.success(user, "Connexion reussie.");
    }

    public List<User> listUsersForAdmin(User actor) {
        ensureAdmin(actor);
        return userRepository.findAll();
    }

    public void updateUserStatus(User actor, int userId, UserStatus status) {
        ensureAdmin(actor);
        userRepository.updateStatus(userId, status);
    }

    public void updateUserByAdmin(User actor, int userId, String nomRaw, String emailRaw, String numTelRaw, UserStatus status) {
        ensureAdmin(actor);

        String nom = normalize(nomRaw);
        String email = normalizeEmail(emailRaw);
        String numTel = normalize(numTelRaw);

        if (nom.length() < 2 || nom.length() > 120) throw new IllegalArgumentException("Nom invalide.");
        if (!EMAIL_PATTERN.matcher(email).matches()) throw new IllegalArgumentException("Email invalide.");
        if (!numTel.isBlank() && !PHONE_PATTERN.matcher(numTel).matches()) throw new IllegalArgumentException("Telephone invalide.");
        if (status == null) throw new IllegalArgumentException("Statut obligatoire.");

        if (userRepository.existsByEmailExceptUserId(email, userId)) {
            throw new IllegalArgumentException("Cet email est deja utilise par un autre utilisateur.");
        }

        userRepository.updateByAdmin(userId, nom, email, numTel, status);
    }

    public void deleteUser(User actor, int userId) {
        ensureAdmin(actor);

        if (actor.getId() == userId) {
            throw new IllegalArgumentException("Vous ne pouvez pas supprimer votre propre compte.");
        }

        userRepository.deleteById(userId);
    }

    public void sendNotificationToUser(User actor, int userId, String message) {
        ensureAdmin(actor);
        String msg = normalize(message);
        if (msg.isBlank()) {
            throw new IllegalArgumentException("Message notification vide.");
        }
        notificationService.create(userId, "ADMIN", msg);
    }

    public void updateUserProfile(User user) {
        if (user == null) throw new IllegalArgumentException("Utilisateur invalide.");

        String nom = normalize(user.getNom());
        String email = normalizeEmail(user.getEmail());
        String numTel = normalize(user.getNumTel());

        if (nom.length() < 2 || nom.length() > 120) throw new IllegalArgumentException("Nom invalide.");
        if (!EMAIL_PATTERN.matcher(email).matches()) throw new IllegalArgumentException("Email invalide.");
        if (!numTel.isBlank() && !PHONE_PATTERN.matcher(numTel).matches()) throw new IllegalArgumentException("Telephone invalide.");

        if (userRepository.existsByEmailExceptUserId(email, user.getId())) {
            throw new IllegalArgumentException("Cet email est deja utilise.");
        }

        userRepository.updateProfile(user.getId(), nom, email, numTel);
    }

    private void ensureAdmin(User actor) {
        if (actor == null || actor.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Action reservee a un administrateur.");
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeEmail(String value) {
        return normalize(value).toLowerCase();
    }

    private static String shortMsg(String msg) {
        String value = normalize(msg);
        if (value.isBlank()) return "raison inconnue";
        return value.length() > 160 ? value.substring(0, 160) + "..." : value;
    }

    private void safeAuditOtpRequest(Integer userId, String email, String channel, String requestId, boolean success, String reason) {
        try {
            auditService.logOtpRequest(userId, email, channel, requestId, success, reason);
        } catch (Exception ignored) {
        }
    }

    private void safeAuditOtpValidation(Integer userId, String email, String channel, String requestId, boolean success, String reason, Integer validationSeconds) {
        try {
            auditService.logOtpValidation(userId, email, channel, requestId, success, reason, validationSeconds);
        } catch (Exception ignored) {
        }
    }

}
