package org.example.Utils;

import org.example.Model.Budget.Alerte;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Very small in-process notification center used to notify UI controllers
 * when a new Alerte is created so they can refresh their views immediately.
 */
public class NotificationCenter {

    private static final NotificationCenter INSTANCE = new NotificationCenter();

    public static NotificationCenter getInstance() { return INSTANCE; }

    private final List<Consumer<Alerte>> alertListeners = new ArrayList<>();

    private NotificationCenter() {}

    public void addAlerteListener(Consumer<Alerte> listener) {
        synchronized (alertListeners) {
            alertListeners.add(listener);
        }
    }

    public void removeAlerteListener(Consumer<Alerte> listener) {
        synchronized (alertListeners) {
            alertListeners.remove(listener);
        }
    }

    public void postAlerte(Alerte a) {
        List<Consumer<Alerte>> snapshot;
        synchronized (alertListeners) {
            snapshot = new ArrayList<>(alertListeners);
        }
        for (Consumer<Alerte> c : snapshot) {
            try { c.accept(a); } catch (Exception ignored) {}
        }
    }
}
