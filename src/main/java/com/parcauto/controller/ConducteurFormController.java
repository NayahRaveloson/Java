package com.parcauto.controller;

import com.parcauto.dao.ConducteurDAO;
import com.parcauto.model.Conducteur;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.regex.Pattern;


public class ConducteurFormController {

    private static final Pattern NOM_VALIDE = Pattern.compile("^[A-Za-zÀ-ÖØ-öø-ÿ '\\-]{2,50}$");
    private static final Pattern PERMIS_VALIDE = Pattern.compile("^[A-Za-z0-9\\-]{4,20}$");

    @FXML private Label lblTitreFormulaire;
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtPermis;
    @FXML private TextField txtTelephone;
    @FXML private DatePicker datePickerEmbauche;
    @FXML private Label lblErreur;

    private final ConducteurDAO conducteurDAO = new ConducteurDAO();
    private Conducteur conducteurEnCours;
    private boolean sauvegarde = false;

    @FXML
    public void initialize() {
        txtTelephone.setTextFormatter(new TextFormatter<String>(change -> {
            String nouveauTexte = change.getControlNewText();
            if (!nouveauTexte.matches("[0-9]*")) {
                return null;
            }
            if (nouveauTexte.length() > 10) {
                return null;
            }
            return change;
        }));

        datePickerEmbauche.setDayCellFactory(picker -> new DateCell() {
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

    public void initialiser(Conducteur conducteur) {
        this.conducteurEnCours = conducteur;
        if (conducteur == null) {
            lblTitreFormulaire.setText("Ajouter un conducteur");
            return;
        }

        lblTitreFormulaire.setText("Modifier le conducteur");
        txtNom.setText(conducteur.getNom());
        txtPrenom.setText(conducteur.getPrenom());
        txtPermis.setText(conducteur.getNumeroPermis());
        txtTelephone.setText(conducteur.getTelephone());
        if (conducteur.getDateEmbauche() != null && !conducteur.getDateEmbauche().isBlank()) {
            try {
                datePickerEmbauche.setValue(LocalDate.parse(conducteur.getDateEmbauche()));
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

        Conducteur c = (conducteurEnCours == null) ? new Conducteur() : conducteurEnCours;
        c.setNom(txtNom.getText().trim());
        c.setPrenom(txtPrenom.getText().trim());
        c.setNumeroPermis(txtPermis.getText().trim());
        c.setTelephone(txtTelephone.getText().trim());
        c.setDateEmbauche(datePickerEmbauche.getValue() != null ? datePickerEmbauche.getValue().toString() : null);

        try {
            if (conducteurEnCours == null) {
                conducteurDAO.ajouter(c);
            } else {
                conducteurDAO.modifier(c);
            }
            sauvegarde = true;
            fermer();
        } catch (SQLException e) {
            if (e.getMessage() != null && (e.getMessage().toUpperCase().contains("DUPLICATE") || e.getMessage().contains("UNIQUE"))) {
                lblErreur.setText("Ce numéro de permis existe déjà.");
            } else {
                lblErreur.setText("Erreur base de données : " + e.getMessage());
            }
        }
    }

    private String valider() {
        if (txtNom.getText().isBlank()) return "Le nom est obligatoire.";
        if (!NOM_VALIDE.matcher(txtNom.getText().trim()).matches()) return "Le nom ne doit contenir que des lettres.";
        if (txtPrenom.getText().isBlank()) return "Le prénom est obligatoire.";
        if (!NOM_VALIDE.matcher(txtPrenom.getText().trim()).matches()) return "Le prénom ne doit contenir que des lettres.";
        if (txtPermis.getText().isBlank()) return "Le numéro de permis est obligatoire.";
        if (!PERMIS_VALIDE.matcher(txtPermis.getText().trim()).matches()) {
            return "Le numéro de permis doit contenir 4 à 20 lettres, chiffres ou tirets.";
        }

        String telephone = txtTelephone.getText();
        if (telephone != null && !telephone.isBlank() && !telephone.matches("[0-9]{10}")) {
            return "Le téléphone doit contenir exactement 10 chiffres (ni plus, ni moins), sans espace ni symbole.";
        }

        if (datePickerEmbauche.getValue() != null) {
            LocalDate date = datePickerEmbauche.getValue();
            if (date.isBefore(LocalDate.now())) {
                return "La date d'embauche ne peut pas être une date déjà passée.";
            }
        }

        return null;
    }

    @FXML
    public void annuler() {
        fermer();
    }

    private void fermer() {
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }
}
