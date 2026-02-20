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
import org.example.Model.Loan.LoanClass.Repayment;
import org.example.Model.Loan.LoanEnum.RepaymentStatus;
import org.example.Service.LoanService.RepaymentService;

import java.util.Optional;

public class RepaymentListController {

    @FXML private TableView<Repayment> repaymentTable;
    @FXML private TableColumn<Repayment, Integer> colMonth;
    @FXML private TableColumn<Repayment, Double> colAmount;
    @FXML private TableColumn<Repayment, Double> colCapital;
    @FXML private TableColumn<Repayment, Double> colInterest;
    @FXML private TableColumn<Repayment, RepaymentStatus> colStatus;
    @FXML private TableColumn<Repayment, Void> colPay;

    private final RepaymentService service = new RepaymentService();
    private final ObservableList<Repayment> repaymentList = FXCollections.observableArrayList();

    private int loanId;

    // ======================
    // SET LOAN
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
    // SETUP TABLE
    // ======================
    private void setupColumns() {

        colMonth.setCellValueFactory(new PropertyValueFactory<>("month"));

        // FIXED PROPERTY NAME
        colAmount.setCellValueFactory(new PropertyValueFactory<>("monthlyPayment"));
        colCapital.setCellValueFactory(new PropertyValueFactory<>("capitalPart"));
        colInterest.setCellValueFactory(new PropertyValueFactory<>("interestPart"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colAmount.setCellFactory(col -> formatMoneyCell());
        colCapital.setCellFactory(col -> formatMoneyCell());
        colInterest.setCellFactory(col -> formatMoneyCell());

        colAmount.setStyle("-fx-alignment: CENTER-RIGHT;");
        colCapital.setStyle("-fx-alignment: CENTER-RIGHT;");
        colInterest.setStyle("-fx-alignment: CENTER-RIGHT;");
        colMonth.setStyle("-fx-alignment: CENTER;");

        // CLEAN STATUS STYLE USING CSS CLASSES
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(RepaymentStatus status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(status.name());

                    badge.getStyleClass().clear();

                    if (status == RepaymentStatus.PAID) {
                        badge.getStyleClass().add("status-completed");
                    } else {
                        badge.getStyleClass().add("status-pending");
                    }

                    setGraphic(badge);
                    setText(null);
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
    // PAY BUTTON
    // ======================
    private void setupButtons() {

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
    }

    // ======================
    // LOAD DATA
    // ======================
    private void loadData() {
        repaymentList.setAll(service.getByLoan(loanId));
        repaymentTable.setItems(repaymentList);
    }

    // ======================
    // HANDLE PAY
    // ======================
    private void handlePay(Repayment r) {

        if (r.getStatus() == RepaymentStatus.PAID) {
            showError("Cette échéance est déjà payée.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Paiement échéance n° " + r.getMonth());
        confirm.setContentText("Confirmer le paiement ?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            service.markAsPaid(r.getRepayId());

            // Update local object instead of full reload
            r.setStatus(RepaymentStatus.PAID);
            repaymentTable.refresh();
        }
    }

    // ======================
    // BACK
    // ======================
    @FXML
    private void handleBack() {

        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/Loan/LoanListUser.fxml")
            );

            Stage stage = (Stage) repaymentTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mes prêts");

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