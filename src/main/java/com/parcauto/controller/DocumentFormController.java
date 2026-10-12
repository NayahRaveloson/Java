package com.parcauto.controller;

import com.parcauto.dao.DocumentDAO;
import com.parcauto.dao.VehiculeDAO;
import com.parcauto.model.DocumentAdministratif;
import com.parcauto.model.Vehicule;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;


public class DocumentFormController {

    @FXML private Label lblTitreFormulaire;
    @FXML private ComboBox<Vehicule> cmbVehicule;
    @FXML private ComboBox<String> cmbType;
    @FXML private DatePicker datePickerDebut;
    @FXML private DatePicker datePickerExpiration;
    @FXML private TextField txtReference;
    @FXML private Label lblErreur;

    private final DocumentDAO documentDAO = new DocumentDAO();
    private final VehiculeDAO vehiculeDAO = new VehiculeDAO();

    private DocumentAdministratif documentEnCours;
    private boolean sauvegarde = false;

    @FXML
    public void initialize() {
        cmbType.setItems(FXCollections.observableArrayList(
                "Assurance", "Visite technique", "Vignette", "Carte grise", "Autre"));
        try {
            cmbVehicule.setItems(FXCollections.observableArrayList(vehiculeDAO.getAll()));
        } catch (SQLException e) {
            lblErreur.setText("Impossible de charger les véhicules : " + e.getMessage());
        }
    }

    public void initialiser(DocumentAdministratif document) {
        this.documentEnCours = document;
        if (document == null) {
            lblTitreFormulaire.setText("Ajouter un document");
            return;
        }

        lblTitreFormulaire.setText("Modifier le document");
        for (Vehicule v : cmbVehicule.getItems()) {
            if (v.getIdVehicule() == document.getIdVehicule()) {
                cmbVehicule.getSelectionModel().select(v);
                break;
            }
        }
        cmbType.getSelectionModel().select(document.getTypeDocument());
        if (document.getDateDebut() != null && !document.getDateDebut().isBlank()) {
            datePickerDebut.setValue(LocalDate.parse(document.getDateDebut()));
        }
        if (document.getDateExpiration() != null && !document.getDateExpiration().isBlank()) {
            datePickerExpiration.setValue(LocalDate.parse(document.getDateExpiration()));
        }
        txtReference.setText(document.getReference());
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

        DocumentAdministratif d = (documentEnCours == null) ? new DocumentAdministratif() : documentEnCours;
        d.setIdVehicule(cmbVehicule.getValue().getIdVehicule());
        d.setTypeDocument(cmbType.getValue());
        d.setDateDebut(datePickerDebut.getValue() != null ? datePickerDebut.getValue().toString() : null);
        d.setDateExpiration(datePickerExpiration.getValue().toString());
        d.setReference(txtReference.getText());

        try {
            if (documentEnCours == null) {
                documentDAO.ajouter(d);
            } else {
                documentDAO.modifier(d);
            }
            sauvegarde = true;
            fermer();
        } catch (SQLException e) {
            lblErreur.setText("Erreur base de données : " + e.getMessage());
        }
    }

    private String valider() {
        if (cmbVehicule.getValue() == null) return "Veuillez choisir un véhicule.";
        if (cmbType.getValue() == null) return "Veuillez choisir un type de document.";
        if (datePickerExpiration.getValue() == null) return "La date d'expiration est obligatoire.";
        if (datePickerDebut.getValue() != null && datePickerDebut.getValue().isAfter(datePickerExpiration.getValue())) {
            return "La date de début doit être antérieure à la date d'expiration.";
        }

        java.time.LocalDate borneMin = java.time.LocalDate.of(1950, 1, 1);
        java.time.LocalDate borneMax = java.time.LocalDate.now().plusYears(20);
        if (datePickerExpiration.getValue().isBefore(borneMin) || datePickerExpiration.getValue().isAfter(borneMax)) {
            return "La date d'expiration semble incorrecte (doit être entre 1950 et " + borneMax.getYear() + ").";
        }
        if (datePickerDebut.getValue() != null &&
                (datePickerDebut.getValue().isBefore(borneMin) || datePickerDebut.getValue().isAfter(borneMax))) {
            return "La date de début semble incorrecte (doit être entre 1950 et " + borneMax.getYear() + ").";
        }

        return null;
    }

    @FXML
    public void annuler() {
        fermer();
    }

    private void fermer() {
        Stage stage = (Stage) cmbVehicule.getScene().getWindow();
        stage.close();
    }
}
