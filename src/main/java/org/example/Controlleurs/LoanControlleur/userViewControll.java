package org.example.Controlleurs.LoanControlleur;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.Model.Loan.LoanClass.Loan;
import org.example.Service.LoanService.LoanService;

import java.util.List;

public class userViewControll {

    @FXML private TableView<Loan> loanTable;
    @FXML private TableColumn<Loan, String> colType;
    @FXML private TableColumn<Loan, Double> colAmount;
    @FXML private TableColumn<Loan, Integer> colDuration;
    @FXML private TableColumn<Loan, Double> colRate;
    @FXML private TableColumn<Loan, Double> colRemaining;
    @FXML private TableColumn<Loan, String> colStatus;
    @FXML private TableColumn<Loan, Void> colAction;

    private final LoanService loanService = new LoanService();

    @FXML
    public void initialize() {

        colType.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        cell.getValue().getLoanType().name()));

        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colRate.setCellValueFactory(new PropertyValueFactory<>("interestRate"));
        colRemaining.setCellValueFactory(new PropertyValueFactory<>("remainingPrincipal"));
        colStatus.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        cell.getValue().getStatus().name()));

        formatMoneyColumn(colAmount);
        formatMoneyColumn(colRemaining);

        addDetailButton();

        loadData();
    }

    private void loadData() {
        List<Loan> loans = loanService.ReadAll();
        loanTable.setItems(FXCollections.observableArrayList(loans));
    }

    private void formatMoneyColumn(TableColumn<Loan, Double> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null :
                        String.format("%.2f DT", value));
            }
        });
    }

    private void addDetailButton() {

        colAction.setCellFactory(param -> new TableCell<>() {

            private final Button btn = new Button("Voir");

            {
                btn.getStyleClass().add("btn-primary");

                btn.setOnAction(event -> {
                    Loan loan = getTableView().getItems().get(getIndex());
                    openLoanDetail(loan);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void openLoanDetail(Loan loan) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Loan/RepaymentList.fxml")
            );

            Parent root = loader.load();

            RepaymentListController controller =
                    loader.getController();

            controller.setLoanId(loan.getLoanId());

            Stage stage =
                    (Stage) loanTable.getScene().getWindow();

            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNewLoan() {

        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/Loan/Simulator.fxml")
            );

            Stage stage =
                    (Stage) loanTable.getScene().getWindow();

            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/Client/ClientDashboard.fxml")
            );

            Stage stage = (Stage) loanTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mes prêts");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
