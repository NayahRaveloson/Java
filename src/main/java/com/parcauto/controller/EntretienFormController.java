package com.parcauto.controller;

import com.parcauto.dao.EntretienDAO;
import com.parcauto.dao.VehiculeDAO;
import com.parcauto.model.Entretien;
import com.parcauto.model.Vehicule;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;


public class EntretienFormController {

    @FXML private Label lblTitreFormulaire;
    @FXML private ComboBox<Vehicule> cmbVehicule;
    @FXML private ComboBox<String> cmbType;
    @FXML private DatePicker datePickerDate;
    @FXML private TextField txtKilometrage;
    @FXML private TextField txtCout;
    @FXML private TextArea txtDescription;
    @FXML private Label lblErreur;

    private final EntretienDAO entretienDAO = new EntretienDAO();
    private final VehiculeDAO vehiculeDAO = new VehiculeDAO();

    private Entretien entretienEnCours;
    private boolean sauvegarde = false;

    @FXML
    public void initialize() {
        cmbType.setItems(FXCollections.observableArrayList(
                "Vidange", "Révision", "Réparation", "Changement de pneus", "Contrôle technique", "Autre"));
        try {
            cmbVehicule.setItems(FXCollections.observableArrayList(vehiculeDAO.getAll()));
        } catch (SQLException e) {
            lblErreur.setText("Impossible de charger les véhicules : " + e.getMessage());
        }

        datePickerDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean vide) {
                super.updateItem(date, vide);
                if (!vide && date != null && date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f1f1f1; -fx-text-fill: #b0b0b0;");
                }
            }
        });
    }

    public void initialiser(Entretien entretien) {
        this.entretienEnCours = entretien;
        if (entretien == null) {
            lblTitreFormulaire.setText("Ajouter un entretien");
            return;
        }

        lblTitreFormulaire.setText("Modifier l'entretien");
        for (Vehicule v : cmbVehicule.getItems()) {
            if (v.getIdVehicule() == entretien.getIdVehicule()) {
                cmbVehicule.getSelectionModel().select(v);
                break;
            }
        }
        cmbType.getSelectionModel().select(entretien.getTypeEntretien());
        if (entretien.getDateEntretien() != null && !entretien.getDateEntretien().isBlank()) {
            datePickerDate.setValue(LocalDate.parse(entretien.getDateEntretien()));
        }
        txtKilometrage.setText(entretien.getKilometrage() > 0 ? String.valueOf(entretien.getKilometrage()) : "");
        txtCout.setText(String.valueOf((long) entretien.getCout()));
        txtDescription.setText(entretien.getDescription());
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

        Entretien e = (entretienEnCours == null) ? new Entretien() : entretienEnCours;
        e.setIdVehicule(cmbVehicule.getValue().getIdVehicule());
        e.setTypeEntretien(cmbType.getValue());
        e.setDateEntretien(datePickerDate.getValue().toString());
        e.setKilometrage(txtKilometrage.getText().isBlank() ? 0 : Integer.parseInt(txtKilometrage.getText().trim()));
        e.setCout(Double.parseDouble(txtCout.getText().trim()));
        e.setDescription(txtDescription.getText());

        try {
            if (entretienEnCours == null) {
                entretienDAO.ajouter(e);
            } else {
                entretienDAO.modifier(e);
            }
            if (e.getKilometrage() > 0) {
                vehiculeDAO.synchroniserKilometrageSiSuperieur(e.getIdVehicule(), e.getKilometrage());
            }
            sauvegarde = true;
            fermer();
        } catch (SQLException ex) {
            lblErreur.setText("Erreur base de données : " + ex.getMessage());
        }
    }

    private String valider() {
        if (cmbVehicule.getValue() == null) return "Veuillez choisir un véhicule.";
        if (cmbType.getValue() == null) return "Veuillez choisir un type d'entretien.";
        if (datePickerDate.getValue() == null) return "La date est obligatoire.";

        if (datePickerDate.getValue().isAfter(java.time.LocalDate.now())) {
            return "La date de l'entretien ne peut pas être dans le futur.";
        }

        if (!txtKilometrage.getText().isBlank()) {
            try {
                int km = Integer.parseInt(txtKilometrage.getText().trim());
                if (km < 0) return "Le kilométrage ne peut pas être négatif.";
                Vehicule vehiculeChoisi = cmbVehicule.getValue();
                if (vehiculeChoisi != null && km < vehiculeChoisi.getKilometrage()) {
                    return "Le kilométrage saisi (" + km + " km) est inférieur au kilométrage actuel du véhicule (" +
                            vehiculeChoisi.getKilometrage() + " km). Vérifiez la valeur.";
                }
            } catch (NumberFormatException ex) {
                return "Le kilométrage doit être un nombre entier.";
            }
        }

        if (txtCout.getText().isBlank()) return "Le coût est obligatoire.";
        try {
            if (Double.parseDouble(txtCout.getText().trim()) < 0) return "Le coût ne peut pas être négatif.";
        } catch (NumberFormatException ex) {
            return "Le coût doit être un nombre (ex : 250000).";
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
