package com.parcauto.controller;

import com.parcauto.dao.DocumentDAO;
import com.parcauto.model.DocumentAdministratif;
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


public class DocumentController {

    @FXML private TextField txtRecherche;
    @FXML private Label lblCompteur;

    @FXML private TableView<DocumentAdministratif> tableDocuments;
    @FXML private TableColumn<DocumentAdministratif, String> colVehicule;
    @FXML private TableColumn<DocumentAdministratif, String> colType;
    @FXML private TableColumn<DocumentAdministratif, String> colDateDebut;
    @FXML private TableColumn<DocumentAdministratif, String> colDateExpiration;
    @FXML private TableColumn<DocumentAdministratif, String> colReference;
    @FXML private TableColumn<DocumentAdministratif, String> colStatutConformite;

    private final DocumentDAO documentDAO = new DocumentDAO();
    private final ObservableList<DocumentAdministratif> donnees = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colVehicule.setCellValueFactory(new PropertyValueFactory<>("immatriculationVehicule"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeDocument"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateExpiration.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
        colReference.setCellValueFactory(new PropertyValueFactory<>("reference"));

        colStatutConformite.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStatutConformite()));
        colStatutConformite.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean vide) {
                super.updateItem(statut, vide);
                if (vide || statut == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(statut);
                badge.getStyleClass().add("badge");
                switch (statut) {
                    case "Valide" -> badge.getStyleClass().add("badge-valide");
                    case "Bientôt expiré" -> badge.getStyleClass().add("badge-bientot-expire");
                    case "Expiré" -> badge.getStyleClass().add("badge-expire");
                    default -> {}
                }
                setGraphic(badge);
            }
        });

        tableDocuments.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(DocumentAdministratif document, boolean vide) {
                super.updateItem(document, vide);
                getStyleClass().removeAll("row-expire", "row-bientot");
                if (!vide && document != null) {
                    String statut = document.getStatutConformite();
                    if ("Expiré".equals(statut)) {
                        getStyleClass().add("row-expire");
                    } else if ("Bientôt expiré".equals(statut)) {
                        getStyleClass().add("row-bientot");
                    }
                }
            }
        });

        tableDocuments.setItems(donnees);
        txtRecherche.textProperty().addListener((obs, ancien, nouveau) -> rechercher());
        rechercher();
    }

    private void rechercher() {
        try {
            String motCle = txtRecherche.getText();
            var resultat = (motCle == null || motCle.isBlank())
                    ? documentDAO.getAll()
                    : documentDAO.rechercher(motCle.trim());
            donnees.setAll(resultat);
            lblCompteur.setText(resultat.size() + " document(s)");
        } catch (SQLException e) {
            afficherErreur("Erreur lors du chargement des documents.", e);
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
        DocumentAdministratif selection = tableDocuments.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherAvertissement("Veuillez sélectionner un document à modifier.");
            return;
        }
        ouvrirFormulaire(selection);
    }

    private void ouvrirFormulaire(DocumentAdministratif documentExistant) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/document_form.fxml"));
            Parent racine = loader.load();

            DocumentFormController controleur = loader.getController();
            controleur.initialiser(documentExistant);

            Stage dialogue = new Stage();
            dialogue.setTitle(documentExistant == null ? "Ajouter un document" : "Modifier le document");
            dialogue.initModality(Modality.APPLICATION_MODAL);
            dialogue.setScene(new javafx.scene.Scene(racine));
            dialogue.showAndWait();

            if (controleur.aEteSauvegarde()) {
                rechercher();
            }
        } catch (Exception e) {
            afficherErreur("Impossible d'ouvrir le formulaire document.", e);
        }
    }

    @FXML
    public void supprimerDocument() {
        DocumentAdministratif selection = tableDocuments.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherAvertissement("Veuillez sélectionner un document à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer ce document (" + selection.getImmatriculationVehicule() + " - " + selection.getTypeDocument() + ") ?",
                ButtonType.YES, ButtonType.NO);
        confirmation.setHeaderText("Confirmation de suppression");
        confirmation.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.YES) {
                try {
                    documentDAO.supprimer(selection.getIdDocument());
                    rechercher();
                } catch (SQLException e) {
                    afficherErreur("Impossible de supprimer ce document.", e);
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
