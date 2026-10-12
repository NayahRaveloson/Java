package com.parcauto.model;


public class Conducteur {

    private int idConducteur;
    private String nom;
    private String prenom;
    private String numeroPermis;
    private String telephone;
    private String dateEmbauche; 

    public Conducteur() {
        
    }

    public Conducteur(int idConducteur, String nom, String prenom, String numeroPermis,
                       String telephone, String dateEmbauche) {
        this.idConducteur = idConducteur;
        this.nom = nom;
        this.prenom = prenom;
        this.numeroPermis = numeroPermis;
        this.telephone = telephone;
        this.dateEmbauche = dateEmbauche;
    }


    public int getIdConducteur() {
        return idConducteur;
    }

    public void setIdConducteur(int idConducteur) {
        this.idConducteur = idConducteur;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNumeroPermis() {
        return numeroPermis;
    }

    public void setNumeroPermis(String numeroPermis) {
        this.numeroPermis = numeroPermis;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getDateEmbauche() {
        return dateEmbauche;
    }

    public void setDateEmbauche(String dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    @Override
    public String toString() {
        return getNomComplet();
    }
}
