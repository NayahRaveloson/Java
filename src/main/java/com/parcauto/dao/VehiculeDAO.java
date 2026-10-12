package com.parcauto.dao;

import com.parcauto.model.Vehicule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class VehiculeDAO {

    public List<Vehicule> getAll() throws SQLException {
        List<Vehicule> liste = new ArrayList<>();
        String sql = "SELECT * FROM vehicules ORDER BY immatriculation";
        Connection conn = DatabaseConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapperLigne(rs));
            }
        }
        return liste;
    }

    public List<Vehicule> rechercher(String motCle) throws SQLException {
        List<Vehicule> liste = new ArrayList<>();
        String sql = "SELECT * FROM vehicules " +
                "WHERE immatriculation LIKE ? OR marque LIKE ? OR modele LIKE ? " +
                "ORDER BY immatriculation";
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

    public List<Vehicule> filtrerParStatut(String statut) throws SQLException {
        List<Vehicule> liste = new ArrayList<>();
        String sql = "SELECT * FROM vehicules WHERE statut = ? ORDER BY immatriculation";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statut);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapperLigne(rs));
                }
            }
        }
        return liste;
    }

    public void modifierStatut(int idVehicule, String nouveauStatut) throws SQLException {
        String sql = "UPDATE vehicules SET statut=? WHERE id_vehicule=?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nouveauStatut);
            stmt.setInt(2, idVehicule);
            stmt.executeUpdate();
        }
    }


    public void synchroniserKilometrageSiSuperieur(int idVehicule, int kilometrageConstate) throws SQLException {
        String sql = "UPDATE vehicules SET kilometrage = ? WHERE id_vehicule = ? AND kilometrage < ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, kilometrageConstate);
            stmt.setInt(2, idVehicule);
            stmt.setInt(3, kilometrageConstate);
            stmt.executeUpdate();
        }
    }

    public Vehicule getParId(int idVehicule) throws SQLException {
        String sql = "SELECT * FROM vehicules WHERE id_vehicule = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idVehicule);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapperLigne(rs) : null;
            }
        }
    }

    public void ajouter(Vehicule v) throws SQLException {
        String sql = "INSERT INTO vehicules " +
                "(immatriculation, marque, modele, type_vehicule, annee, kilometrage, statut, date_acquisition, prix_achat) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            remplirParametres(stmt, v);
            stmt.executeUpdate();
        }
    }

    public void modifier(Vehicule v) throws SQLException {
        String sql = "UPDATE vehicules SET immatriculation=?, marque=?, modele=?, type_vehicule=?, " +
                "annee=?, kilometrage=?, statut=?, date_acquisition=?, prix_achat=? WHERE id_vehicule=?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            remplirParametres(stmt, v);
            stmt.setInt(10, v.getIdVehicule());
            stmt.executeUpdate();
        }
    }

    public void supprimer(int idVehicule) throws SQLException {
        String sql = "DELETE FROM vehicules WHERE id_vehicule=?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idVehicule);
            stmt.executeUpdate();
        }
    }

    public int compterParStatut(String statut) throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehicules WHERE statut = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statut);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int compterTous() throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehicules";
        Connection conn = DatabaseConnection.getConnection();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }


    private void remplirParametres(PreparedStatement stmt, Vehicule v) throws SQLException {
        stmt.setString(1, v.getImmatriculation());
        stmt.setString(2, v.getMarque());
        stmt.setString(3, v.getModele());
        stmt.setString(4, v.getTypeVehicule());
        stmt.setInt(5, v.getAnnee());
        stmt.setInt(6, v.getKilometrage());
        stmt.setString(7, v.getStatut());
        stmt.setString(8, v.getDateAcquisition());
        stmt.setDouble(9, v.getPrixAchat());
    }

    private Vehicule mapperLigne(ResultSet rs) throws SQLException {
        return new Vehicule(
                rs.getInt("id_vehicule"),
                rs.getString("immatriculation"),
                rs.getString("marque"),
                rs.getString("modele"),
                rs.getString("type_vehicule"),
                rs.getInt("annee"),
                rs.getInt("kilometrage"),
                rs.getString("statut"),
                rs.getString("date_acquisition"),
                rs.getDouble("prix_achat")
        );
    }
}
