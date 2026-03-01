package org.example.Utils;

import org.example.Model.Budget.Alerte;

public class EmailTest {
    public static void main(String[] args) {
        System.out.println("--- EmailConfig ---");
        System.out.println("SMTP_HOST=" + EmailConfig.SMTP_HOST);
        System.out.println("SMTP_PORT=" + EmailConfig.SMTP_PORT);
        System.out.println("SMTP_USER=" + (EmailConfig.SMTP_USER.isEmpty() ? "(empty)" : EmailConfig.SMTP_USER));
        System.out.println("SMTP_FROM=" + EmailConfig.SMTP_FROM);
        System.out.println("DEFAULT_RECIPIENT=" + EmailConfig.DEFAULT_RECIPIENT);
        System.out.println("TEST_MODE=" + EmailConfig.TEST_MODE);
        System.out.println("--------------------\n");

        Alerte a = new Alerte(1, "Test d'alerte depuis EmailTest", 1.0);
        boolean ok = EmailSender.sendAlerteEmail(a, "TestCategory");
        System.out.println("sendAlerteEmail returned: " + ok);
    }
}
