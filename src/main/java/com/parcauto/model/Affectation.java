package com.parcauto.model;

import java.time.LocalDate;


public class Affectation {

    private int idAffectation;
    private int idVehicule;
    private String immatriculationVehicule;
    private int idConducteur;
    private String nomConducteur;
    private String dateDebut;  
    private String dateFin;    
    private String remarque;

    public Affectation() {
    }

    public Affectation(int idAffectation, int idVehicule, String immatriculationVehicule,
                        int idConducteur, String nomConducteur, String dateDebut,
                        String dateFin, String remarque) {
        this.idAffectation = idAffectation;
        this.idVehicule = idVehicule;
        this.immatriculationVehicule = immatriculationVehicule;
        this.idConducteur = idConducteur;
        this.nomConducteur = nomConducteur;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.remarque = remarque;
    }


    public int getIdAffectation() {
        return idAffectation;
    }

    public void setIdAffectation(int idAffectation) {
        this.idAffectation = idAffectation;
    }

    public int getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(int idVehicule) {
        this.idVehicule = idVehicule;
    }

    public String getImmatriculationVehicule() {
        return immatriculationVehicule;
    }

    public void setImmatriculationVehicule(String immatriculationVehicule) {
        this.immatriculationVehicule = immatriculationVehicule;
    }

    public int getIdConducteur() {
        return idConducteur;
    }

    public void setIdConducteur(int idConducteur) {
        this.idConducteur = idConducteur;
    }

    public String getNomConducteur() {
        return nomConducteur;
    }

    public void setNomConducteur(String nomConducteur) {
        this.nomConducteur = nomConducteur;
    }

    public String getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(String dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getDateFin() {
        return dateFin;
    }

    public void setDateFin(String dateFin) {
        this.dateFin = dateFin;
    }

    public String getRemarque() {
        return remarque;
    }

    public void setRemarque(String remarque) {
        this.remarque = remarque;
    }


    public boolean estEnCours() {
        if (dateFin == null || dateFin.isBlank()) {
            return true;
        }
        try {
            LocalDate fin = LocalDate.parse(dateFin);
            return !fin.isBefore(LocalDate.now());
        } catch (Exception e) {
            
            return true;
        }
    }
}
