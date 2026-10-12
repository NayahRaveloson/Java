package com.parcauto.controller;

import com.parcauto.dao.ConducteurDAO;
import com.parcauto.model.Conducteur;
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


public class ConducteurController {

    @FXML private TextField txtRecherche;
    @FXML private Label lblCompteur;

    @FXML private TableView<Conducteur> tableConducteurs;
    @FXML private TableColumn<Conducteur, String> colNom;
    @FXML private TableColumn<Conducteur, String> colPrenom;
    @FXML private TableColumn<Conducteur, String> colPermis;
    @FXML private TableColumn<Conducteur, String> colTelephone;
    @FXML private TableColumn<Conducteur, String> colDateEmbauche;

    private final ConducteurDAO conducteurDAO = new ConducteurDAO();
    private final ObservableList<Conducteur> donnees = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colPermis.setCellValueFactory(new PropertyValueFactory<>("numeroPermis"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colDateEmbauche.setCellValueFactory(new PropertyValueFactory<>("dateEmbauche"));

        tableConducteurs.setItems(donnees);
        txtRecherche.textProperty().addListener((obs, ancien, nouveau) -> rechercher());
        rechercher();
    }

    private void rechercher() {
        try {
            String motCle = txtRecherche.getText();
            var resultat = (motCle == null || motCle.isBlank())
                    ? conducteurDAO.getAll()
                    : conducteurDAO.rechercher(motCle.trim());
            donnees.setAll(resultat);
            lblCompteur.setText(resultat.size() + " conducteur(s)");
        } catch (SQLException e) {
            afficherErreur("Erreur lors du chargement des conducteurs.", e);
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
        Conducteur selection = tableConducteurs.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherAvertissement("Veuillez sélectionner un conducteur à modifier.");
            return;
        }
        ouvrirFormulaire(selection);
    }

    private void ouvrirFormulaire(Conducteur conducteurExistant) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/conducteur_form.fxml"));
            Parent racine = loader.load();

            ConducteurFormController controleur = loader.getController();
            controleur.initialiser(conducteurExistant);

            Stage dialogue = new Stage();
            dialogue.setTitle(conducteurExistant == null ? "Ajouter un conducteur" : "Modifier le conducteur");
            dialogue.initModality(Modality.APPLICATION_MODAL);
            dialogue.setScene(new javafx.scene.Scene(racine));
            dialogue.showAndWait();

            if (controleur.aEteSauvegarde()) {
                rechercher();
            }
        } catch (Exception e) {
            afficherErreur("Impossible d'ouvrir le formulaire conducteur.", e);
        }
    }

    @FXML
    public void supprimerConducteur() {
        Conducteur selection = tableConducteurs.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherAvertissement("Veuillez sélectionner un conducteur à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le conducteur " + selection.getNomComplet() + " ?\n" +
                "Ses affectations liées seront aussi supprimées.",
                ButtonType.YES, ButtonType.NO);
        confirmation.setHeaderText("Confirmation de suppression");
        confirmation.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.YES) {
                try {
                    conducteurDAO.supprimer(selection.getIdConducteur());
                    rechercher();
                } catch (SQLException e) {
                    afficherErreur("Impossible de supprimer ce conducteur.", e);
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
