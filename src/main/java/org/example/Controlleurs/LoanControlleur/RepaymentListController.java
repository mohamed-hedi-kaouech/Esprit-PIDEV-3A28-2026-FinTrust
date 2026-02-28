package org.example.Controlleurs.LoanControlleur;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.Model.Loan.LoanClass.Repayment;
import org.example.Model.Loan.LoanEnum.RepaymentStatus;
import org.example.Service.LoanService.RepaymentService;

import java.util.Optional;

public class RepaymentListController {

    @FXML private TableView<Repayment> repaymentTable;
    @FXML private TableColumn<Repayment, Integer> colNumber;
    @FXML private TableColumn<Repayment, Double> colAmount;
    @FXML private TableColumn<Repayment, Double> colCapital;
    @FXML private TableColumn<Repayment, Double> colInterest;
    @FXML private TableColumn<Repayment, RepaymentStatus> colStatus;
    @FXML private TableColumn<Repayment, Void> colPay;
    @FXML private TableColumn<Repayment, Void> colDelete;

    private RepaymentService service = new RepaymentService();
    private ObservableList<Repayment> repaymentList = FXCollections.observableArrayList();

    private int loanId;

    // ======================
    // Called from LoanManager
    // ======================
    public void setLoanId(int loanId) {
        this.loanId = loanId;
        loadData();
    }

    // ======================
    // INITIALIZE
    // ======================
    @FXML
    public void initialize() {
        setupColumns();
        setupButtons();
    }

    // ======================
    // Setup Columns
    // ======================
    private void setupColumns() {

        colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colCapital.setCellValueFactory(new PropertyValueFactory<>("capitalPart"));
        colInterest.setCellValueFactory(new PropertyValueFactory<>("interestPart"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colAmount.setCellFactory(col -> formatMoneyCell());
        colCapital.setCellFactory(col -> formatMoneyCell());
        colInterest.setCellFactory(col -> formatMoneyCell());

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(RepaymentStatus status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.name());

                    if (status == RepaymentStatus.PAID) {
                        setStyle("-fx-background-color:#e8f5e9; -fx-text-fill:#2e7d32;");
                    } else {
                        setStyle("-fx-background-color:#fff3e0; -fx-text-fill:#e65100;");
                    }
                }
            }
        });
    }

    private TableCell<Repayment, Double> formatMoneyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null :
                        String.format("%.2f DT", value));
            }
        };
    }

    // ======================
    // Setup Buttons
    // ======================
    private void setupButtons() {

        // PAY BUTTON
        colPay.setCellFactory(param -> new TableCell<>() {

            private final Button btn = new Button("Payer");

            {
                btn.getStyleClass().add("btn-primary");

                btn.setOnAction(e -> {
                    Repayment r = getTableView().getItems().get(getIndex());
                    handlePay(r);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Repayment r = getTableView().getItems().get(getIndex());
                    btn.setDisable(r.getStatus() == RepaymentStatus.PAID);
                    setGraphic(btn);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // DELETE BUTTON
        colDelete.setCellFactory(param -> new TableCell<>() {

            private final Button btn = new Button("Supprimer");

            {
                btn.getStyleClass().add("btn-delete");

                btn.setOnAction(e -> {
                    Repayment r = getTableView().getItems().get(getIndex());
                    handleDelete(r);
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

    // ======================
    // Load Data
    // ======================
    private void loadData() {
        repaymentList.setAll(service.getByLoan(loanId));
        repaymentTable.setItems(repaymentList);
    }

    // ======================
    // PAY
    // ======================
    private void handlePay(Repayment r) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Marquer paiement n°" + r.getNumber());
        confirm.setContentText("Confirmer le paiement ?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.markAsPaid(r.getRepayId());
            loadData();
        }
    }

    // ======================
    // DELETE
    // ======================
    private void handleDelete(Repayment r) {

        if (r.getStatus() == RepaymentStatus.PAID) {
            showError("Impossible de supprimer un paiement déjà payé.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer remboursement n°" + r.getNumber());

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.Delete(r.getRepayId());
            loadData();
        }
    }
    @FXML
    private void goToAddRepayment() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Loan/CreateRepayment.fxml")
            );

            Parent root = loader.load();

            CreateRepaymentController controller = loader.getController();
            controller.setLoanId(loanId);

            Stage stage = (Stage) repaymentTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter remboursement");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ======================
    // BACK TO LOAN LIST
    // ======================
    @FXML
    private void handleBack() {

        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/Loan/LoanList.fxml")
            );

            Stage stage = (Stage) repaymentTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des prêts");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}