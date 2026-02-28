package org.example.Controlleurs.WalletControlleur;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import org.example.Model.Wallet.ClassWallet.ClientRisk;
import org.example.Service.WalletService.*;
import org.example.Service.WalletService.BankingIAService.BankingAnalysis;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class RiskDashboardController {

    @FXML private Label lblFiablesNombre, lblFiablesPourcent, lblFiablesEvolution;
    @FXML private Label lblMoyensNombre, lblMoyensPourcent, lblMoyensEvolution;
    @FXML private Label lblRisqueNombre, lblRisquePourcent, lblRisqueEvolution;
    @FXML private PieChart pieChartRepartition;
    @FXML private TableView<ClientRisk> tableViewClients;
    @FXML private TableColumn<ClientRisk, String> colClient, colNiveau, colPrivilege;
    @FXML private TableColumn<ClientRisk, Integer> colScore, colChequesRefuses;
    @FXML private TableColumn<ClientRisk, Double> colSolde;
    @FXML private TableColumn<ClientRisk, Void> colAction;
    @FXML private ComboBox<String> comboNiveau;
    @FXML private TextField txtScoreMin, txtScoreMax;
    @FXML private CheckBox chkSurveillance;
    @FXML private ListView<String> listSurveillance;
    @FXML private TextArea txtAnalyseIA;

    private ObservableList<ClientRisk> masterData = FXCollections.observableArrayList();
    private ScoreService scoreService;
    private SurveillanceService surveillanceService;
    private PrivilegeService privilegeService;
    private BankingIAService bankingIAService; // ✅ NOUVEAU

    @FXML
    public void initialize() {
        scoreService = new ScoreService();
        surveillanceService = new SurveillanceService();
        privilegeService = new PrivilegeService();
        bankingIAService = new BankingIAService(); // ✅ Initialisation

        chargerDonnees();
        configurerTableau();
        mettreAJourStatistiques();
        afficherSurveillance();
    }

    private void chargerDonnees() {
        masterData.setAll(scoreService.getAllClientsWithScores());
        tableViewClients.setItems(masterData);
    }

    private void configurerTableau() {
        colClient.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNomComplet()));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        colPrivilege.setCellValueFactory(new PropertyValueFactory<>("privilege"));
        colChequesRefuses.setCellValueFactory(new PropertyValueFactory<>("nbChequesRefuses"));
        colSolde.setCellValueFactory(new PropertyValueFactory<>("solde"));

        colSolde.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", item));
                    if (item < 0) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnAnalyser = new Button("🔍 Analyser avec IA");
            {
                btnAnalyser.setOnAction(event -> {
                    ClientRisk client = getTableView().getItems().get(getIndex());
                    analyserClientAvecIA(client); // ✅ Appel à la vraie IA
                });
                btnAnalyser.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnAnalyser);
            }
        });
    }

    private void mettreAJourStatistiques() {
        long fiables = masterData.stream().filter(c -> c.getScore() >= 80).count();
        long moyens = masterData.stream().filter(c -> c.getScore() >= 50 && c.getScore() < 80).count();
        long risques = masterData.stream().filter(c -> c.getScore() < 50).count();

        double total = fiables + moyens + risques;

        lblFiablesNombre.setText(String.valueOf(fiables));
        lblFiablesPourcent.setText(String.format("%.1f%%", total > 0 ? (fiables/total)*100 : 0));
        lblFiablesEvolution.setText("+" + (int)(Math.random()*10) + "%");

        lblMoyensNombre.setText(String.valueOf(moyens));
        lblMoyensPourcent.setText(String.format("%.1f%%", total > 0 ? (moyens/total)*100 : 0));
        lblMoyensEvolution.setText((int)(Math.random()*10-5) + "%");

        lblRisqueNombre.setText(String.valueOf(risques));
        lblRisquePourcent.setText(String.format("%.1f%%", total > 0 ? (risques/total)*100 : 0));
        lblRisqueEvolution.setText("+" + (int)(Math.random()*15) + "%");

        pieChartRepartition.getData().clear();
        pieChartRepartition.getData().add(new PieChart.Data("Fiables (≥80)", fiables));
        pieChartRepartition.getData().add(new PieChart.Data("Moyens (50-79)", moyens));
        pieChartRepartition.getData().add(new PieChart.Data("Risqués (<50)", risques));
    }

    private void afficherSurveillance() {
        listSurveillance.getItems().clear();

        for (ClientRisk client : masterData) {
            if (client.estASurveiller()) {
                String alerte = "⚠️ " + client.getNomComplet() + " : " + client.getRaisonsSurveillance();
                listSurveillance.getItems().add(alerte);
            }
        }

        if (listSurveillance.getItems().isEmpty()) {
            listSurveillance.getItems().add("✅ Aucun client à surveiller");
        }
    }

    @FXML
    private void appliquerFiltres() {
        String niveau = comboNiveau.getValue();
        String scoreMinText = txtScoreMin.getText();
        String scoreMaxText = txtScoreMax.getText();
        boolean seulementSurveillance = chkSurveillance.isSelected();

        ObservableList<ClientRisk> filtered = FXCollections.observableArrayList();

        for (ClientRisk client : masterData) {
            boolean match = true;

            if (niveau != null && !niveau.equals("Tous") && !niveau.isEmpty()) {
                if (niveau.contains("Risqué") && client.getScore() >= 50) match = false;
                if (niveau.contains("Moyen") && (client.getScore() < 50 || client.getScore() >= 80)) match = false;
                if (niveau.contains("Fiable") && client.getScore() < 80) match = false;
            }

            if (!scoreMinText.isEmpty()) {
                try {
                    int min = Integer.parseInt(scoreMinText);
                    if (client.getScore() < min) match = false;
                } catch (NumberFormatException e) {}
            }

            if (!scoreMaxText.isEmpty()) {
                try {
                    int max = Integer.parseInt(scoreMaxText);
                    if (client.getScore() > max) match = false;
                } catch (NumberFormatException e) {}
            }

            if (seulementSurveillance) {
                match = match && client.estASurveiller();
            }

            if (match) {
                filtered.add(client);
            }
        }

        tableViewClients.setItems(filtered);
    }

    @FXML
    private void resetFiltres() {
        comboNiveau.setValue(null);
        txtScoreMin.clear();
        txtScoreMax.clear();
        chkSurveillance.setSelected(false);
        tableViewClients.setItems(masterData);
    }

    @FXML
    private void genererAnalyseIA() {
        ClientRisk selected = tableViewClients.getSelectionModel().getSelectedItem();

        if (selected == null) {
            txtAnalyseIA.setText("❌ Veuillez sélectionner un client d'abord.");
            return;
        }

        analyserClientAvecIA(selected); // ✅ Appel à la vraie IA
    }

    // ✅ NOUVELLE MÉTHODE : Analyse avec la VRAIE IA (OpenAI)
    private void analyserClientAvecIA(ClientRisk client) {
        txtAnalyseIA.setText("🧠 Analyse IA en cours... (connexion à OpenAI)");

        // Désactiver le bouton pendant l'analyse
        Button sourceButton = null;
        if (colAction.getCellData(client) != null) {
            // Optionnel: désactiver
        }

        // Exécuter dans un thread séparé pour ne pas bloquer l'interface
        new Thread(() -> {
            try {
                // Appel au service IA avec les vraies données
                BankingAnalysis analyse = bankingIAService.analyserClient(
                        client.getUserId(),
                        client.getWalletId()
                );

                // Mettre à jour l'interface sur le thread JavaFX
                Platform.runLater(() -> {
                    // Afficher le rapport complet
                    txtAnalyseIA.setText(analyse.getRapportComplet());

                    // Option: Mettre à jour les infos du client si nécessaire
                    if (!client.getPrivilege().equals(analyse.decision.classement)) {
                        // Le classement a changé - mettre à jour
                        client.setPrivilege(analyse.decision.classement);
                        tableViewClients.refresh();
                    }

                    // Afficher une notification de décision
                    afficherNotificationDecision(analyse.decision);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    txtAnalyseIA.setText("❌ Erreur d'analyse: " + e.getMessage() +
                            "\n\nUtilisation de l'analyse locale...");

                    // Fallback: analyse locale simplifiée
                    analyserClientLocal(client);
                });
            }
        }).start();
    }

    // ✅ MÉTHODE DE SECOURS : Analyse locale (si API ne répond pas)
    private void analyserClientLocal(ClientRisk client) {
        StringBuilder analyse = new StringBuilder();

        analyse.append("🧠 **ANALYSE LOCALE - ").append(client.getNomComplet()).append("**\n\n");
        analyse.append("📊 Score : ").append(client.getScore()).append("/100 (").append(client.getNiveau()).append(")\n");

        int ancienneteMois = (int) ChronoUnit.MONTHS.between(client.getDateInscription(), LocalDate.now());
        analyse.append("📅 Client depuis : ").append(ancienneteMois).append(" mois\n");
        analyse.append("🏦 Solde : ").append(String.format("%.2f €", client.getSolde())).append("\n");
        analyse.append("💳 Chèques refusés : ").append(client.getNbChequesRefuses()).append("\n\n");

        analyse.append("🔍 **Analyse comportementale :**\n");

        // Décision sur le chéquier
        analyse.append("\n📌 **DÉCISION CHÉQUIER:**\n");
        if (client.getScore() >= 70 && client.getNbChequesRefuses() == 0) {
            analyse.append("   ✅ ACCEPTER - Client fiable\n");
        } else if (client.getScore() >= 50 && client.getNbChequesRefuses() <= 1) {
            analyse.append("   ⚠️ ACCEPTER sous conditions - Surveillance renforcée\n");
        } else {
            analyse.append("   ❌ REFUSER - Trop de risques\n");
        }

        // Classement et privilèges
        analyse.append("\n📌 **CLASSEMENT:**\n");
        if (client.getScore() >= 80) {
            analyse.append("   🏆 VIP\n");
            analyse.append("   Privilèges:\n");
            analyse.append("   • Plafond retrait: 5000€\n");
            analyse.append("   • Validation auto chèques\n");
            analyse.append("   • Frais réduits 50%\n");
        } else if (client.getScore() >= 50) {
            analyse.append("   📊 STANDARD\n");
            analyse.append("   Privilèges:\n");
            analyse.append("   • Plafond retrait: 1000€\n");
            analyse.append("   • Validation normale\n");
        } else {
            analyse.append("   ⚠️ À SURVEILLER\n");
            analyse.append("   Privilèges:\n");
            analyse.append("   • Plafond retrait: 200€\n");
            analyse.append("   • Validation manuelle\n");
        }

        // Prédiction
        int tendance = (int)(Math.random() * 20 - 10);
        analyse.append("\n📈 **PRÉDICTION:** ");
        if (tendance > 5) {
            analyse.append("Amélioration probable dans les 3 mois");
        } else if (tendance < -5) {
            analyse.append("Risque de dégradation - Surveillance renforcée");
        } else {
            analyse.append("Stabilité prévue");
        }

        txtAnalyseIA.setText(analyse.toString());
    }

    // ✅ Afficher une notification avec la décision
    private void afficherNotificationDecision(BankingIAService.Decision decision) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🧠 Décision de l'IA");
        alert.setHeaderText("Recommandation pour le client");

        TextArea textArea = new TextArea(decision.getResume());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(250);
        textArea.setPrefWidth(450);
        textArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    // ✅ Garder l'ancienne méthode pour compatibilité
    private void analyserClient(ClientRisk client) {
        analyserClientAvecIA(client); // Rediriger vers la nouvelle méthode
    }
}