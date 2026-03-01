package org.example.Utils;

/**
 * Configuration for SMTP email sending.
 * Configure via environment variables.
 */
public class EmailConfig {
    // SMTP Server settings
    public static final String SMTP_HOST = System.getenv().getOrDefault("SMTP_HOST", "smtp.gmail.com");
    public static final String SMTP_PORT = System.getenv().getOrDefault("SMTP_PORT", "587");
    public static final String SMTP_USER = System.getenv().getOrDefault("SMTP_USER", "mohamedhedi322@gmail.com");
    public static final String SMTP_PASS = System.getenv().getOrDefault("SMTP_PASS", "xnir pkyn brkj yxcg");
    public static final String SMTP_FROM = System.getenv().getOrDefault("SMTP_FROM", "mohamedhedi322@gmail.com");
    
    // TLS/SSL settings
    public static final boolean USE_STARTTLS = Boolean.parseBoolean(System.getenv().getOrDefault("SMTP_USE_STARTTLS", "true"));
    public static final boolean USE_SSL = Boolean.parseBoolean(System.getenv().getOrDefault("SMTP_USE_SSL", "false"));
    
    // Default recipient when none configured
    public static final String DEFAULT_RECIPIENT = System.getenv().getOrDefault("ALERT_EMAIL_TO", "kaouechmohamedhedi29@gmail.com");
    
    // Test mode: when true, emails are logged but not actually sent
    public static final boolean TEST_MODE = Boolean.parseBoolean(System.getenv().getOrDefault("EMAIL_TEST_MODE", "false"));
}
