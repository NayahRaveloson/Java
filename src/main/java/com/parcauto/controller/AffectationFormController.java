package com.parcauto.controller;

import com.parcauto.dao.AffectationDAO;
import com.parcauto.dao.ConducteurDAO;
import com.parcauto.dao.VehiculeDAO;
import com.parcauto.model.Affectation;
import com.parcauto.model.Conducteur;
import com.parcauto.model.Vehicule;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;


public class AffectationFormController {

    @FXML private Label lblTitreFormulaire;
    @FXML private ComboBox<Vehicule> cmbVehicule;
    @FXML private ComboBox<Conducteur> cmbConducteur;
    @FXML private DatePicker datePickerDebut;
    @FXML private DatePicker datePickerFin;
    @FXML private TextArea txtRemarque;
    @FXML private Label lblErreur;

    private final AffectationDAO affectationDAO = new AffectationDAO();
    private final VehiculeDAO vehiculeDAO = new VehiculeDAO();
    private final ConducteurDAO conducteurDAO = new ConducteurDAO();

    private Affectation affectationEnCours; 
    private boolean etaitActiveAvantModification = false; 
    private int idVehiculeAvantModification = -1;          
    private boolean sauvegarde = false;
    private boolean bloquerDatesPasseesDebut = true;

    @FXML
    public void initialize() {
        try {
            cmbVehicule.setItems(FXCollections.observableArrayList(vehiculeDAO.getAll()));
            cmbConducteur.setItems(FXCollections.observableArrayList(conducteurDAO.getAll()));
        } catch (SQLException e) {
            lblErreur.setText("Impossible de charger les véhicules/conducteurs : " + e.getMessage());
        }

        datePickerDebut.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean vide) {
                super.updateItem(date, vide);
                if (!vide && date != null && bloquerDatesPasseesDebut && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f1f1f1; -fx-text-fill: #b0b0b0;");
                }
            }
        });

       datePickerFin.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean vide) {
                super.updateItem(date, vide);
                LocalDate debutChoisi = datePickerDebut.getValue();
                if (!vide && date != null && debutChoisi != null && date.isBefore(debutChoisi)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f1f1f1; -fx-text-fill: #b0b0b0;");
                }
            }
        });
    }

    public void initialiser(Affectation affectation) {
        this.affectationEnCours = affectation;
        this.bloquerDatesPasseesDebut = (affectation == null);
        if (affectation == null) {
            lblTitreFormulaire.setText("Nouvelle affectation");
            return;
        }

        lblTitreFormulaire.setText("Modifier l'affectation");
        etaitActiveAvantModification = affectation.estEnCours();
        idVehiculeAvantModification = affectation.getIdVehicule();

        selectionnerParId(cmbVehicule, affectation.getIdVehicule(), Vehicule::getIdVehicule);
        selectionnerParId(cmbConducteur, affectation.getIdConducteur(), Conducteur::getIdConducteur);

        if (affectation.getDateDebut() != null && !affectation.getDateDebut().isBlank()) {
            datePickerDebut.setValue(LocalDate.parse(affectation.getDateDebut()));
        }
        if (affectation.getDateFin() != null && !affectation.getDateFin().isBlank()) {
            datePickerFin.setValue(LocalDate.parse(affectation.getDateFin()));
        }
        txtRemarque.setText(affectation.getRemarque());
    }

   private <T> void selectionnerParId(ComboBox<T> combo, int idRecherche, java.util.function.ToIntFunction<T> extracteurId) {
        for (T item : combo.getItems()) {
            if (extracteurId.applyAsInt(item) == idRecherche) {
                combo.getSelectionModel().select(item);
                break;
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

        Vehicule vehiculeChoisi = cmbVehicule.getValue();
        boolean seraActive = (datePickerFin.getValue() == null);

        Affectation a = (affectationEnCours == null) ? new Affectation() : affectationEnCours;
        a.setIdVehicule(vehiculeChoisi.getIdVehicule());
        a.setIdConducteur(cmbConducteur.getValue().getIdConducteur());
        a.setDateDebut(datePickerDebut.getValue().toString());
        a.setDateFin(datePickerFin.getValue() != null ? datePickerFin.getValue().toString() : null);
        a.setRemarque(txtRemarque.getText());

        try {
            if (affectationEnCours == null) {
                affectationDAO.ajouter(a);
            } else {
                affectationDAO.modifier(a);
            }

            synchroniserStatutVehicule(vehiculeChoisi.getIdVehicule(), seraActive);

            sauvegarde = true;
            fermer();
        } catch (SQLException e) {
            lblErreur.setText("Erreur base de données : " + e.getMessage());
        }
    }

    private void synchroniserStatutVehicule(int idNouveauVehicule, boolean seraActive) throws SQLException {
        if (affectationEnCours != null && etaitActiveAvantModification && idVehiculeAvantModification != idNouveauVehicule) {
            vehiculeDAO.modifierStatut(idVehiculeAvantModification, "Disponible");
        }

        if (seraActive) {
            vehiculeDAO.modifierStatut(idNouveauVehicule, "En service");
        } else if (affectationEnCours == null) {
        } else if (etaitActiveAvantModification) {
            vehiculeDAO.modifierStatut(idNouveauVehicule, "Disponible");
        }
    }

    private String valider() {
        if (cmbVehicule.getValue() == null) return "Veuillez choisir un véhicule.";
        if (cmbConducteur.getValue() == null) return "Veuillez choisir un conducteur.";
        if (datePickerDebut.getValue() == null) return "La date de début est obligatoire.";

        LocalDate debut = datePickerDebut.getValue();
        if (debut.isBefore(LocalDate.of(2000, 1, 1))) {
            return "La date de début semble incorrecte (avant l'an 2000).";
        }
        if (debut.isAfter(LocalDate.now().plusYears(1))) {
            return "La date de début ne peut pas être à plus d'un an dans le futur.";
        }

        if (datePickerFin.getValue() != null && datePickerFin.getValue().isBefore(debut)) {
            return "La date de fin ne peut pas être antérieure à la date de début.";
        }

        Vehicule vehiculePourDate = cmbVehicule.getValue();
        if (vehiculePourDate != null && vehiculePourDate.getDateAcquisition() != null
                && !vehiculePourDate.getDateAcquisition().isBlank()) {
            try {
                LocalDate dateAcquisition = LocalDate.parse(vehiculePourDate.getDateAcquisition());
                if (debut.isBefore(dateAcquisition)) {
                    return "La date de début (" + debut + ") est antérieure à la date d'acquisition du véhicule (" +
                            dateAcquisition + ").";
                }
            } catch (Exception ignored) {

            }
        }

        boolean seraActive = (datePickerFin.getValue() == null);
        Vehicule vehiculeChoisi = cmbVehicule.getValue();
        Conducteur conducteurChoisi = cmbConducteur.getValue();
        Integer idAIgnorer = (affectationEnCours != null) ? affectationEnCours.getIdAffectation() : null;
        String nouvelleDateDebut = debut.toString();
        String nouvelleDateFin = datePickerFin.getValue() != null ? datePickerFin.getValue().toString() : null;

        if (seraActive) {
            String statutVehicule = vehiculeChoisi.getStatut();
            if ("En entretien".equals(statutVehicule) || "Hors service".equals(statutVehicule)) {
                return "Ce véhicule est actuellement \"" + statutVehicule + "\" et ne peut pas être affecté.";
            }
        }

        try {
            if (affectationDAO.vehiculeADejaUneAffectationSurPeriode(
                    vehiculeChoisi.getIdVehicule(), nouvelleDateDebut, nouvelleDateFin, idAIgnorer)) {
                return "Ce véhicule est déjà affecté sur cette période (du " + nouvelleDateDebut +
                        (nouvelleDateFin != null ? " au " + nouvelleDateFin : " à une date indéterminée") + ").";
            }
            if (affectationDAO.conducteurADejaUneAffectationSurPeriode(
                    conducteurChoisi.getIdConducteur(), nouvelleDateDebut, nouvelleDateFin, idAIgnorer)) {
                return "Ce conducteur est déjà affecté à un autre véhicule sur cette période.";
            }
        } catch (SQLException e) {
            return "Impossible de vérifier les disponibilités : " + e.getMessage();
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
