package com.parcauto.controller;

import com.parcauto.dao.VehiculeDAO;
import com.parcauto.model.Vehicule;
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
import java.util.List;


public class VehiculeController {

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> cmbFiltreStatut;
    @FXML private Label lblCompteur;

    @FXML private TableView<Vehicule> tableVehicules;
    @FXML private TableColumn<Vehicule, String> colImmat;
    @FXML private TableColumn<Vehicule, String> colMarque;
    @FXML private TableColumn<Vehicule, String> colModele;
    @FXML private TableColumn<Vehicule, String> colType;
    @FXML private TableColumn<Vehicule, Integer> colAnnee;
    @FXML private TableColumn<Vehicule, Integer> colKm;
    @FXML private TableColumn<Vehicule, String> colStatut;

    private final VehiculeDAO vehiculeDAO = new VehiculeDAO();
    private final ObservableList<Vehicule> donnees = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurerColonnes();
        configurerFiltreStatut();
        configurerRecherche();
        chargerDonnees();
    }

    private void configurerColonnes() {
        colImmat.setCellValueFactory(new PropertyValueFactory<>("immatriculation"));
        colMarque.setCellValueFactory(new PropertyValueFactory<>("marque"));
        colModele.setCellValueFactory(new PropertyValueFactory<>("modele"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeVehicule"));
        colAnnee.setCellValueFactory(new PropertyValueFactory<>("annee"));
        colKm.setCellValueFactory(new PropertyValueFactory<>("kilometrage"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colStatut.setCellFactory(col -> new TableCell<>() {
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
                    case "Disponible" -> badge.getStyleClass().add("badge-disponible");
                    case "En service" -> badge.getStyleClass().add("badge-service");
                    case "En entretien" -> badge.getStyleClass().add("badge-entretien");
                    case "Hors service" -> badge.getStyleClass().add("badge-hors-service");
                    default -> {}
                }
                setGraphic(badge);
            }
        });

        tableVehicules.setItems(donnees);
    }

    private void configurerFiltreStatut() {
        cmbFiltreStatut.setItems(FXCollections.observableArrayList(
                "Tous les statuts", "Disponible", "En service", "En entretien", "Hors service"));
        cmbFiltreStatut.getSelectionModel().selectFirst();
        cmbFiltreStatut.setOnAction(e -> appliquerFiltres());
    }

    private void configurerRecherche() {
        txtRecherche.textProperty().addListener((obs, ancien, nouveau) -> appliquerFiltres());
    }

    private void appliquerFiltres() {
        try {
            String motCle = txtRecherche.getText();
            String statutChoisi = cmbFiltreStatut.getValue();

            List<Vehicule> resultat;
            if (motCle != null && !motCle.isBlank()) {
                resultat = vehiculeDAO.rechercher(motCle.trim());
            } else if (statutChoisi != null && !statutChoisi.equals("Tous les statuts")) {
                resultat = vehiculeDAO.filtrerParStatut(statutChoisi);
            } else {
                resultat = vehiculeDAO.getAll();
            }

            donnees.setAll(resultat);
            lblCompteur.setText(resultat.size() + " véhicule(s)");
        } catch (SQLException e) {
            afficherErreur("Erreur lors du chargement des véhicules.", e);
        }
    }

    private void chargerDonnees() {
        appliquerFiltres();
    }

    @FXML
    public void effacerRecherche() {
        txtRecherche.clear();
        cmbFiltreStatut.getSelectionModel().selectFirst();
        appliquerFiltres();
    }

    @FXML
    public void ouvrirFormulaireAjout() {
        ouvrirFormulaire(null);
    }

    @FXML
    public void ouvrirFormulaireModification() {
        Vehicule selection = tableVehicules.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherAvertissement("Veuillez sélectionner un véhicule à modifier");
            return;
        }
        ouvrirFormulaire(selection);
    }

    private void ouvrirFormulaire(Vehicule vehiculeExistant) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/vehicule_form.fxml"));
            Parent racine = loader.load();

            VehiculeFormController controleurFormulaire = loader.getController();
            controleurFormulaire.initialiser(vehiculeExistant);

            Stage dialogue = new Stage();
            dialogue.setTitle(vehiculeExistant == null ? "Ajouter un véhicule" : "Modifier le véhicule");
            dialogue.initModality(Modality.APPLICATION_MODAL);
            dialogue.setScene(new javafx.scene.Scene(racine));
            dialogue.showAndWait();

            if (controleurFormulaire.aEteSauvegarde()) {
                appliquerFiltres();
            }
        } catch (Exception e) {
            afficherErreur("Impossible d'ouvrir le formulaire véhicule", e);
        }
    }

    @FXML
    public void supprimerVehicule() {
        Vehicule selection = tableVehicules.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherAvertissement("Veuillez sélectionner un véhicule à supprimer");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le véhicule " + selection.getImmatriculation() + " ?\n" +
                "Ses affectations, entretiens, pleins et documents liés seront aussi supprimés",
                ButtonType.YES, ButtonType.NO);
        confirmation.setHeaderText("Confirmation de suppression");
        confirmation.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.YES) {
                try {
                    vehiculeDAO.supprimer(selection.getIdVehicule());
                    appliquerFiltres();
                } catch (SQLException e) {
                    afficherErreur("Impossible de supprimer ce véhicule", e);
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
