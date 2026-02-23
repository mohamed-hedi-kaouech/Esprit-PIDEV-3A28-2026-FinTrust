package org.example.Controlleurs.AdminControlleur;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Model.User.User;
import org.example.Model.User.UserStatus;
import org.example.Service.UserService.UserService;
import org.example.Utils.SessionContext;

import java.lang.reflect.Method;

public class AdminUserEditController {

    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField numTelField;
    @FXML private ComboBox<UserStatus> statusComboBox;
    @FXML private Label infoLabel;

    private User userToEdit;
    private final UserService userService = new UserService();

    @FXML
    private void initialize() {
        // Remplir la combobox une seule fois
        statusComboBox.getItems().setAll(UserStatus.values());
    }

    public void setUserToEdit(User user) {
        this.userToEdit = user;

        if (user == null) {
            setInfo("Erreur: utilisateur null.", true);
            return;
        }

        nomField.setText(safe(user.getNom()));
        emailField.setText(safe(user.getEmail()));
        numTelField.setText(safe(user.getNumTel()));
        statusComboBox.setValue(user.getStatus());
    }

    @FXML
    private void handleSave() {
        if (userToEdit == null) {
            setInfo("Aucun utilisateur à modifier.", true);
            return;
        }

        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String tel = numTelField.getText() == null ? "" : numTelField.getText().trim();
        UserStatus status = statusComboBox.getValue();

        if (nom.isBlank() || email.isBlank()) {
            setInfo("Nom et Email sont obligatoires.", true);
            return;
        }
        if (status == null) {
            setInfo("Veuillez sélectionner un statut.", true);
            return;
        }

        // Mettre à jour l'objet en mémoire
        userToEdit.setNom(nom);
        userToEdit.setEmail(email);
        userToEdit.setNumTel(tel);
        userToEdit.setStatus(status);

        try {
            User admin = SessionContext.getInstance().getCurrentUser();

            // 1) Essayer une vraie mise à jour complète si elle existe dans ton service
            // Exemples possibles:
            // - updateUser(User admin, User user)
            // - updateUser(User user)
            boolean updated = tryUpdateUserFully(admin, userToEdit);

            // 2) Sinon, au minimum mettre à jour le statut (ce que tu faisais déjà)
            if (!updated) {
                userService.updateUserStatus(admin, userToEdit.getId(), userToEdit.getStatus());
                setInfo("Statut mis à jour. (MàJ complète non trouvée dans UserService)", false);
            } else {
                setInfo("Utilisateur modifié avec succès.", false);
            }

            // Fermer après succès
            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Erreur lors de l'enregistrement: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean tryUpdateUserFully(User admin, User user) {
        try {
            // updateUser(User admin, User user)
            Method m = userService.getClass().getMethod("updateUser", admin.getClass(), User.class);
            m.invoke(userService, admin, user);
            return true;
        } catch (Exception ignored) {
        }

        try {
            // updateUser(User user)
            Method m = userService.getClass().getMethod("updateUser", User.class);
            m.invoke(userService, user);
            return true;
        } catch (Exception ignored) {
        }

        return false;
    }

    private void closeWindow() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private void setInfo(String text, boolean isError) {
        if (infoLabel == null) return;
        infoLabel.setText(text == null ? "" : text);
        infoLabel.setStyle(isError ? "-fx-text-fill: #cc2e2e;" : "-fx-text-fill: #1d6b34;");
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
