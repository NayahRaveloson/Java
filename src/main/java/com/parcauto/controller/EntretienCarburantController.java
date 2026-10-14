package com.parcauto.controller;

import com.parcauto.dao.CarburantDAO;
import com.parcauto.dao.AffectationDAO;
import com.parcauto.dao.EntretienDAO;
import com.parcauto.dao.VehiculeDAO;
import com.parcauto.model.Carburant;
import com.parcauto.model.Entretien;
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


public class EntretienCarburantController {

    @FXML private Label lblTotalEntretiens;
    @FXML private Label lblTotalCarburant;
    @FXML private Label lblTotalGlobal;

    @FXML private TextField txtRechercheEntretien;
    @FXML private TableView<Entretien> tableEntretiens;
    @FXML private TableColumn<Entretien, String> colEntVehicule;
    @FXML private TableColumn<Entretien, String> colEntType;
    @FXML private TableColumn<Entretien, String> colEntEtat;
    @FXML private TableColumn<Entretien, String> colEntDate;
    @FXML private TableColumn<Entretien, Integer> colEntKm;
    @FXML private TableColumn<Entretien, Double> colEntCout;
    @FXML private TableColumn<Entretien, String> colEntDescription;

    @FXML private TextField txtRechercheCarburant;
    @FXML private TableView<Carburant> tableCarburant;
    @FXML private TableColumn<Carburant, String> colCarVehicule;
    @FXML private TableColumn<Carburant, String> colCarDate;
    @FXML private TableColumn<Carburant, Double> colCarQuantite;
    @FXML private TableColumn<Carburant, Double> colCarCout;
    @FXML private TableColumn<Carburant, Integer> colCarKm;

    private final EntretienDAO entretienDAO = new EntretienDAO();
    private final CarburantDAO carburantDAO = new CarburantDAO();
    private final VehiculeDAO vehiculeDAO = new VehiculeDAO();
    private final AffectationDAO affectationDAO = new AffectationDAO();

    private final ObservableList<Entretien> donneesEntretiens = FXCollections.observableArrayList();
    private final ObservableList<Carburant> donneesCarburant = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurerColonnesEntretiens();
        configurerColonnesCarburant();

        txtRechercheEntretien.textProperty().addListener((o, a, n) -> rechercherEntretiens());
        txtRechercheCarburant.textProperty().addListener((o, a, n) -> rechercherCarburant());

        rechercherEntretiens();
        rechercherCarburant();
        rafraichirTotaux();
    }

    private void configurerColonnesEntretiens() {
        colEntVehicule.setCellValueFactory(new PropertyValueFactory<>("immatriculationVehicule"));
        colEntType.setCellValueFactory(new PropertyValueFactory<>("typeEntretien"));
        colEntEtat.setCellValueFactory(new PropertyValueFactory<>("etatIntervention"));
        colEntDate.setCellValueFactory(new PropertyValueFactory<>("dateEntretien"));
        colEntKm.setCellValueFactory(new PropertyValueFactory<>("kilometrage"));
        colEntCout.setCellValueFactory(new PropertyValueFactory<>("cout"));
        colEntDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        tableEntretiens.setItems(donneesEntretiens);
    }

    private void configurerColonnesCarburant() {
        colCarVehicule.setCellValueFactory(new PropertyValueFactory<>("immatriculationVehicule"));
        colCarDate.setCellValueFactory(new PropertyValueFactory<>("datePlein"));
        colCarQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteLitres"));
        colCarCout.setCellValueFactory(new PropertyValueFactory<>("cout"));
        colCarKm.setCellValueFactory(new PropertyValueFactory<>("kilometrage"));
        tableCarburant.setItems(donneesCarburant);
    }


    private void rechercherEntretiens() {
        try {
            String motCle = txtRechercheEntretien.getText();
            var resultat = (motCle == null || motCle.isBlank())
                    ? entretienDAO.getAll()
                    : entretienDAO.rechercher(motCle.trim());
            donneesEntretiens.setAll(resultat);
        } catch (SQLException e) {
            afficherErreur("Erreur lors du chargement des entretiens", e);
        }
    }

    @FXML
    public void effacerRechercheEntretien() {
        txtRechercheEntretien.clear();
        rechercherEntretiens();
    }

    @FXML
    public void ouvrirFormulaireAjoutEntretien() {
        ouvrirFormulaireEntretien(null);
    }

    @FXML
    public void ouvrirFormulaireModificationEntretien() {
        Entretien selection = tableEntretiens.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherAvertissement("Veuillez sélectionner un entretien à modifier");
            return;
        }
        ouvrirFormulaireEntretien(selection);
    }

    private void ouvrirFormulaireEntretien(Entretien entretienExistant) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/entretien_form.fxml"));
            Parent racine = loader.load();

            EntretienFormController controleur = loader.getController();
            controleur.initialiser(entretienExistant);

            Stage dialogue = new Stage();
            dialogue.setTitle(entretienExistant == null ? "Ajouter un entretien" : "Modifier l'entretien");
            dialogue.initModality(Modality.APPLICATION_MODAL);
            dialogue.setScene(new javafx.scene.Scene(racine));
            dialogue.showAndWait();

            if (controleur.aEteSauvegarde()) {
                rechercherEntretiens();
                rafraichirTotaux();
            }
        } catch (Exception e) {
            afficherErreur("Impossible d'ouvrir le formulaire entretien", e);
        }
    }

    @FXML
    public void supprimerEntretien() {
        Entretien selection = tableEntretiens.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherAvertissement("Veuillez sélectionner un entretien à supprimer");
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cet entretien (" + selection.getImmatriculationVehicule() + " - " + selection.getTypeEntretien() + ") ?",
                ButtonType.YES, ButtonType.NO);
        confirmation.setHeaderText("Confirmation de suppression");
        confirmation.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.YES) {
                try {
                    boolean reparationEnCours = "Réparation en cours".equals(selection.getEtatIntervention());
                    int idVehicule = selection.getIdVehicule();
                    entretienDAO.supprimer(selection.getIdEntretien());
                    if (reparationEnCours
                            && entretienDAO.compterReparationsEnCours(idVehicule) == 0
                            && !affectationDAO.vehiculeAUneMissionActive(idVehicule)) {
                        vehiculeDAO.libererApresReparation(idVehicule);
                    }
                    rechercherEntretiens();
                    rafraichirTotaux();
                } catch (SQLException e) {
                    afficherErreur("Impossible de supprimer cet entretien", e);
                }
            }
        });
    }


    private void rechercherCarburant() {
        try {
            String motCle = txtRechercheCarburant.getText();
            var resultat = (motCle == null || motCle.isBlank())
                    ? carburantDAO.getAll()
                    : carburantDAO.rechercher(motCle.trim());
            donneesCarburant.setAll(resultat);
        } catch (SQLException e) {
            afficherErreur("Erreur lors du chargement des pleins de carburant.", e);
        }
    }

    @FXML
    public void effacerRechercheCarburant() {
        txtRechercheCarburant.clear();
        rechercherCarburant();
    }

    @FXML
    public void ouvrirFormulaireAjoutCarburant() {
        ouvrirFormulaireCarburant(null);
    }

    @FXML
    public void ouvrirFormulaireModificationCarburant() {
        Carburant selection = tableCarburant.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherAvertissement("Veuillez sélectionner un plein à modifier.");
            return;
        }
        ouvrirFormulaireCarburant(selection);
    }

    private void ouvrirFormulaireCarburant(Carburant carburantExistant) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/carburant_form.fxml"));
            Parent racine = loader.load();

            CarburantFormController controleur = loader.getController();
            controleur.initialiser(carburantExistant);

            Stage dialogue = new Stage();
            dialogue.setTitle(carburantExistant == null ? "Ajouter un plein" : "Modifier le plein");
            dialogue.initModality(Modality.APPLICATION_MODAL);
            dialogue.setScene(new javafx.scene.Scene(racine));
            dialogue.showAndWait();

            if (controleur.aEteSauvegarde()) {
                rechercherCarburant();
                rafraichirTotaux();
            }
        } catch (Exception e) {
            afficherErreur("Impossible d'ouvrir le formulaire carburant.", e);
        }
    }

    @FXML
    public void supprimerCarburant() {
        Carburant selection = tableCarburant.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherAvertissement("Veuillez sélectionner un plein à supprimer.");
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer ce plein de carburant (" + selection.getImmatriculationVehicule() + ") ?",
                ButtonType.YES, ButtonType.NO);
        confirmation.setHeaderText("Confirmation de suppression");
        confirmation.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.YES) {
                try {
                    carburantDAO.supprimer(selection.getIdCarburant());
                    rechercherCarburant();
                    rafraichirTotaux();
                } catch (SQLException e) {
                    afficherErreur("Impossible de supprimer ce plein.", e);
                }
            }
        });
    }


    private void rafraichirTotaux() {
        try {
            double totalEntretiens = entretienDAO.sommeCoutsTotaux();
            double totalCarburant = carburantDAO.sommeCoutsTotaux();
            lblTotalEntretiens.setText(formaterMontant(totalEntretiens));
            lblTotalCarburant.setText(formaterMontant(totalCarburant));
            lblTotalGlobal.setText(formaterMontant(totalEntretiens + totalCarburant));
        } catch (SQLException e) {
            afficherErreur("Erreur lors du calcul des totaux.", e);
        }
    }

    private String formaterMontant(double montant) {
        return String.format("%,.0f Ar", montant).replace(',', ' ');
    }

    private void afficherAvertissement(String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }

    private void afficherErreur(String message, Exception e) {
        new Alert(Alert.AlertType.ERROR, message + "\n" + e.getMessage(), ButtonType.OK).showAndWait();
    }
}
