package com.parcauto.controller;

import com.parcauto.dao.DocumentDAO;
import com.parcauto.dao.VehiculeDAO;
import com.parcauto.model.DocumentAdministratif;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;


public class DashboardController {

    @FXML private Label lblTotalVehicules;
    @FXML private Label lblDisponibles;
    @FXML private Label lblEnService;
    @FXML private Label lblEnEntretien;

    @FXML private VBox alertBox;
    @FXML private Label lblAlerteTitre;
    @FXML private Label lblAlerteDetail;

    @FXML private PieChart pieChartStatuts;
    @FXML private VBox documentsAlertBox;

    private final VehiculeDAO vehiculeDAO = new VehiculeDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();

    @FXML
    public void initialize() {
        chargerStatistiques();
        chargerAlertesDocuments();
    }

    private void chargerStatistiques() {
        try {
            int total = vehiculeDAO.compterTous();
            int disponibles = vehiculeDAO.compterParStatut("Disponible");
            int enService = vehiculeDAO.compterParStatut("En service");
            int enEntretien = vehiculeDAO.compterParStatut("En entretien");
            int horsService = vehiculeDAO.compterParStatut("Hors service");

            lblTotalVehicules.setText(String.valueOf(total));
            lblDisponibles.setText(String.valueOf(disponibles));
            lblEnService.setText(String.valueOf(enService));
            lblEnEntretien.setText(String.valueOf(enEntretien));

            pieChartStatuts.getData().clear();
            if (disponibles > 0) pieChartStatuts.getData().add(new PieChart.Data("Disponibles (" + disponibles + ")", disponibles));
            if (enService > 0) pieChartStatuts.getData().add(new PieChart.Data("En service (" + enService + ")", enService));
            if (enEntretien > 0) pieChartStatuts.getData().add(new PieChart.Data("En entretien (" + enEntretien + ")", enEntretien));
            if (horsService > 0) pieChartStatuts.getData().add(new PieChart.Data("Hors service (" + horsService + ")", horsService));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void chargerAlertesDocuments() {
        try {
            List<DocumentAdministratif> documents = documentDAO.getDocumentsAExpirer(30);
            documentsAlertBox.getChildren().clear();

            if (documents.isEmpty()) {
                Label vide = new Label("Aucun document à échéance proche. Tout est en règle ");
                vide.setStyle("-fx-text-fill: #686868; -fx-font-size: 14px;");
                documentsAlertBox.getChildren().add(vide);
                alertBox.setVisible(false);
                alertBox.setManaged(false);
                return;
            }

            long expires = documents.stream()
                    .filter(d -> "Expiré".equals(d.getStatutConformite()))
                    .count();

            alertBox.setVisible(true);
            alertBox.setManaged(true);
            lblAlerteTitre.setText(documents.size() + " document(s) à surveiller");
            lblAlerteDetail.setText(expires + " déjà expiré(s) — consultez l'onglet \"Documents administratifs\" pour les renouveler.");

            for (DocumentAdministratif doc : documents) {
                VBox ligne = new VBox(2);
                Label titre = new Label(doc.getImmatriculationVehicule() + " — " + doc.getTypeDocument());
                titre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #202020;");

                String statut = doc.getStatutConformite();
                Label detail = new Label("Expire le " + doc.getDateExpiration() + " (" + statut + ")");
                detail.getStyleClass().add("badge");
                detail.getStyleClass().add("Expiré".equals(statut) ? "badge-expire" : "badge-bientot-expire");

                ligne.getChildren().addAll(titre, detail);
                documentsAlertBox.getChildren().add(ligne);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
