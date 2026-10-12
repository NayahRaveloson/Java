package com.parcauto.model;


public class Carburant {

    private int idCarburant;
    private int idVehicule;
    private String immatriculationVehicule; 
    private String datePlein; 
    private double quantiteLitres;
    private double cout;
    private int kilometrage;

    public Carburant() {
    }

    public Carburant(int idCarburant, int idVehicule, String immatriculationVehicule,
                      String datePlein, double quantiteLitres, double cout, int kilometrage) {
        this.idCarburant = idCarburant;
        this.idVehicule = idVehicule;
        this.immatriculationVehicule = immatriculationVehicule;
        this.datePlein = datePlein;
        this.quantiteLitres = quantiteLitres;
        this.cout = cout;
        this.kilometrage = kilometrage;
    }


    public int getIdCarburant() {
        return idCarburant;
    }

    public void setIdCarburant(int idCarburant) {
        this.idCarburant = idCarburant;
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

    public String getDatePlein() {
        return datePlein;
    }

    public void setDatePlein(String datePlein) {
        this.datePlein = datePlein;
    }

    public double getQuantiteLitres() {
        return quantiteLitres;
    }

    public void setQuantiteLitres(double quantiteLitres) {
        this.quantiteLitres = quantiteLitres;
    }

    public double getCout() {
        return cout;
    }

    public void setCout(double cout) {
        this.cout = cout;
    }

    public int getKilometrage() {
        return kilometrage;
    }

    public void setKilometrage(int kilometrage) {
        this.kilometrage = kilometrage;
    }
}
