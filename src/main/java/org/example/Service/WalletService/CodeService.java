package org.example.Service.WalletService;

import java.util.Random;

public class CodeService {

    private EmailService emailService = new EmailService();

    public String genererCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public void envoyerCode(String telephone, String email, String code) {
        System.out.println("\n📱 === CODE D'ACCÈS CLIENT ===");
        if (telephone != null && !telephone.isEmpty()) {
            System.out.println("📞 Téléphone: " + telephone);
            // Pour SMS, il faudrait Twilio ou autre API
        }
        if (email != null && !email.isEmpty()) {
            System.out.println("📧 Email: " + email);
            // Envoyer l'email
            emailService.envoyerCodeParEmail(email, code);
        }
        System.out.println("🔐 Code: " + code);
        System.out.println("================================\n");
    }
}