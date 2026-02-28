package org.example.Controlleurs.LoanControlleur;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.Model.Loan.LoanClass.Loan;
import org.example.Model.Loan.LoanEnum.LoanStatus;
import org.example.Service.LoanService.LoanService;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoanManagerGUI implements Initializable {

    @FXML private TableView<Loan> loanTable;
    @FXML private TableColumn<Loan, Integer> colId;
    @FXML private TableColumn<Loan, Double> colAmount;
    @FXML private TableColumn<Loan, Integer> colDuration;
    @FXML private TableColumn<Loan, LoanStatus> colStatus;
    @FXML private TableColumn<Loan, Double> colRate;
    @FXML private TableColumn<Loan, Double> colRemaining;
    @FXML private TableColumn<Loan, Void> colUpdate;
    @FXML private TableColumn<Loan, Void> colDelete;

    @FXML private TextField searchField;
    @FXML private Label totalLoansLabel;
    @FXML private TableColumn<Loan, Void> colRepayments;

    private ObservableList<Loan> loanList = FXCollections.observableArrayList();
    private ObservableList<Loan> filteredList = FXCollections.observableArrayList();

    private LoanService service;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        service = new LoanService();

        setupColumns();
        setupButtons();
        loadData();
        setupSearch();
    }

    private void setupColumns() {

        colId.setCellValueFactory(new PropertyValueFactory<>("loanId"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colRate.setCellValueFactory(new PropertyValueFactory<>("interestRate"));
        colRemaining.setCellValueFactory(new PropertyValueFactory<>("remainingPrincipal"));

        colAmount.setCellFactory(col -> new TableCell<Loan, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%.2f DT", value));
            }
        });

        colRemaining.setCellFactory(col -> new TableCell<Loan, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%.2f DT", value));
            }
        });

        colStatus.setCellFactory(col -> new TableCell<Loan, LoanStatus>() {
            @Override
            protected void updateItem(LoanStatus status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.name());
                    if (status == LoanStatus.ACTIVE) {
                        setStyle("-fx-background-color:#e8f5e9; -fx-text-fill:#2e7d32;");
                    } else if (status == LoanStatus.COMPLETED) {
                        setStyle("-fx-background-color:#e3f2fd; -fx-text-fill:#1565C0;");
                    }else if (status == LoanStatus.PENDING) {
                        setStyle("-fx-background-color:#fff3e0; -fx-text-fill:#ef6c00;");
                    }
                }
            }
        });
    }


    private void setupButtons() {

        colUpdate.setCellFactory(getButtonCell("Modifier", "btn-update", true));
        colDelete.setCellFactory(getButtonCell("Supprimer", "btn-delete", false));

        colRepayments.setCellFactory(param -> new TableCell<>() {

            private final Button btn = new Button("Voir");

            {
                btn.getStyleClass().add("btn-primary");

                btn.setOnAction(e -> {
                    Loan loan = getTableView().getItems().get(getIndex());
                    openRepaymentPage(loan);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private Callback<TableColumn<Loan, Void>, TableCell<Loan, Void>> getButtonCell(String text, String style, boolean isUpdate) {

        return param -> new TableCell<>() {
            private final Button btn = new Button(text);

            {
                btn.getStyleClass().add(style);
                btn.setOnAction(e -> {
                    Loan loan = getTableView().getItems().get(getIndex());
                    if (isUpdate) handleUpdate(loan);
                    else handleDelete(loan);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
                setAlignment(Pos.CENTER);
            }
        };
    }

    private void loadData() {
        loanList.setAll(service.ReadAll());
        filteredList.setAll(loanList);
        loanTable.setItems(filteredList);
        updateTotal();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.clear();
            if (newVal == null || newVal.isEmpty()) {
                filteredList.setAll(loanList);
            } else {
                for (Loan l : loanList) {
                    if (String.valueOf(l.getLoanId()).contains(newVal) ||
                            l.getStatus().name().toLowerCase().contains(newVal.toLowerCase())) {
                        filteredList.add(l);
                    }
                }
            }
            updateTotal();
        });
    }

    private void updateTotal() {
        totalLoansLabel.setText("Total: " + filteredList.size() + " prêt(s)");
    }

    private void handleUpdate(Loan loan) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Loan/UpdateLoan.fxml")
            );

            Parent root = loader.load();

            UpdateLoanControlleur controller = loader.getController();
            controller.setLoanData(loan);

            Stage stage = (Stage) loanTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier prêt");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Loan loan) {

        if (loan.getStatus() == LoanStatus.ACTIVE) {
            showError("Impossible de supprimer un prêt actif.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer le prêt #" + loan.getLoanId());
        confirm.setContentText(
                "Montant: " + loan.getAmount() + " DT\n" +
                        "Statut: " + loan.getStatus() + "\n\n" +
                        "Cette action est irréversible."
        );

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            service.Delete(loan.getLoanId());

            showSuccess("Prêt supprimé avec succès.");

            loadData();
        }
    }

    @FXML
    private void goToCreatePage() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Loan/Simulator.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage) loanTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Simulateur de prêt");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void openRepaymentPage(Loan loan) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Loan/RepaymentList.fxml")
            );

            Parent root = loader.load();

            RepaymentListController controller = loader.getController();
            controller.setLoanId(loan.getLoanId());

            Stage stage = (Stage) loanTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Remboursements du prêt #" + loan.getLoanId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
