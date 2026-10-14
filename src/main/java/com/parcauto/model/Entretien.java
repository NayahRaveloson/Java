package com.parcauto.model;


public class Entretien {

    private int idEntretien;
    private int idVehicule;
    private String immatriculationVehicule; 
    private String typeEntretien;
    private String etatIntervention = "Entretien terminé";
    private String dateEntretien;
    private int kilometrage;
    private double cout;
    private String description;

    public Entretien() {
    }

    public Entretien(int idEntretien, int idVehicule, String immatriculationVehicule,
                      String typeEntretien, String dateEntretien, int kilometrage,
                      double cout, String description) {
        this.idEntretien = idEntretien;
        this.idVehicule = idVehicule;
        this.immatriculationVehicule = immatriculationVehicule;
        this.typeEntretien = typeEntretien;
        this.dateEntretien = dateEntretien;
        this.kilometrage = kilometrage;
        this.cout = cout;
        this.description = description;
    }


    public int getIdEntretien() {
        return idEntretien;
    }

    public void setIdEntretien(int idEntretien) {
        this.idEntretien = idEntretien;
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

    public String getTypeEntretien() {
        return typeEntretien;
    }

    public void setTypeEntretien(String typeEntretien) {
        this.typeEntretien = typeEntretien;
    }

    public String getEtatIntervention() {
        return etatIntervention;
    }

    public void setEtatIntervention(String etatIntervention) {
        this.etatIntervention = etatIntervention;
    }

    public String getDateEntretien() {
        return dateEntretien;
    }

    public void setDateEntretien(String dateEntretien) {
        this.dateEntretien = dateEntretien;
    }

    public int getKilometrage() {
        return kilometrage;
    }

    public void setKilometrage(int kilometrage) {
        this.kilometrage = kilometrage;
    }

    public double getCout() {
        return cout;
    }

    public void setCout(double cout) {
        this.cout = cout;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
