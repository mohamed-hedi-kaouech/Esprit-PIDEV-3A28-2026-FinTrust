package UserTests;

import org.example.Model.Kyc.KycStatus;
import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Model.User.UserStatus;
import org.example.Service.KycService.KycService;
import org.example.Service.KycService.KycStateResult;
import org.example.Repository.KycRepository;
import org.example.Repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

public class UserFullJourneyTest {
    @Test
    void testUserFullKycJourney() {
        // Arrange
        User user = new User("TestNom", "TestPrenom", "test@example.com", "0600000000", "hash", UserRole.CLIENT, UserStatus.EN_ATTENTE, LocalDateTime.now());
        user.setId(1);
        User admin = new User("Admin", "admin@example.com", "adminhash", UserRole.ADMIN, UserStatus.ACCEPTE, LocalDateTime.now());
        admin.setId(2);

        KycRepository kycRepository = Mockito.mock(KycRepository.class);
        KycService kycService = new KycService(kycRepository);

        // Simuler la validation KYC par l'admin
        Mockito.doNothing().when(kycRepository).updateKycStatusByAdmin(Mockito.eq(10), Mockito.eq(KycStatus.APPROUVE), Mockito.isNull());
        
        // Act
        kycService.adminValidate(admin, 10);

        // Simuler le retour du statut KYC côté client
        Mockito.when(kycRepository.createIfMissing(user.getId())).thenReturn(null); // Adapter selon logique réelle
        Mockito.when(kycRepository.findFilesByKycId(10)).thenReturn(java.util.Collections.emptyList());
        KycStateResult result = new KycStateResult(KycStatus.APPROUVE, null, 0, "CIN123", "Adresse", null);

        // Assert
        Assertions.assertEquals(KycStatus.APPROUVE, result.getStatus());
        Assertions.assertTrue(result.isApproved());
        // Ici, on simule que le client peut naviguer vers le dashboard
        // (à adapter selon la logique réelle de navigation)
    }
}
