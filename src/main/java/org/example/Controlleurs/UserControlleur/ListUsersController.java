package org.example.Controlleurs.UserControlleur;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.example.Controlleurs.KYCController.ConsultKYCController;
import org.example.Model.User.User;
import org.example.Service.UserService.UserService;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;


public class ListUsersController implements Initializable {

    @FXML
    private ListView<User> userListView;

    @FXML
    private TextField searchField;

    @FXML
    private Label totalUsersLabel;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private ObservableList<User> filteredList = FXCollections.observableArrayList();
    private UserService userService;
    private Image event;
    private TableView<User> tableUsers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = new UserService();
        setupListView();
        loadUserData();
        setupSearchListener();
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterUsers(newValue);
        });
    }

    private void loadUserData() {
        try {
            userList.clear();

            // Charger depuis service
            userList.addAll(userService.ReadAll());

            // Copier dans la liste filtrée
            filteredList.setAll(userList);

            // Afficher dans la ListView
            userListView.setItems(filteredList);

            updateTotalLabel();

        } catch (Exception e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de charger les utilisateurs");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    @FXML

    private void setupListView() {
        userListView.setCellFactory(list -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Ligne horizontale
                    HBox row = new HBox(10);
                    row.setStyle("-fx-padding: 5; -fx-alignment: center-left;");

                    // Label utilisateur
                    Label userLabel = new Label(user.getNom() + " " + user.getPrenom() + " | " + user.getEmail());

                    // Spacer pour pousser les "icônes" à droite
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                    // Boutons avec emojis comme icônes
                    Button editBtn = new Button("✏️");
                    editBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16;");
                    editBtn.setOnAction(e -> handleEdit(user));

                    Button kycBtn = new Button("📝");
                    kycBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16;");
                    kycBtn.setOnAction(e -> handleKYC(user));

                    Button deleteBtn = new Button("🗑️");
                    deleteBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16;");
                    deleteBtn.setOnAction(e -> handleDelete(user));

                    // Ajouter tous les éléments à la ligne
                    row.getChildren().addAll(userLabel, spacer, editBtn, kycBtn, deleteBtn);

                    // Définir la ligne comme graphique de la cellule
                    setGraphic(row);
                }
            }
        });

        // Placeholder si vide
        Label placeholder = new Label("Aucun utilisateur disponible");
        placeholder.setStyle("-fx-text-fill:#7f8c8d; -fx-font-size:14px;");
        userListView.setPlaceholder(placeholder);
    }


    private void handleEdit(User user) {
        if (user == null) return;

        try {
            // Vérifie que le FXML existe et récupère l'URL
            URL fxmlUrl = getClass().getResource("/User/editUser.fxml");
            if (fxmlUrl == null) {
                System.out.println("Fichier FXML introuvable !");
                return; // arrête la méthode si le fichier est introuvable
            }

            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Récupérer le contrôleur
            EditUserController controller = loader.getController();

            // Charger l'utilisateur sélectionné
            controller.loadUser(user);

            // Ouvrir la nouvelle fenêtre
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier l'utilisateur : " + user.getNom() + " " + user.getPrenom());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir la page de modification");
            alert.setContentText("Vérifiez le chemin du FXML et le contrôleur.\nDétails : " + e.getMessage());
            alert.showAndWait();
        }
    }




    private void handleDelete(User user) {
        if (user == null) return;

        // Confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer l'utilisateur");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cet utilisateur ?\n\n" +
                "Nom: " + user.getNom() + " " + user.getPrenom() + "\n" +
                "Email: " + user.getEmail() + "\n\n" +
                "Cette action est irréversible !");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Appel au service utilisateur pour suppression
            boolean deleted = userService.Delete(user.getId()); // Assure-toi que delete() existe dans UserService

            if (deleted) {
                // Retirer de la liste affichée
                filteredList.remove(user);
                userListView.getItems().remove(user);
                updateTotalLabel();

                // Message succès
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText(null);
                successAlert.setContentText("L'utilisateur a été supprimé avec succès !");
                successAlert.showAndWait();
            } else {
                // Message erreur
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Erreur lors de la suppression de l'utilisateur.");
                errorAlert.showAndWait();
            }
        }
    }


    private void handleKYC(User user) {
        try {
            // Charger le FXML de la page KYC
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/KYC/ConsultKYC.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur de la page KYC
            ConsultKYCController controller = loader.getController();

            // Passer l'utilisateur sélectionné au contrôleur KYC
            controller.setUser(user);

            // Ouvrir la nouvelle fenêtre
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Consultation KYC : " + user.getNom() + " " + user.getPrenom());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir la page KYC");
            alert.setContentText("Vérifiez le chemin du FXML et le contrôleur.\nDétails : " + e.getMessage());
            alert.showAndWait();
        }
    }


    @FXML
    public void goToCreateUserPage(ActionEvent event) {
        try {
            // ⚠ Assurez-vous que le FXML est dans src/main/resources/fxml/createUser.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/User/createUser.fxml"));

            if (loader.getLocation() == null) {
                throw new IOException("Le fichier FXML 'createUser.fxml' n'a pas été trouvé dans /User/");
            }

            Parent root = loader.load();

            // Récupérer le contrôleur de la page de création
            UserCreationController controller = loader.getController();

            // Passer la TableView si elle existe
            if (this.tableUsers != null) {
                controller.setTableUsers(this.tableUsers);
            }

            // Récupérer la fenêtre actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Définir la nouvelle scène
            stage.setScene(new Scene(root));
            stage.setTitle("Créer un nouvel utilisateur");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace(); // Affiche la vraie cause dans la console
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir la page de création");
            alert.setContentText("Vérifiez le chemin du FXML et le contrôleur.\nDétails : " + e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace(); // Exception dans initialize() ou constructeur du contrôleur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur critique");
            alert.setHeaderText("Erreur inattendue lors du chargement de la page");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }




    @FXML
    public void handleSearch(ActionEvent event) {
        String searchText = (searchField != null) ? searchField.getText() : "";
        filterUsers(searchText);
    }

    private void filterUsers(String searchText) {

        // Sécurité contre null
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredList.setAll(userList);
            updateTotalLabel();
            return;
        }

        String filter = searchText.toLowerCase().trim();

        filteredList.clear();

        for (User u : userList) {

            if (
                    (u.getNom() != null && u.getNom().toLowerCase().contains(filter)) ||
                            (u.getPrenom() != null && u.getPrenom().toLowerCase().contains(filter)) ||
                            (u.getEmail() != null && u.getEmail().toLowerCase().contains(filter)) ||
                            (u.getRole() != null && u.getRole().toLowerCase().contains(filter))
            ) {
                filteredList.add(u);
            }
        }

        updateTotalLabel();
    }
    @FXML
    private void updateTotalLabel() {
        if (totalUsersLabel == null) {
            return; // sécurité si le label n'est pas injecté
        }

        int total = filteredList.size();

        totalUsersLabel.setText(
                "Total : " + total + " utilisateur" + (total > 1 ? "s" : "")
        );
    }


    public void handleReset(ActionEvent actionEvent) {
        // Vider le champ de recherche
        searchField.clear();

        // Restaurer la liste complète
        filteredList.setAll(userList);

        // Rafraîchir l'affichage
        userListView.setItems(filteredList);

        // Mettre à jour le compteur
        updateTotalLabel();
    }
    @FXML
    private void goBackToMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/MenuGUI.fxml")
            );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Manager");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


