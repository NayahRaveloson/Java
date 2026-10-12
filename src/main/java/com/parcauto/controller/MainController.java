package com.parcauto.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;


public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label lblTitreEcran;
    @FXML private Label lblSousTitreEcran;

    @FXML private Button btnDashboard;
    @FXML private Button btnVehicules;
    @FXML private Button btnConducteurs;
    @FXML private Button btnAffectations;
    @FXML private Button btnEntretiens;
    @FXML private Button btnDocuments;

    @FXML
    public void initialize() {
        afficherDashboard();
    }

    @FXML
    public void afficherDashboard() {
        chargerEcran("/fxml/dashboard.fxml", btnDashboard,
                "Tableau de bord", "Vue d'ensemble du parc automobile");
    }

    @FXML
    public void afficherVehicules() {
        chargerEcran("/fxml/vehicule_list.fxml", btnVehicules,
                "Véhicules", "Gestion du parc de véhicules");
    }

    @FXML
    public void afficherConducteurs() {
        chargerEcran("/fxml/conducteur_list.fxml", btnConducteurs,
                "Conducteurs", "Gestion des conducteurs habilités");
    }

    @FXML
    public void afficherAffectations() {
        chargerEcran("/fxml/affectation_list.fxml", btnAffectations,
                "Affectations", "Attribution des véhicules aux conducteurs");
    }

    @FXML
    public void afficherEntretiens() {
        chargerEcran("/fxml/entretien_carburant.fxml", btnEntretiens,
                "Entretiens & Carburant", "Historique de maintenance et de consommation");
    }

    @FXML
    public void afficherDocuments() {
        chargerEcran("/fxml/document_list.fxml", btnDocuments,
                "Documents administratifs", "Suivi des échéances (assurance, contrôle technique...)");
    }


    private void chargerEcran(String cheminFxml, Button boutonActif, String titre, String sousTitre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(cheminFxml));
            Node ecran = loader.load();
            contentArea.getChildren().setAll(ecran);

            lblTitreEcran.setText(titre);
            lblSousTitreEcran.setText(sousTitre);

            for (Button b : new Button[]{btnDashboard, btnVehicules, btnConducteurs,
                    btnAffectations, btnEntretiens, btnDocuments}) {
                b.getStyleClass().remove("nav-button-active");
            }
            boutonActif.getStyleClass().add("nav-button-active");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
