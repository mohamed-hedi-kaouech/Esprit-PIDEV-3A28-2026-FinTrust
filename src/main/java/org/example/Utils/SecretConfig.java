package org.example.Utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Properties;

public final class SecretConfig {

    private static final Properties FILE_PROPS = new Properties();
    private static volatile boolean loaded = false;

    private SecretConfig() {
    }

    public static String get(String key) {
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) return env;

        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys;

        loadOnce();
        String value = FILE_PROPS.getProperty(key);
        return value == null ? "" : value.trim();
    }

    public static Optional<String> missingKeys(String... keys) {
        for (String key : keys) {
            if (get(key).isBlank()) return Optional.of(key);
        }
        return Optional.empty();
    }

    private static synchronized void loadOnce() {
        if (loaded) return;
        loaded = true;

        // 1) classpath resource: src/main/resources/config.properties
        try (InputStream is = SecretConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                FILE_PROPS.load(is);
            }
        } catch (Exception ignored) {
        }

        // 2) user-level secure file: %USERPROFILE%/.fintrust/secrets.properties
        Path userSecrets = Paths.get(System.getProperty("user.home"), ".fintrust", "secrets.properties");
        if (Files.isRegularFile(userSecrets)) {
            loadFile(userSecrets);
        }

        // 3) project local file (optional): ./fintrust.local.properties
        Path projectLocal = Paths.get("fintrust.local.properties").toAbsolutePath();
        if (Files.isRegularFile(projectLocal)) {
            loadFile(projectLocal);
        }
    }

    private static void loadFile(Path path) {
        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            Properties temp = new Properties();
            temp.load(fis);
            mergeNonBlank(temp);
        } catch (Exception ignored) {
        }
    }

    private static void mergeNonBlank(Properties source) {
        Enumeration<?> keys = source.propertyNames();
        while (keys.hasMoreElements()) {
            String key = String.valueOf(keys.nextElement());
            String value = source.getProperty(key);
            if (value == null) continue;
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                FILE_PROPS.setProperty(key, trimmed);
            }
        }
    }
}
