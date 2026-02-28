package org.example.Utils;

/**
 * Configuration for SMTP email sending.
 * Configure via environment variables.
 */
public class EmailConfig {
    // SMTP Server settings
    public static final String SMTP_HOST = System.getenv().getOrDefault("SMTP_HOST", "localhost");
    public static final String SMTP_PORT = System.getenv().getOrDefault("SMTP_PORT", "25");
    public static final String SMTP_USER = System.getenv().getOrDefault("SMTP_USER", "");
    public static final String SMTP_PASS = System.getenv().getOrDefault("SMTP_PASS", "");
    public static final String SMTP_FROM = System.getenv().getOrDefault("SMTP_FROM", "alerts@pidev.local");
    
    // TLS/SSL settings
    public static final boolean USE_STARTTLS = Boolean.parseBoolean(System.getenv().getOrDefault("SMTP_USE_STARTTLS", "false"));
    public static final boolean USE_SSL = Boolean.parseBoolean(System.getenv().getOrDefault("SMTP_USE_SSL", "false"));
    
    // Default recipient when none configured
    public static final String DEFAULT_RECIPIENT = System.getenv().getOrDefault("ALERT_EMAIL_TO", "admin@pidev.local");
    
    // Test mode: when true, emails are logged but not actually sent
    public static final boolean TEST_MODE = Boolean.parseBoolean(System.getenv().getOrDefault("EMAIL_TEST_MODE", "true"));
}
