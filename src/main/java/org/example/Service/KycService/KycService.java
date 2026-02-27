package org.example.Service.KycService;

import org.example.Model.Kyc.Kyc;
import org.example.Model.Kyc.KycFile;
import org.example.Model.Kyc.KycStatus;
import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Repository.KycRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class KycService {
    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;
    private static final int MAX_FILES_PER_SUBMISSION = 10;
    private static final Set<String> ALLOWED_MIME = Set.of("application/pdf", "image/jpeg", "image/png");
    private static final Set<String> ALLOWED_EXT = Set.of("pdf", "jpg", "jpeg", "png");
    private static final Pattern CIN_PATTERN = Pattern.compile("^\\d{8}$");

    private final KycRepository kycRepository;

    public KycService() {
        this(new KycRepository());
    }

    public KycService(KycRepository kycRepository) {
        this.kycRepository = kycRepository;
    }

    public KycStateResult getClientKycState(User actor) {
        ensureClient(actor);
        Kyc kyc = kycRepository.createIfMissing(actor.getId());
        int filesCount = kycRepository.findFilesByKycId(kyc.getId()).size();
        String cin = normalizeCin(kyc.getCin());
        String adresse = normalizeAdresse(kyc.getAdresse());
        LocalDate dateNaissance = kyc.getDateNaissance();
        if (KycRepository.isTempCin(cin)) {
            cin = "";
            if ("Adresse non renseignee".equalsIgnoreCase(adresse)) {
                adresse = "";
            }
            if (LocalDate.of(1970, 1, 1).equals(dateNaissance)) {
                dateNaissance = null;
            }
        }
        return new KycStateResult(kyc.getStatut(), kyc.getCommentaireAdmin(), filesCount, cin, adresse, dateNaissance, kyc.getSignaturePath());
    }

    public List<KycFile> getClientKycFiles(User actor) {
        ensureClient(actor);
        Kyc kyc = kycRepository.createIfMissing(actor.getId());
        return kycRepository.findFilesByKycId(kyc.getId());
    }

    public KycStateResult updateClientBirthDate(User actor, LocalDate dateNaissance) {
        ensureClient(actor);
        if (dateNaissance == null) {
            throw new IllegalArgumentException("La date de naissance est obligatoire.");
        }
        if (dateNaissance.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La date de naissance ne peut pas etre dans le futur.");
        }

        Kyc current = kycRepository.createIfMissing(actor.getId());
        String cin = current.getCin() == null ? "" : current.getCin();
        String adresse = current.getAdresse() == null ? "" : current.getAdresse();

        KycStatus targetStatus = current.getStatut();
        String commentaire = current.getCommentaireAdmin();

        kycRepository.saveOrUpdateKyc(
                actor.getId(),
                cin,
                adresse,
                dateNaissance,
                targetStatus,
                commentaire,
                LocalDateTime.now()
        );
        return getClientKycState(actor);
    }

    public KycSubmitResult submitOrUpdateClientKyc(User actor,
                                                   String cinRaw,
                                                   String adresseRaw,
                                                   LocalDate dateNaissance,
                                                   List<UploadDoc> docs) {
        ensureClient(actor);
        if (docs == null || docs.isEmpty()) {
            return KycSubmitResult.failure("Ajoutez au moins un document.");
        }
        if (docs.size() > MAX_FILES_PER_SUBMISSION) {
            return KycSubmitResult.failure("Nombre maximum de fichiers depasse (10). ");
        }
        if (dateNaissance == null) {
            return KycSubmitResult.failure("La date de naissance est obligatoire.");
        }
        if (dateNaissance.isAfter(LocalDate.now())) {
            return KycSubmitResult.failure("La date de naissance ne peut pas etre dans le futur.");
        }

        String cin = normalizeCin(cinRaw);
        if (!CIN_PATTERN.matcher(cin).matches()) {
            return KycSubmitResult.failure("CIN invalide (exactement 8 chiffres, sans lettres).");
        }

        String adresse = normalizeAdresse(adresseRaw);
        if (adresse.length() < 5) {
            return KycSubmitResult.failure("Adresse invalide (minimum 5 caracteres).");
        }

        if (kycRepository.existsByCinExceptUser(cin, actor.getId())) {
            return KycSubmitResult.failure("Ce CIN est deja utilise.");
        }

        Set<String> dedupe = new HashSet<>();
        for (UploadDoc doc : docs) {
            if (doc == null || doc.getData() == null || doc.getData().length == 0) {
                return KycSubmitResult.failure("Un fichier est vide ou invalide.");
            }
            if (doc.getSize() <= 0 || doc.getSize() > MAX_FILE_SIZE) {
                return KycSubmitResult.failure("Taille fichier invalide. Max 5MB.");
            }
            if (!isAllowedType(doc)) {
                return KycSubmitResult.failure("Type de fichier non autorise. PDF/JPG/PNG uniquement.");
            }
            String key = safeName(doc.getFileName()).toLowerCase(Locale.ROOT);
            if (!dedupe.add(key)) {
                return KycSubmitResult.failure("Doublon detecte dans la selection: " + doc.getFileName());
            }
        }

        Kyc kyc = kycRepository.createIfMissing(actor.getId());
        for (UploadDoc doc : docs) {
            kycRepository.upsertKycFile(
                    kyc.getId(),
                    safeName(doc.getFileName()),
                    normalizeType(doc),
                    doc.getSize(),
                    doc.getData()
            );
        }

        kycRepository.saveOrUpdateKyc(actor.getId(), cin, adresse, dateNaissance, KycStatus.EN_ATTENTE, null, LocalDateTime.now());
        return KycSubmitResult.success("KYC soumis avec succes. En attente de validation admin.");
    }

    public List<KycAdminRow> listKycForAdmin(User actor) {
        ensureAdmin(actor);
        return kycRepository.findAllAdminRows();
    }

    public List<KycFile> listKycFilesForAdmin(User actor, int kycId) {
        ensureAdmin(actor);
        return kycRepository.findFilesByKycId(kycId);
    }

    public KycFileDownload downloadKycFile(User actor, int fileId) {
        if (actor == null) {
            throw new SecurityException("Utilisateur non connecte.");
        }

        KycFile file = kycRepository.findFileById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("Fichier introuvable."));

        if (actor.getRole() == UserRole.ADMIN) {
            return new KycFileDownload(file.getFileName(), file.getFileType(), file.getFileData());
        }

        Kyc userKyc = kycRepository.createIfMissing(actor.getId());
        if (file.getKycId() != userKyc.getId()) {
            throw new SecurityException("Acces fichier refuse.");
        }

        return new KycFileDownload(file.getFileName(), file.getFileType(), file.getFileData());
    }

    public void adminValidate(User actor, int kycId) {
        ensureAdmin(actor);
        kycRepository.updateKycStatusByAdmin(kycId, KycStatus.APPROUVE, null);
    }

    public void adminSetPending(User actor, int kycId) {
        ensureAdmin(actor);
        kycRepository.updateKycStatusByAdmin(kycId, KycStatus.EN_ATTENTE, null);
    }

    public void adminRefuse(User actor, int kycId, String commentaireObligatoire) {
        ensureAdmin(actor);
        String commentaire = commentaireObligatoire == null ? "" : commentaireObligatoire.trim();
        if (commentaire.length() < 10) {
            throw new IllegalArgumentException("Commentaire obligatoire (minimum 10 caracteres).");
        }
        kycRepository.updateKycStatusByAdmin(kycId, KycStatus.REFUSE, commentaire);
    }

    private boolean isAllowedType(UploadDoc doc) {
        String fileName = safeName(doc.getFileName()).toLowerCase(Locale.ROOT);
        int idx = fileName.lastIndexOf('.');
        String ext = idx > 0 ? fileName.substring(idx + 1) : "";
        if (!ALLOWED_EXT.contains(ext)) {
            return false;
        }

        String mime = doc.getMimeType() == null ? "" : doc.getMimeType().toLowerCase(Locale.ROOT);
        if (mime.isBlank()) {
            return true;
        }
        return ALLOWED_MIME.contains(mime);
    }

    private String normalizeType(UploadDoc doc) {
        String mime = doc.getMimeType() == null ? "" : doc.getMimeType().toLowerCase(Locale.ROOT);
        if (!mime.isBlank()) {
            return mime;
        }
        String fileName = safeName(doc.getFileName()).toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".pdf")) return "application/pdf";
        if (fileName.endsWith(".png")) return "image/png";
        return "image/jpeg";
    }

    private static String safeName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "document";
        }
        return fileName.replace("\\", "_").replace("/", "_").trim();
    }

    private static String normalizeCin(String cin) {
        return cin == null ? "" : cin.trim();
    }

    private static String normalizeAdresse(String adresse) {
        return adresse == null ? "" : adresse.trim();
    }

    private void ensureAdmin(User actor) {
        if (actor == null || actor.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Action reservee a un administrateur.");
        }
    }

    private void ensureClient(User actor) {
        if (actor == null || actor.getRole() != UserRole.CLIENT) {
            throw new SecurityException("Action reservee a un client.");
        }
    }

    public void deleteUser(User currentUser, int userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUser'");
    }
}
