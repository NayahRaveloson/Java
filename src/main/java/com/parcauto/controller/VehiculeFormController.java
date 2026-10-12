package com.parcauto.controller;

import com.parcauto.dao.VehiculeDAO;
import com.parcauto.model.Vehicule;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Year;
import java.util.regex.Pattern;


public class VehiculeFormController {

    private static final Pattern IMMAT_VALIDE = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9 \\-]{2,18}$");
    private static final Pattern NOM_VALIDE = Pattern.compile("^[A-Za-zÀ-ÖØ-öø-ÿ0-9][A-Za-zÀ-ÖØ-öø-ÿ0-9 .'\\-]{0,49}$");

    @FXML private Label lblTitreFormulaire;
    @FXML private TextField txtImmatriculation;
    @FXML private TextField txtMarque;
    @FXML private TextField txtModele;
    @FXML private ComboBox<String> cmbType;
    @FXML private TextField txtAnnee;
    @FXML private TextField txtKilometrage;
    @FXML private DatePicker datePickerAcquisition;
    @FXML private TextField txtPrixAchat;
    @FXML private Label lblErreur;

    private final VehiculeDAO vehiculeDAO = new VehiculeDAO();
    private Vehicule vehiculeEnCours;   
    private boolean sauvegarde = false;

    @FXML
    public void initialize() {
        cmbType.setItems(FXCollections.observableArrayList(
                "Berline", "Citadine", "SUV", "Utilitaire", "Fourgonnette", "Pick-up", "Camion", "Autre"));

        datePickerAcquisition.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean vide) {
                super.updateItem(date, vide);
                if (date != null && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f1f1f1; -fx-text-fill: #b0b0b0;");
                }
            }
        });
    }


    public void initialiser(Vehicule vehicule) {
        this.vehiculeEnCours = vehicule;
        if (vehicule == null) {
            lblTitreFormulaire.setText("Ajouter un véhicule");
            return;
        }

        lblTitreFormulaire.setText("Modifier le véhicule");
        txtImmatriculation.setText(vehicule.getImmatriculation());
        txtMarque.setText(vehicule.getMarque());
        txtModele.setText(vehicule.getModele());
        cmbType.getSelectionModel().select(vehicule.getTypeVehicule());
        txtAnnee.setText(vehicule.getAnnee() > 0 ? String.valueOf(vehicule.getAnnee()) : "");
        txtKilometrage.setText(String.valueOf(vehicule.getKilometrage()));
        txtPrixAchat.setText(vehicule.getPrixAchat() > 0 ? String.valueOf((long) vehicule.getPrixAchat()) : "");
        if (vehicule.getDateAcquisition() != null && !vehicule.getDateAcquisition().isBlank()) {
            try {
                datePickerAcquisition.setValue(LocalDate.parse(vehicule.getDateAcquisition()));
            } catch (Exception ignored) {

            }
        }
    }

    public boolean aEteSauvegarde() {
        return sauvegarde;
    }

    @FXML
    public void enregistrer() {
        String erreur = valider();
        if (erreur != null) {
            lblErreur.setText(erreur);
            return;
        }

        Vehicule v = (vehiculeEnCours == null) ? new Vehicule() : vehiculeEnCours;
        v.setImmatriculation(txtImmatriculation.getText().trim());
        v.setMarque(txtMarque.getText().trim());
        v.setModele(txtModele.getText().trim());
        v.setTypeVehicule(cmbType.getValue());
        v.setAnnee(txtAnnee.getText().isBlank() ? 0 : Integer.parseInt(txtAnnee.getText().trim()));
        v.setKilometrage(Integer.parseInt(txtKilometrage.getText().trim()));
        v.setDateAcquisition(datePickerAcquisition.getValue() != null ? datePickerAcquisition.getValue().toString() : null);
        v.setPrixAchat(txtPrixAchat.getText().isBlank() ? 0 : Double.parseDouble(txtPrixAchat.getText().trim()));

        if (vehiculeEnCours == null) {
            v.setStatut("Disponible");
        }

        try {
            if (vehiculeEnCours == null) {
                vehiculeDAO.ajouter(v);
            } else {
                vehiculeDAO.modifier(v);
            }
            sauvegarde = true;
            fermer();
        } catch (SQLException e) {
            if (e.getMessage() != null && (e.getMessage().toUpperCase().contains("DUPLICATE") || e.getMessage().contains("UNIQUE"))) {
                lblErreur.setText("Cette immatriculation existe déjà dans le parc.");
            } else {
                lblErreur.setText("Erreur base de données : " + e.getMessage());
            }
        }
    }


    private String valider() {
        if (txtImmatriculation.getText().isBlank()) return "L'immatriculation est obligatoire.";
        if (!IMMAT_VALIDE.matcher(txtImmatriculation.getText().trim()).matches()) {
            return "L'immatriculation ne doit contenir que des lettres, chiffres, espaces ou tirets (4 à 19 caractères).";
        }
        if (txtMarque.getText().isBlank()) return "La marque est obligatoire.";
        if (!NOM_VALIDE.matcher(txtMarque.getText().trim()).matches()) return "La marque contient des caractères invalides.";
        if (txtModele.getText().isBlank()) return "Le modèle est obligatoire.";
        if (!NOM_VALIDE.matcher(txtModele.getText().trim()).matches()) return "Le modèle contient des caractères invalides.";
        if (cmbType.getValue() == null) return "Veuillez choisir un type de véhicule.";

        if (!txtAnnee.getText().isBlank()) {
            try {
                int annee = Integer.parseInt(txtAnnee.getText().trim());
                if (annee < 1950 || annee > Year.now().getValue() + 1) {
                    return "L'année doit être comprise entre 1950 et " + (Year.now().getValue() + 1) + ".";
                }
            } catch (NumberFormatException e) {
                return "L'année doit être un nombre entier (ex : 2022).";
            }
        }

        if (txtKilometrage.getText().isBlank()) return "Le kilométrage est obligatoire.";
        try {
            int km = Integer.parseInt(txtKilometrage.getText().trim());
            if (km < 0) return "Le kilométrage ne peut pas être négatif.";
            if (vehiculeEnCours != null && km < vehiculeEnCours.getKilometrage()) {
                return "Le kilométrage ne peut pas être inférieur au kilométrage déjà enregistré (" +
                        vehiculeEnCours.getKilometrage() + " km).";
            }
        } catch (NumberFormatException e) {
            return "Le kilométrage doit être un nombre entier (ex : 45000).";
        }

        if (datePickerAcquisition.getValue() != null) {
            LocalDate date = datePickerAcquisition.getValue();
            if (date.isBefore(LocalDate.now())) {
                return "La date d'acquisition ne peut pas être une date déjà passée.";
            }
        }

        if (!txtPrixAchat.getText().isBlank()) {
            try {
                double prix = Double.parseDouble(txtPrixAchat.getText().trim());
                if (prix < 0) return "Le prix d'achat ne peut pas être négatif.";
            } catch (NumberFormatException e) {
                return "Le prix d'achat doit être un nombre (ex : 35000000).";
            }
        }

        return null;
    }

    @FXML
    public void annuler() {
        fermer();
    }

    private void fermer() {
        Stage stage = (Stage) txtImmatriculation.getScene().getWindow();
        stage.close();
    }
}
