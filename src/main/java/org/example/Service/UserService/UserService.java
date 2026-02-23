package org.example.Service.UserService;

import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Model.User.UserStatus;
import org.example.Model.Kyc.Kyc;
import org.example.Repository.KycRepository;
import org.example.Repository.UserRepository;
import org.example.Service.Security.BCryptPasswordHasher;
import org.example.Service.Security.PasswordHasher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserService {
        public PasswordResetResult resetPassword(String emailRaw, String newPassword, String confirmPassword) {
            String email = normalizeEmail(emailRaw);
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return PasswordResetResult.failure("Email invalide.");
            }
            if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
                return PasswordResetResult.failure("Le mot de passe doit contenir au moins 8 caracteres, avec lettres et chiffres.");
            }
            if (!newPassword.equals(confirmPassword)) {
                return PasswordResetResult.failure("La confirmation du mot de passe ne correspond pas.");
            }
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isEmpty()) {
                return PasswordResetResult.failure("Utilisateur introuvable.");
            }
            User user = optionalUser.get();
            user.setPasswordHash(passwordHasher.hash(newPassword));
            userRepository.save(user);
            return PasswordResetResult.success("Mot de passe réinitialisé avec succès.");
        }
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-\\s]{8,20}$");

    private final UserRepository userRepository;
    private final KycRepository kycRepository;
    private final PasswordHasher passwordHasher;

    public UserService() {
        this(new UserRepository(), new KycRepository(), new BCryptPasswordHasher());
    }

    public UserService(UserRepository userRepository, KycRepository kycRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.kycRepository = kycRepository;
        this.passwordHasher = passwordHasher;
    }

    public SignupResult signup(SignupRequest req) {
        if (req == null) {
            return SignupResult.failure("Requete invalide.");
        }

        String nom = normalize(req.getNom());
        String prenom = normalize(req.getPrenom());
        String email = normalizeEmail(req.getEmail());
        String numTel = normalize(req.getNumTel());
        String password = req.getPassword() == null ? "" : req.getPassword();
        String confirmPassword = req.getConfirmPassword() == null ? "" : req.getConfirmPassword();

        if (nom.length() < 2 || nom.length() > 120) {
            return SignupResult.failure("Le nom doit contenir entre 2 et 120 caracteres.");
        }
        if (prenom.length() < 2 || prenom.length() > 120) {
            return SignupResult.failure("Le prenom doit contenir entre 2 et 120 caracteres.");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return SignupResult.failure("Veuillez saisir un email valide.");
        }
        if (!PHONE_PATTERN.matcher(numTel).matches()) {
            return SignupResult.failure("Veuillez saisir un numero de telephone valide.");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return SignupResult.failure("Le mot de passe doit contenir au moins 8 caracteres, avec lettres et chiffres.");
        }

        if (!password.equals(confirmPassword)) {
            return SignupResult.failure("La confirmation du mot de passe ne correspond pas.");
        }

        if (userRepository.existsByEmail(email)) {
            return SignupResult.failure("Cet email est deja utilise.");
        }

        User user = new User(
                nom,
                prenom,
                email,
                numTel,
                passwordHasher.hash(password),
                UserRole.CLIENT,
                UserStatus.EN_ATTENTE,
                LocalDateTime.now()
        );

        userRepository.save(user);
        return SignupResult.success("Inscription reussie. Votre compte est en attente de validation.");
    }

    public LoginResult login(String emailRaw, String rawPassword) {
        String email = normalizeEmail(emailRaw);
        String password = rawPassword == null ? "" : rawPassword;

        // Cas admin direct
        if ("admin".equalsIgnoreCase(email) && "admin".equals(password)) {
            // Seed admin si absent
            userRepository.seedDefaultAdminIfMissing("Admin", "admin", passwordHasher.hash("admin"));
            Optional<User> adminOpt = userRepository.findByEmail("admin");
            if (adminOpt.isPresent()) {
                User admin = adminOpt.get();
                return LoginResult.success(admin, "Connexion admin reussie.");
            } else {
                return LoginResult.failure("Impossible de créer l'admin.");
            }
        }

        if (email.isBlank() || password.isBlank()) {
            return LoginResult.failure("Email ou mot de passe invalide.");
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return LoginResult.failure("Email ou mot de passe invalide.");
        }

        User user = optionalUser.get();
        if (!passwordHasher.verify(password, user.getPasswordHash())) {
            return LoginResult.failure("Email ou mot de passe invalide.");
        }

        if (user.getStatus() == UserStatus.EN_ATTENTE) {
            return LoginResult.failure("Votre compte est en attente de validation.");
        }

        if (user.getStatus() == UserStatus.REFUSE) {
            return LoginResult.failure("Votre compte a ete refuse.");
        }

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

    public SignupResult createUserByAdmin(User actor,
                                          String nomRaw,
                                          String emailRaw,
                                          String passwordRaw,
                                          String confirmPasswordRaw,
                                          UserRole role,
                                          UserStatus status) {
        ensureAdmin(actor);

        String nom = normalize(nomRaw);
        String email = normalizeEmail(emailRaw);
        String password = passwordRaw == null ? "" : passwordRaw;
        String confirmPassword = confirmPasswordRaw == null ? "" : confirmPasswordRaw;

        if (role == null) {
            return SignupResult.failure("Le role est obligatoire.");
        }
        if (status == null) {
            return SignupResult.failure("Le statut est obligatoire.");
        }
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
        if (userRepository.existsByEmail(email)) {
            return SignupResult.failure("Cet email est deja utilise.");
        }

        User user = new User(
                nom,
                email,
                passwordHasher.hash(password),
                role,
                status,
                LocalDateTime.now()
        );
        userRepository.save(user);
        return SignupResult.success("Utilisateur cree avec succes.");
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
}
