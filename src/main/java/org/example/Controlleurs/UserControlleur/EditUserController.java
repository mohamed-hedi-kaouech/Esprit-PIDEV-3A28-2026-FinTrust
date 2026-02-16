package org.example.Controlleurs.UserControlleur;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Model.User.User;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class EditUserController implements Initializable {

    @FXML
    private Label currentNameLabel;
    @FXML
    private Label currentEmailLabel;
    @FXML
    private Label currentRoleLabel;
    @FXML
    private Label currentStatusLabel;
    @FXML
    private Label currentCreatedAtLabel;


    // Form fields
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtNumTel;
    @FXML private TextField txtRole;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblStatus;

    // Buttons
    @FXML private Button updateButton;
    @FXML private Button cancelButton;

    private User currentUser;
    @FXML private VBox rootPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (lblStatus != null) {
            lblStatus.setText("En attente");
        }
    }

    @FXML
    private ComboBox<String> roleComboBox;       // Déjà injecté depuis le FXML
    @FXML
    private ComboBox<String> kycStatusComboBox; // Déjà injecté depuis le FXML
    @FXML
    private DatePicker createdAtPicker;          // Déjà injecté depuis le FXML

    public void loadUser(User user) {
        if (user == null) return;

        this.currentUser = user;

        // Remplir les champs texte
        txtNom.setText(user.getNom());
        txtPrenom.setText(user.getPrenom());
        txtEmail.setText(user.getEmail());
        txtNumTel.setText(user.getNumTel());
        txtRole.setText(user.getRole());
        txtPassword.setText(user.getPassword());

        // Remplir les ComboBox
        roleComboBox.setValue(user.getRole());
        kycStatusComboBox.setValue(user.getKycStatus());

        // Remplir la date
        if (user.getCreatedAt() != null) {
            createdAtPicker.setValue(user.getCreatedAt().toLocalDate());
        }

        // Optionnel : synchroniser les valeurs dans l'objet courant
        currentUser.setRole(roleComboBox.getValue());
        currentUser.setKycStatus(kycStatusComboBox.getValue());
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (!validateInput()) return;

        // Confirmation
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer les modifications");
        confirm.setHeaderText("Êtes-vous sûr de vouloir enregistrer les modifications ?");
        confirm.setContentText("Utilisateur : " + currentUser.getNom() + " " + currentUser.getPrenom());
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Mettre à jour l'utilisateur
            currentUser.setNom(txtNom.getText());
            currentUser.setPrenom(txtPrenom.getText());
            currentUser.setEmail(txtEmail.getText());
            currentUser.setNumTel(txtNumTel.getText());
            currentUser.setRole(txtRole.getText());
            currentUser.setPassword(txtPassword.getText());
            // kycStatus reste inchangé (ou tu peux le modifier si besoin)
            // createdAt reste inchangé

            showSuccessAlert("Succès", "Utilisateur modifié avec succès !");

            // Actualiser les labels
            loadUser(currentUser);

            // Optionnel : revenir à la liste
            goBack();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Annuler les modifications");
        confirm.setHeaderText("Voulez-vous annuler les modifications ?");
        confirm.setContentText("Les modifications non enregistrées seront perdues.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            loadUser(currentUser); // recharger les valeurs initiales
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (txtNom.getText().isEmpty()) errors.append("- Le nom est obligatoire.\n");
        if (txtPrenom.getText().isEmpty()) errors.append("- Le prénom est obligatoire.\n");
        if (txtEmail.getText().isEmpty()) errors.append("- L'email est obligatoire.\n");
        if (txtNumTel.getText().isEmpty()) errors.append("- Le numéro de téléphone est obligatoire.\n");
        if (txtRole.getText().isEmpty()) errors.append("- Le rôle est obligatoire.\n");
        if (txtPassword.getText().isEmpty()) errors.append("- Le mot de passe est obligatoire.\n");

        if (errors.length() > 0) {
            showErrorAlert("Erreur de validation", errors.toString());
            return false;
        }

        return true;
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/listUsers.fxml"));
            Stage stage = (Stage) Stage.getWindows()
                    .filtered(window -> window.isShowing())
                    .get(0);
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des utilisateurs");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void goBackToList(ActionEvent actionEvent) {
    }
}
