package com.parcauto.controller;

import com.parcauto.dao.CarburantDAO;
import com.parcauto.dao.VehiculeDAO;
import com.parcauto.model.Carburant;
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
import java.util.Map;


public class CarburantFormController {

    private static final Map<String, Double> CAPACITES_RESERVOIR = Map.of(
            "Citadine", 40.0,
            "Berline", 50.0,
            "SUV", 65.0,
            "Utilitaire", 70.0,
            "Fourgonnette", 80.0,
            "Pick-up", 75.0,
            "Camion", 150.0,
            "Autre", 50.0
    );

    private static final double PRIX_REFERENCE_LITRE = 3500.0;

    private static final String NIVEAU_PLEIN = "Plein complet";
    private static final String NIVEAU_TROIS_QUARTS = "Trois-quarts (¾ réservoir)";
    private static final String NIVEAU_DEMI = "Demi-plein (½ réservoir)";
    private static final String NIVEAU_QUART = "Quart (¼ réservoir)";
    private static final String NIVEAU_PERSONNALISE = "Personnalisé";

    @FXML private Label lblTitreFormulaire;
    @FXML private ComboBox<Vehicule> cmbVehicule;
    @FXML private DatePicker datePickerDate;
    @FXML private ComboBox<String> cmbNiveauPlein;
    @FXML private TextField txtQuantite;
    @FXML private TextField txtCout;
    @FXML private TextField txtKilometrage;
    @FXML private Label lblErreur;

    private final CarburantDAO carburantDAO = new CarburantDAO();
    private final VehiculeDAO vehiculeDAO = new VehiculeDAO();

    private Carburant carburantEnCours;
    private boolean sauvegarde = false;
    private boolean chargementEnCours = false; 

    @FXML
    public void initialize() {
        try {
            cmbVehicule.setItems(FXCollections.observableArrayList(vehiculeDAO.getAll()));
        } catch (SQLException e) {
            lblErreur.setText("Impossible de charger les véhicules : " + e.getMessage());
        }

        cmbNiveauPlein.setItems(FXCollections.observableArrayList(
                NIVEAU_PLEIN, NIVEAU_TROIS_QUARTS, NIVEAU_DEMI, NIVEAU_QUART, NIVEAU_PERSONNALISE));

        datePickerDate.setDisable(true);
        datePickerDate.setEditable(false);

        txtCout.setEditable(false);
        txtCout.setFocusTraversable(false);

        cmbVehicule.valueProperty().addListener((obs, ancien, nouveau) -> recalculerQuantiteEtCout());
        cmbNiveauPlein.valueProperty().addListener((obs, ancien, nouveau) -> recalculerQuantiteEtCout());
        txtQuantite.textProperty().addListener((obs, ancien, nouveau) -> recalculerCoutSeul());
    }

    public void initialiser(Carburant carburant) {
        this.carburantEnCours = carburant;
        chargementEnCours = true;
        try {
            if (carburant == null) {
                lblTitreFormulaire.setText("Ajouter un plein");
                datePickerDate.setValue(LocalDate.now());
                cmbNiveauPlein.getSelectionModel().select(NIVEAU_PLEIN);
                return;
            }

            lblTitreFormulaire.setText("Modifier le plein");
            for (Vehicule v : cmbVehicule.getItems()) {
                if (v.getIdVehicule() == carburant.getIdVehicule()) {
                    cmbVehicule.getSelectionModel().select(v);
                    break;
                }
            }
            if (carburant.getDatePlein() != null && !carburant.getDatePlein().isBlank()) {
                datePickerDate.setValue(LocalDate.parse(carburant.getDatePlein()));
            } else {
                datePickerDate.setValue(LocalDate.now());
            }

            cmbNiveauPlein.getSelectionModel().select(NIVEAU_PERSONNALISE);
            txtQuantite.setEditable(true);
            txtQuantite.setText(formaterNombre(carburant.getQuantiteLitres()));
            txtCout.setText(String.valueOf((long) carburant.getCout()));
            txtKilometrage.setText(carburant.getKilometrage() > 0 ? String.valueOf(carburant.getKilometrage()) : "");
        } finally {
            chargementEnCours = false;
        }
    }

    private void recalculerQuantiteEtCout() {
        if (chargementEnCours) {
            return;
        }
        Vehicule vehicule = cmbVehicule.getValue();
        String niveau = cmbNiveauPlein.getValue();
        if (vehicule == null || niveau == null) {
            return;
        }

        if (NIVEAU_PERSONNALISE.equals(niveau)) {
            txtQuantite.setEditable(true);
            txtQuantite.getStyleClass().remove("field-calculated");
            recalculerCoutSeul();
            return;
        }

        double capacite = CAPACITES_RESERVOIR.getOrDefault(vehicule.getTypeVehicule(), 50.0);
        double ratio = switch (niveau) {
            case NIVEAU_PLEIN -> 1.0;
            case NIVEAU_TROIS_QUARTS -> 0.75;
            case NIVEAU_DEMI -> 0.5;
            case NIVEAU_QUART -> 0.25;
            default -> 1.0;
        };
        double litres = Math.round(capacite * ratio * 10.0) / 10.0;

        txtQuantite.setEditable(false);
        if (!txtQuantite.getStyleClass().contains("field-calculated")) {
            txtQuantite.getStyleClass().add("field-calculated");
        }
        txtQuantite.setText(formaterNombre(litres));
    }

    private void recalculerCoutSeul() {
        if (chargementEnCours) {
            return;
        }
        try {
            double litres = Double.parseDouble(txtQuantite.getText().trim().replace(",", "."));
            txtCout.setText(String.valueOf(Math.round(litres * PRIX_REFERENCE_LITRE)));
        } catch (NumberFormatException ignored) {
            txtCout.setText("");
        }
    }

    private String formaterNombre(double valeur) {
        return (valeur == Math.rint(valeur)) ? String.valueOf((long) valeur) : String.valueOf(valeur);
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

        Carburant f = (carburantEnCours == null) ? new Carburant() : carburantEnCours;
        f.setIdVehicule(cmbVehicule.getValue().getIdVehicule());
        f.setDatePlein(datePickerDate.getValue().toString());
        f.setQuantiteLitres(Double.parseDouble(txtQuantite.getText().trim().replace(",", ".")));
        f.setCout(Double.parseDouble(txtCout.getText().trim()));
        f.setKilometrage(txtKilometrage.getText().isBlank() ? 0 : Integer.parseInt(txtKilometrage.getText().trim()));

        try {
            if (carburantEnCours == null) {
                carburantDAO.ajouter(f);
            } else {
                carburantDAO.modifier(f);
            }
            if (f.getKilometrage() > 0) {
                vehiculeDAO.synchroniserKilometrageSiSuperieur(f.getIdVehicule(), f.getKilometrage());
            }
            sauvegarde = true;
            fermer();
        } catch (SQLException e) {
            lblErreur.setText("Erreur base de données : " + e.getMessage());
        }
    }

    private String valider() {
        if (cmbVehicule.getValue() == null) return "Veuillez choisir un véhicule.";
        if (cmbNiveauPlein.getValue() == null) return "Veuillez choisir un niveau de plein.";
        if (datePickerDate.getValue() == null) return "La date du plein est introuvable (erreur interne).";

        if (txtQuantite.getText().isBlank()) return "La quantité est obligatoire.";
        double quantite;
        try {
            quantite = Double.parseDouble(txtQuantite.getText().trim().replace(",", "."));
            if (quantite <= 0) return "La quantité doit être positive.";
        } catch (NumberFormatException ex) {
            return "La quantité doit être un nombre (ex : 45.5).";
        }

        Vehicule vehiculeChoisi = cmbVehicule.getValue();
        double capaciteMax = CAPACITES_RESERVOIR.getOrDefault(vehiculeChoisi.getTypeVehicule(), 50.0);
        if (quantite > capaciteMax * 1.15) {
            return "La quantité saisie (" + formaterNombre(quantite) + " L) dépasse largement la capacité " +
                    "habituelle du réservoir pour ce type de véhicule (≈ " + formaterNombre(capaciteMax) + " L).";
        }

        if (txtCout.getText().isBlank()) return "Le coût n'a pas pu être calculé, vérifiez la quantité saisie.";
        try {
            if (Double.parseDouble(txtCout.getText().trim()) < 0) return "Le coût ne peut pas être négatif.";
        } catch (NumberFormatException ex) {
            return "Le coût calculé est invalide.";
        }

        if (!txtKilometrage.getText().isBlank()) {
            try {
                int km = Integer.parseInt(txtKilometrage.getText().trim());
                if (km < 0) return "Le kilométrage ne peut pas être négatif.";
                if (km < vehiculeChoisi.getKilometrage()) {
                    return "Le kilométrage saisi (" + km + " km) est inférieur au kilométrage actuel du véhicule (" +
                            vehiculeChoisi.getKilometrage() + " km). Vérifiez la valeur.";
                }
            } catch (NumberFormatException ex) {
                return "Le kilométrage doit être un nombre entier.";
            }
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
