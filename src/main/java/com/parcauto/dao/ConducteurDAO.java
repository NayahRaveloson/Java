package com.parcauto.dao;

import com.parcauto.model.Conducteur;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class ConducteurDAO {

    public List<Conducteur> getAll() throws SQLException {
        List<Conducteur> liste = new ArrayList<>();
        String sql = "SELECT * FROM conducteurs ORDER BY nom, prenom";
        Connection conn = DatabaseConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapperLigne(rs));
            }
        }
        return liste;
    }

    public List<Conducteur> rechercher(String motCle) throws SQLException {
        List<Conducteur> liste = new ArrayList<>();
        String sql = "SELECT * FROM conducteurs " +
                "WHERE nom LIKE ? OR prenom LIKE ? OR numero_permis LIKE ? " +
                "ORDER BY nom, prenom";
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

    public void ajouter(Conducteur c) throws SQLException {
        String sql = "INSERT INTO conducteurs (nom, prenom, numero_permis, telephone, date_embauche) " +
                "VALUES (?,?,?,?,?)";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            remplirParametres(stmt, c);
            stmt.executeUpdate();
        }
    }

    public void modifier(Conducteur c) throws SQLException {
        String sql = "UPDATE conducteurs SET nom=?, prenom=?, numero_permis=?, telephone=?, date_embauche=? " +
                "WHERE id_conducteur=?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            remplirParametres(stmt, c);
            stmt.setInt(6, c.getIdConducteur());
            stmt.executeUpdate();
        }
    }

    public void supprimer(int idConducteur) throws SQLException {
        String sql = "DELETE FROM conducteurs WHERE id_conducteur=?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idConducteur);
            stmt.executeUpdate();
        }
    }

    public int compterTous() throws SQLException {
        String sql = "SELECT COUNT(*) FROM conducteurs";
        Connection conn = DatabaseConnection.getConnection();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }


    private void remplirParametres(PreparedStatement stmt, Conducteur c) throws SQLException {
        stmt.setString(1, c.getNom());
        stmt.setString(2, c.getPrenom());
        stmt.setString(3, c.getNumeroPermis());
        stmt.setString(4, c.getTelephone());
        stmt.setString(5, c.getDateEmbauche());
    }

    private Conducteur mapperLigne(ResultSet rs) throws SQLException {
        return new Conducteur(
                rs.getInt("id_conducteur"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("numero_permis"),
                rs.getString("telephone"),
                rs.getString("date_embauche")
        );
    }
}
