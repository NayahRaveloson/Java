package com.parcauto.dao;

import com.parcauto.model.Entretien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class EntretienDAO {

    private static final String SELECT_BASE =
            "SELECT e.id_entretien, e.id_vehicule, v.immatriculation, " +
            "e.type_entretien, e.date_entretien, e.kilometrage, e.cout, e.description " +
            "FROM entretiens e " +
            "JOIN vehicules v ON v.id_vehicule = e.id_vehicule ";

    public List<Entretien> getAll() throws SQLException {
        List<Entretien> liste = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY e.date_entretien DESC";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                liste.add(mapperLigne(rs));
            }
        }
        return liste;
    }

    public List<Entretien> rechercher(String motCle) throws SQLException {
        List<Entretien> liste = new ArrayList<>();
        String sql = SELECT_BASE +
                "WHERE v.immatriculation LIKE ? OR e.type_entretien LIKE ? " +
                "ORDER BY e.date_entretien DESC";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String param = "%" + motCle + "%";
            stmt.setString(1, param);
            stmt.setString(2, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapperLigne(rs));
                }
            }
        }
        return liste;
    }

    public void ajouter(Entretien e) throws SQLException {
        String sql = "INSERT INTO entretiens (id_vehicule, type_entretien, date_entretien, kilometrage, cout, description) " +
                "VALUES (?,?,?,?,?,?)";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            remplirParametres(stmt, e);
            stmt.executeUpdate();
        }
    }

    public void modifier(Entretien e) throws SQLException {
        String sql = "UPDATE entretiens SET id_vehicule=?, type_entretien=?, date_entretien=?, " +
                "kilometrage=?, cout=?, description=? WHERE id_entretien=?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            remplirParametres(stmt, e);
            stmt.setInt(7, e.getIdEntretien());
            stmt.executeUpdate();
        }
    }

    public void supprimer(int idEntretien) throws SQLException {
        String sql = "DELETE FROM entretiens WHERE id_entretien=?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEntretien);
            stmt.executeUpdate();
        }
    }

    public double sommeCoutsTotaux() throws SQLException {
        String sql = "SELECT COALESCE(SUM(cout), 0) FROM entretiens";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    private void remplirParametres(PreparedStatement stmt, Entretien e) throws SQLException {
        stmt.setInt(1, e.getIdVehicule());
        stmt.setString(2, e.getTypeEntretien());
        stmt.setString(3, e.getDateEntretien());
        stmt.setInt(4, e.getKilometrage());
        stmt.setDouble(5, e.getCout());
        stmt.setString(6, e.getDescription());
    }

    private Entretien mapperLigne(ResultSet rs) throws SQLException {
        return new Entretien(
                rs.getInt("id_entretien"),
                rs.getInt("id_vehicule"),
                rs.getString("immatriculation"),
                rs.getString("type_entretien"),
                rs.getString("date_entretien"),
                rs.getInt("kilometrage"),
                rs.getDouble("cout"),
                rs.getString("description")
        );
    }
}
