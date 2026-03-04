package UserTests;

import java.time.LocalDateTime;
import java.util.Optional;

import org.example.Model.Kyc.Kyc;
import org.example.Model.Kyc.KycStatus;
import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Model.User.UserStatus;
import org.example.Repository.KycRepository;
import org.example.Service.KycService.KycService;
import org.example.Service.KycService.KycStateResult;
import org.example.Service.UserService.UserService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class UserFullJourneyTest {
    @Test
    void testUserFullKycJourney() {
        User user = new User(
                "TestNom",
                "TestPrenom",
                "test@example.com",
                "0600000000",
                "hash",
                UserRole.CLIENT,
                UserStatus.EN_ATTENTE,
                LocalDateTime.now()
        );
        user.setId(1);

        User admin = new User(
                "Admin",
                "admin@example.com",
                "adminhash",
                UserRole.ADMIN,
                UserStatus.ACCEPTE,
                LocalDateTime.now()
        );
        admin.setId(2);

        KycRepository kycRepository = Mockito.mock(KycRepository.class);
        KycService kycService = new KycService(kycRepository);

        // adminValidate appelle findById plusieurs fois.
        Mockito.when(kycRepository.findById(10)).thenReturn(Optional.empty());
        Mockito.doNothing().when(kycRepository)
                .updateKycStatusByAdmin(Mockito.eq(10), Mockito.eq(KycStatus.APPROUVE), Mockito.isNull());

        kycService.adminValidate(admin, 10);

        Mockito.verify(kycRepository, Mockito.times(1))
                .updateKycStatusByAdmin(10, KycStatus.APPROUVE, null);

        // V�rification de la partie "statut KYC client".
        Kyc kyc = new Kyc();
        kyc.setId(10);
        kyc.setUserId(user.getId());
        kyc.setStatut(KycStatus.APPROUVE);
        kyc.setCin("12345678");
        kyc.setAdresse("Adresse test");
        kyc.setSignaturePath("storage/kyc/signatures/signature_user_1.png");

        Mockito.when(kycRepository.createIfMissing(user.getId())).thenReturn(kyc);
        Mockito.when(kycRepository.findFilesByKycId(10)).thenReturn(java.util.Collections.emptyList());

        KycStateResult result = kycService.getClientKycState(user);

        Assertions.assertEquals(KycStatus.APPROUVE, result.getStatus());
        Assertions.assertTrue(result.isApproved());
        Assertions.assertEquals("12345678", result.getCin());
    }
}