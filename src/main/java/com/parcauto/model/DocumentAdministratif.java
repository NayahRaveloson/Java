package com.parcauto.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


public class DocumentAdministratif {

    private int idDocument;
    private int idVehicule;
    private String immatriculationVehicule; 
    private String typeDocument;
    private String dateDebut;      
    private String dateExpiration; 
    private String reference;

    public DocumentAdministratif() {
    }

    public DocumentAdministratif(int idDocument, int idVehicule, String immatriculationVehicule,
                                  String typeDocument, String dateDebut, String dateExpiration,
                                  String reference) {
        this.idDocument = idDocument;
        this.idVehicule = idVehicule;
        this.immatriculationVehicule = immatriculationVehicule;
        this.typeDocument = typeDocument;
        this.dateDebut = dateDebut;
        this.dateExpiration = dateExpiration;
        this.reference = reference;
    }


    public int getIdDocument() {
        return idDocument;
    }

    public void setIdDocument(int idDocument) {
        this.idDocument = idDocument;
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

    public String getTypeDocument() {
        return typeDocument;
    }

    public void setTypeDocument(String typeDocument) {
        this.typeDocument = typeDocument;
    }

    public String getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(String dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(String dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

 
    public String getStatutConformite() {
        try {
            LocalDate expiration = LocalDate.parse(dateExpiration);
            LocalDate aujourdHui = LocalDate.now();
            long joursRestants = ChronoUnit.DAYS.between(aujourdHui, expiration);
            if (joursRestants < 0) {
                return "Expiré";
            } else if (joursRestants <= 30) {
                return "Bientôt expiré";
            } else {
                return "Valide";
            }
        } catch (Exception e) {
            return "Inconnu";
        }
    }
}
