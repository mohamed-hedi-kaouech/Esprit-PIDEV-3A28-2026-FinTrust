package org.example.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OtpStore {

    private static class Entry {
        final String code;
        final long expiresAtEpochSec;

        Entry(String code, long expiresAtEpochSec) {
            this.code = code;
            this.expiresAtEpochSec = expiresAtEpochSec;
        }
    }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    public void save(String key, String code, int ttlSeconds) {
        long exp = Instant.now().getEpochSecond() + ttlSeconds;
        store.put(key, new Entry(code, exp));
    }

    public boolean verifyAndConsume(String key, String code) {
        Entry e = store.get(key);
        if (e == null) return false;

        long now = Instant.now().getEpochSecond();
        if (now > e.expiresAtEpochSec) {
            store.remove(key);
            return false;
        }
        if (!e.code.equals(code)) return false;

        store.remove(key);
        return true;
    }

    public void remove(String key) {
        store.remove(key);
    }
}
