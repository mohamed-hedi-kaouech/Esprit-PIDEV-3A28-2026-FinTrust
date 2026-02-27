package org.example.Service.WalletService;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsService {

    private static final String ACCOUNT_SID = "AC9715f25cc7308bc0cd35a7e0625ea14c";
    private static final String AUTH_TOKEN = "a7858ec707a3887e786af26a830c6f74";  // À remplacer

    // ✅ VOTRE VRAI NUMÉRO TWILIO (acheté)
    private static final String TWILIO_PHONE_NUMBER = "+18633342720";

    public boolean envoyerCodeSms(String numeroDestinataire, String code) {
        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

            Message message = Message.creator(
                    new PhoneNumber(numeroDestinataire),      // Destinataire (+216...)
                    new PhoneNumber(TWILIO_PHONE_NUMBER),     // ✅ +18633342720
                    "🔐 Votre code FinTrust est : " + code
            ).create();

            System.out.println("✅ SMS envoyé ! SID: " + message.getSid());
            System.out.println("📱 De: " + TWILIO_PHONE_NUMBER);
            System.out.println("📲 Vers: " + numeroDestinataire);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi SMS: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        SmsService service = new SmsService();

        // ÉTAPE 1 : Test avec numéro magique (gratuit)
        String testNumber = "+15005550006";
        boolean testResult = service.envoyerCodeSms(testNumber, "123456");
        System.out.println("Test magique: " + (testResult ? "✅" : "❌"));

        // ÉTAPE 2 : Si test réussi, test avec vrai numéro (décommentez)
        // String vraiNumero = "+21650123456";  // Votre numéro personnel
        // boolean vraiResult = service.envoyerCodeSms(vraiNumero, "123456");
        // System.out.println("Test réel: " + (vraiResult ? "✅" : "❌"));
    }
}