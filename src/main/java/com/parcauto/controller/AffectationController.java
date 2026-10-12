package com.parcauto.controller;

import com.parcauto.dao.AffectationDAO;
import com.parcauto.dao.VehiculeDAO;
import com.parcauto.model.Affectation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;


public class AffectationController {

    @FXML private TextField txtRecherche;
    @FXML private Label lblCompteur;

    @FXML private TableView<Affectation> tableAffectations;
    @FXML private TableColumn<Affectation, String> colVehicule;
    @FXML private TableColumn<Affectation, String> colConducteur;
    @FXML private TableColumn<Affectation, String> colDateDebut;
    @FXML private TableColumn<Affectation, String> colDateFin;
    @FXML private TableColumn<Affectation, String> colStatutAffectation;
    @FXML private TableColumn<Affectation, String> colRemarque;

    private final AffectationDAO affectationDAO = new AffectationDAO();
    private final VehiculeDAO vehiculeDAO = new VehiculeDAO();
    private final ObservableList<Affectation> donnees = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colVehicule.setCellValueFactory(new PropertyValueFactory<>("immatriculationVehicule"));
        colConducteur.setCellValueFactory(new PropertyValueFactory<>("nomConducteur"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colRemarque.setCellValueFactory(new PropertyValueFactory<>("remarque"));

        colStatutAffectation.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().estEnCours() ? "En cours" : "Terminée"));
        colStatutAffectation.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean vide) {
                super.updateItem(statut, vide);
                if (vide || statut == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(statut);
                badge.getStyleClass().add("badge");
                badge.getStyleClass().add("En cours".equals(statut) ? "badge-service" : "badge-disponible");
                setGraphic(badge);
            }
        });

        tableAffectations.setItems(donnees);
        txtRecherche.textProperty().addListener((obs, ancien, nouveau) -> rechercher());
        rechercher();
    }

    private void rechercher() {
        try {
            String motCle = txtRecherche.getText();
            var resultat = (motCle == null || motCle.isBlank())
                    ? affectationDAO.getAll()
                    : affectationDAO.rechercher(motCle.trim());
            donnees.setAll(resultat);
            lblCompteur.setText(resultat.size() + " affectation(s)");
        } catch (SQLException e) {
            afficherErreur("Erreur lors du chargement des affectations.", e);
        }
    }

    @FXML
    public void effacerRecherche() {
        txtRecherche.clear();
        rechercher();
    }

    @FXML
    public void ouvrirFormulaireAjout() {
        ouvrirFormulaire(null);
    }

    @FXML
    public void ouvrirFormulaireModification() {
        Affectation selection = tableAffectations.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherAvertissement("Veuillez sélectionner une affectation à modifier.");
            return;
        }
        ouvrirFormulaire(selection);
    }

    private void ouvrirFormulaire(Affectation affectationExistante) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/affectation_form.fxml"));
            Parent racine = loader.load();

            AffectationFormController controleur = loader.getController();
            controleur.initialiser(affectationExistante);

            Stage dialogue = new Stage();
            dialogue.setTitle(affectationExistante == null ? "Nouvelle affectation" : "Modifier l'affectation");
            dialogue.initModality(Modality.APPLICATION_MODAL);
            dialogue.setScene(new javafx.scene.Scene(racine));
            dialogue.showAndWait();

            if (controleur.aEteSauvegarde()) {
                rechercher();
            }
        } catch (Exception e) {
            afficherErreur("Impossible d'ouvrir le formulaire d'affectation.", e);
        }
    }

    @FXML
    public void supprimerAffectation() {
        Affectation selection = tableAffectations.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherAvertissement("Veuillez sélectionner une affectation à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cette affectation (" + selection.getImmatriculationVehicule() +
                " ↔ " + selection.getNomConducteur() + ") ?",
                ButtonType.YES, ButtonType.NO);
        confirmation.setHeaderText("Confirmation de suppression");
        confirmation.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.YES) {
                try {
                    boolean etaitActive = selection.estEnCours();
                    affectationDAO.supprimer(selection.getIdAffectation());
                    if (etaitActive) {
                        vehiculeDAO.modifierStatut(selection.getIdVehicule(), "Disponible");
                    }
                    rechercher();
                } catch (SQLException e) {
                    afficherErreur("Impossible de supprimer cette affectation.", e);
                }
            }
        });
    }

    private void afficherAvertissement(String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }

    private void afficherErreur(String message, Exception e) {
        new Alert(Alert.AlertType.ERROR, message + "\n" + e.getMessage(), ButtonType.OK).showAndWait();
    }
}
