package org.example.Controlleurs.LoanControlleur;


import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.Model.Loan.LoanClass.Loan;
import org.example.Model.Loan.LoanClass.Repayment;
import org.example.Model.Loan.LoanEnum.LoanStatus;
import org.example.Service.LoanService.LoanService;
import org.example.Service.LoanService.RepaymentService;

import java.util.List;
import java.util.Optional;

public class AdminLoanController {

    @FXML
    private TableView<Loan> loanTable;
    @FXML private TableColumn<Loan, Integer> colId;
    @FXML private TableColumn<Loan, String> colType;
    @FXML private TableColumn<Loan, Double> colAmount;
    @FXML private TableColumn<Loan, Integer> colDuration;
    @FXML private TableColumn<Loan, LoanStatus> colStatus;
    @FXML private TableColumn<Loan, Void> colAction;

    private final LoanService loanService = new LoanService();
    private final RepaymentService repaymentService = new RepaymentService();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("loanId"));
        colType.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getLoanType().name()));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        addActionButtons();
        loadData();
    }
    @FXML
    private void loadData() {
        loanTable.getItems().setAll(loanService.ReadAll());
    }
    @FXML
    private void addActionButtons() {

        colAction.setCellFactory(col -> new TableCell<>() {

            private final Button approveBtn = new Button("Approve");
            private final Button deleteBtn = new Button("Delete");
            private final Button historyBtn = new Button("History");
            {
                approveBtn.getStyleClass().add("btn-add");
                deleteBtn.getStyleClass().add("btn-delete");
                historyBtn.getStyleClass().add("btn-primary");

                approveBtn.setOnAction(e -> {
                    Loan loan = getTableView().getItems().get(getIndex());
                    approveLoan(loan);
                });

                deleteBtn.setOnAction(e -> {
                    Loan loan = getTableView().getItems().get(getIndex());
                    rejectLoan(loan);
                    loadData();
                });
                historyBtn.setOnAction(e -> {
                    Loan loan = getTableView().getItems().get(getIndex());
                    openHistoryPage(loan);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Loan loan = getTableView().getItems().get(getIndex());
                    if (loan.getStatus() == LoanStatus.PENDING) {
                        setGraphic(new HBox(10, approveBtn, deleteBtn));
                    } else {
                        setGraphic(historyBtn);
                    }
                }
            }
        });
    }

    @FXML
    private void approveLoan(Loan loan) {

        loan.setStatus(LoanStatus.ACTIVE);
        loanService.Update(loan);
        loadData();
    }

    @FXML
    private void rejectLoan(Loan loan) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Refuser la demande de prêt ?");
        confirm.setContentText(
                "Cette action supprimera définitivement le prêt ID "
                        + loan.getLoanId() + ". Continuer ?"
        );

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            try {

                // Delete loan (repayments auto deleted via CASCADE)
                loanService.Delete(loan.getLoanId());

                // Refresh table
                loadData();

                // Success message
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Succès");
                success.setHeaderText(null);
                success.setContentText("Prêt supprimé avec succès.");
                success.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();

                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Erreur");
                error.setHeaderText(null);
                error.setContentText("Impossible de supprimer le prêt.");
                error.showAndWait();
            }
        }
    }

    @FXML
    private void openHistoryPage(Loan loan) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Loan/LoanHistoryPayment.fxml"));

            Parent root = loader.load();

            AdminLoanHistoryController controller =
                    loader.getController();

            controller.setLoan(loan);

            Stage stage = (Stage) loanTable.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/Loan/AdminDashboard.fxml"));
        Stage stage = (Stage) loanTable.getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}