package com.parcauto.dao;

import com.parcauto.model.DocumentAdministratif;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DocumentDAO {

    private static final String SELECT_BASE =
            "SELECT d.id_document, d.id_vehicule, v.immatriculation, " +
            "d.type_document, d.date_debut, d.date_expiration, d.reference " +
            "FROM documents_administratifs d " +
            "JOIN vehicules v ON v.id_vehicule = d.id_vehicule ";

    public List<DocumentAdministratif> getAll() throws SQLException {
        List<DocumentAdministratif> liste = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY d.date_expiration ASC";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                liste.add(mapperLigne(rs));
            }
        }
        return liste;
    }

    public List<DocumentAdministratif> rechercher(String motCle) throws SQLException {
        List<DocumentAdministratif> liste = new ArrayList<>();
        String sql = SELECT_BASE +
                "WHERE v.immatriculation LIKE ? OR d.type_document LIKE ? OR d.reference LIKE ? " +
                "ORDER BY d.date_expiration ASC";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String param = "%" + motCle + "%";
            stmt.setString(1, param);
            stmt.setString(2, param);
            stmt.setString(3, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapperLigne(rs));
                }
            }
        }
        return liste;
    }

    public List<DocumentAdministratif> getDocumentsAExpirer(int nombreJours) throws SQLException {
        List<DocumentAdministratif> liste = new ArrayList<>();
        String sql = SELECT_BASE +
                "WHERE date(d.date_expiration) <= date('now', ?) " +
                "ORDER BY d.date_expiration ASC";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "+" + nombreJours + " days");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapperLigne(rs));
                }
            }
        }
        return liste;
    }

    public void ajouter(DocumentAdministratif d) throws SQLException {
        String sql = "INSERT INTO documents_administratifs (id_vehicule, type_document, date_debut, date_expiration, reference) " +
                "VALUES (?,?,?,?,?)";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            remplirParametres(stmt, d);
            stmt.executeUpdate();
        }
    }

    public void modifier(DocumentAdministratif d) throws SQLException {
        String sql = "UPDATE documents_administratifs SET id_vehicule=?, type_document=?, date_debut=?, " +
                "date_expiration=?, reference=? WHERE id_document=?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            remplirParametres(stmt, d);
            stmt.setInt(6, d.getIdDocument());
            stmt.executeUpdate();
        }
    }

    public void supprimer(int idDocument) throws SQLException {
        String sql = "DELETE FROM documents_administratifs WHERE id_document=?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idDocument);
            stmt.executeUpdate();
        }
    }

    private void remplirParametres(PreparedStatement stmt, DocumentAdministratif d) throws SQLException {
        stmt.setInt(1, d.getIdVehicule());
        stmt.setString(2, d.getTypeDocument());
        stmt.setString(3, d.getDateDebut());
        stmt.setString(4, d.getDateExpiration());
        stmt.setString(5, d.getReference());
    }

    private DocumentAdministratif mapperLigne(ResultSet rs) throws SQLException {
        return new DocumentAdministratif(
                rs.getInt("id_document"),
                rs.getInt("id_vehicule"),
                rs.getString("immatriculation"),
                rs.getString("type_document"),
                rs.getString("date_debut"),
                rs.getString("date_expiration"),
                rs.getString("reference")
        );
    }
}
