package org.example.Utils;

import org.example.Model.Budget.Alerte;
import org.example.Service.BudgetService.BudgetService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Very small in-process notification center used to notify UI controllers
 * when a new Alerte is created so they can refresh their views immediately.
 * Also triggers email sending when an alert is posted.
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
        // Notify UI listeners
        List<Consumer<Alerte>> snapshot;
        synchronized (alertListeners) {
            snapshot = new ArrayList<>(alertListeners);
        }
        for (Consumer<Alerte> c : snapshot) {
            try { c.accept(a); } catch (Exception ignored) {}
        }

        // Send email asynchronously
        new Thread(() -> {
            try {
                System.out.println("[NotificationCenter] Sending alert email for category " + a.getIdCategorie());
                BudgetService bs = new BudgetService();
                var categorie = bs.ReadId(a.getIdCategorie());
                String categoryName = (categorie != null) ? categorie.getNomCategorie() : ("Categorie_" + a.getIdCategorie());
                EmailSender.sendAlerteEmail(a, categoryName);
            } catch (Exception e) {
                System.err.println("[NotificationCenter] Failed to send alert email: " + e.getMessage());
                e.printStackTrace();
            }
        }, "alerte-email-sender").start();
    }
}
