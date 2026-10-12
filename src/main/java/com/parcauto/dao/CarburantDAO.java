package com.parcauto.dao;

import com.parcauto.model.Carburant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CarburantDAO {

    private static final String SELECT_BASE =
            "SELECT f.id_carburant, f.id_vehicule, v.immatriculation, " +
            "f.date_plein, f.quantite_litres, f.cout, f.kilometrage " +
            "FROM carburant f " +
            "JOIN vehicules v ON v.id_vehicule = f.id_vehicule ";

    public List<Carburant> getAll() throws SQLException {
        List<Carburant> liste = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY f.date_plein DESC";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                liste.add(mapperLigne(rs));
            }
        }
        return liste;
    }

    public List<Carburant> rechercher(String motCle) throws SQLException {
        List<Carburant> liste = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE v.immatriculation LIKE ? ORDER BY f.date_plein DESC";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + motCle + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapperLigne(rs));
                }
            }
        }
        return liste;
    }

    public void ajouter(Carburant f) throws SQLException {
        String sql = "INSERT INTO carburant (id_vehicule, date_plein, quantite_litres, cout, kilometrage) " +
                "VALUES (?,?,?,?,?)";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            remplirParametres(stmt, f);
            stmt.executeUpdate();
        }
    }

    public void modifier(Carburant f) throws SQLException {
        String sql = "UPDATE carburant SET id_vehicule=?, date_plein=?, quantite_litres=?, cout=?, kilometrage=? " +
                "WHERE id_carburant=?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            remplirParametres(stmt, f);
            stmt.setInt(6, f.getIdCarburant());
            stmt.executeUpdate();
        }
    }

    public void supprimer(int idCarburant) throws SQLException {
        String sql = "DELETE FROM carburant WHERE id_carburant=?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCarburant);
            stmt.executeUpdate();
        }
    }

    public double sommeCoutsTotaux() throws SQLException {
        String sql = "SELECT COALESCE(SUM(cout), 0) FROM carburant";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    private void remplirParametres(PreparedStatement stmt, Carburant f) throws SQLException {
        stmt.setInt(1, f.getIdVehicule());
        stmt.setString(2, f.getDatePlein());
        stmt.setDouble(3, f.getQuantiteLitres());
        stmt.setDouble(4, f.getCout());
        stmt.setInt(5, f.getKilometrage());
    }

    private Carburant mapperLigne(ResultSet rs) throws SQLException {
        return new Carburant(
                rs.getInt("id_carburant"),
                rs.getInt("id_vehicule"),
                rs.getString("immatriculation"),
                rs.getString("date_plein"),
                rs.getDouble("quantite_litres"),
                rs.getDouble("cout"),
                rs.getInt("kilometrage")
        );
    }
}
