package com.parcauto.model;


public class Vehicule {

    private int idVehicule;
    private String immatriculation;
    private String marque;
    private String modele;
    private String typeVehicule;
    private int annee;
    private int kilometrage;
    private String statut;          
    private String dateAcquisition; 
    private double prixAchat;

    public Vehicule() {
    }

    public Vehicule(int idVehicule, String immatriculation, String marque, String modele,
                     String typeVehicule, int annee, int kilometrage, String statut,
                     String dateAcquisition, double prixAchat) {
        this.idVehicule = idVehicule;
        this.immatriculation = immatriculation;
        this.marque = marque;
        this.modele = modele;
        this.typeVehicule = typeVehicule;
        this.annee = annee;
        this.kilometrage = kilometrage;
        this.statut = statut;
        this.dateAcquisition = dateAcquisition;
        this.prixAchat = prixAchat;
    }


    public int getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(int idVehicule) {
        this.idVehicule = idVehicule;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public String getTypeVehicule() {
        return typeVehicule;
    }

    public void setTypeVehicule(String typeVehicule) {
        this.typeVehicule = typeVehicule;
    }

    public int getAnnee() {
        return annee;
    }

    public void setAnnee(int annee) {
        this.annee = annee;
    }

    public int getKilometrage() {
        return kilometrage;
    }

    public void setKilometrage(int kilometrage) {
        this.kilometrage = kilometrage;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDateAcquisition() {
        return dateAcquisition;
    }

    public void setDateAcquisition(String dateAcquisition) {
        this.dateAcquisition = dateAcquisition;
    }

    public double getPrixAchat() {
        return prixAchat;
    }

    public void setPrixAchat(double prixAchat) {
        this.prixAchat = prixAchat;
    }

    @Override
    public String toString() {
        return marque + " " + modele + " (" + immatriculation + ")";
    }
}
