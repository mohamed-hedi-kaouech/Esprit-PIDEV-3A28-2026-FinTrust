package org.example.Controlleurs.UserControlleur;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Model.User.User;
import org.example.Service.UserService.UserService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class UserCreationController implements Initializable {

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtPrenom;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtNumTel;
    @FXML
    private ComboBox<String> comboRole;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblStatus;

    private ListView<User> listViewUsers; // Optionnel si tu passes la ListView
    private TableView<User> tableUsers;   // Optionnel si tu passes la TableView

    private final UserService userService = new UserService();

    public void setListViewUsers(ListView<User> listViewUsers) {
        this.listViewUsers = listViewUsers;
    }

    public void setTableUsers(TableView<User> tableUsers) {
        this.tableUsers = tableUsers;
    }

    @FXML
    private ComboBox<String> statusComboBox; // au lieu de lblStatus

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // ROLE : toujours CLIENT
        comboRole.getItems().clear();
        comboRole.getItems().add("CLIENT");
        comboRole.setValue("CLIENT");

        // STATUT : choix possible
        statusComboBox.getItems().clear();
        statusComboBox.getItems().addAll("EN ATTENTE", "VALIDÉ", "INACTIF");
        statusComboBox.setValue("EN ATTENTE"); // valeur par défaut

        // Vérification optionnelle
        String statut = statusComboBox.getValue();
        if (statut.equals("EN ATTENTE") || statut.equals("VALIDÉ") || statut.equals("INACTIF")) {
            System.out.println("Statut correct : " + statut);
        } else {
            System.out.println("Statut invalide !");
            statusComboBox.setValue("EN ATTENTE"); // valeur par défaut
        }
    }




    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();
        if (txtNom.getText().isEmpty()) errors.append("- Le nom est obligatoire.\n");
        if (txtPrenom.getText().isEmpty()) errors.append("- Le prénom est obligatoire.\n");
        if (txtEmail.getText().isEmpty()) errors.append("- L'email est obligatoire.\n");
        if (txtNumTel.getText().isEmpty()) errors.append("- Le numéro de téléphone est obligatoire.\n");
        if (txtPassword.getText().isEmpty()) errors.append("- Le mot de passe est obligatoire.\n");

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errors.toString());
            return false;
        }
        return true;
    }

    @FXML
    private void handleClear() {
        if (txtNom != null) txtNom.clear();
        if (txtPrenom != null) txtPrenom.clear();
        if (txtEmail != null) txtEmail.clear();
        if (txtNumTel != null) txtNumTel.clear();
        if (txtPassword != null) txtPassword.clear();
        if (comboRole != null) comboRole.setValue("Client");
        if (lblStatus != null) lblStatus.setText("En attente");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goBackToList(ActionEvent event) {

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/User/listUsers.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des utilisateurs");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner à la liste des utilisateurs.");
        }

    }


    @FXML
    public void createUser(ActionEvent event) {
        if (!validateInput()) return;

        User newUser = new User(
                txtNom.getText().trim(),
                txtPrenom.getText().trim(),
                txtEmail.getText().trim(),
                txtNumTel.getText().trim(),
                comboRole.getValue(),         // "CLIENT"
                txtPassword.getText().trim(),
                statusComboBox.getValue(),    // récupérer le statut sélectionné
                LocalDateTime.now()           // <-- important, ne doit pas être null
        );


        try {
            // Ajouter en BDD
            userService.Add(newUser);

            // Ajouter dans la ListView/TableView si existante
            if (listViewUsers != null) listViewUsers.getItems().add(newUser);
            if (tableUsers != null) tableUsers.getItems().add(newUser);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur créé avec succès !");

            // Retour à la liste
            goBackToList(event);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter l'utilisateur en base.\n" + e.getMessage());
        }
    }
}
