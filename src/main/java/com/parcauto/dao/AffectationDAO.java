package com.parcauto.dao;

import com.parcauto.model.Affectation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class AffectationDAO {

    private static final String SELECT_BASE =
            "SELECT a.id_affectation, a.id_vehicule, v.immatriculation, " +
            "a.id_conducteur, CONCAT(c.prenom, ' ', c.nom) AS nom_conducteur, " +
            "a.date_debut, a.date_fin, a.remarque " +
            "FROM affectations a " +
            "JOIN vehicules v ON v.id_vehicule = a.id_vehicule " +
            "JOIN conducteurs c ON c.id_conducteur = a.id_conducteur ";

    public List<Affectation> getAll() throws SQLException {
        List<Affectation> liste = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY a.date_debut DESC";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                liste.add(mapperLigne(rs));
            }
        }
        return liste;
    }

    public List<Affectation> rechercher(String motCle) throws SQLException {
        List<Affectation> liste = new ArrayList<>();
        String sql = SELECT_BASE +
                "WHERE v.immatriculation LIKE ? OR c.nom LIKE ? OR c.prenom LIKE ? " +
                "ORDER BY a.date_debut DESC";
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

 
    public boolean vehiculeADejaUneAffectationActive(int idVehicule, Integer idAffectationAIgnorer) throws SQLException {
        String sql = "SELECT COUNT(*) FROM affectations WHERE id_vehicule = ? AND date_fin IS NULL" +
                (idAffectationAIgnorer != null ? " AND id_affectation != ?" : "");
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idVehicule);
            if (idAffectationAIgnorer != null) {
                stmt.setInt(2, idAffectationAIgnorer);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * @param idAffectationAIgnorer permet d'exclure l'affectation qu'on modifie (peut être null)
     */
    public boolean vehiculeADejaUneAffectationSurPeriode(int idVehicule, String nouvelleDateDebut,
                                                          String nouvelleDateFin, Integer idAffectationAIgnorer) throws SQLException {
        return aUneAffectationSurPeriode("id_vehicule", idVehicule, nouvelleDateDebut, nouvelleDateFin, idAffectationAIgnorer);
    }

  
    public boolean conducteurADejaUneAffectationSurPeriode(int idConducteur, String nouvelleDateDebut,
                                                            String nouvelleDateFin, Integer idAffectationAIgnorer) throws SQLException {
        return aUneAffectationSurPeriode("id_conducteur", idConducteur, nouvelleDateDebut, nouvelleDateFin, idAffectationAIgnorer);
    }


    private boolean aUneAffectationSurPeriode(String colonneId, int id, String nouvelleDateDebut,
                                               String nouvelleDateFin, Integer idAffectationAIgnorer) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM affectations WHERE " + colonneId + " = ? " +
                "AND (date_fin IS NULL OR date_fin >= ?) ");
        boolean nouvelleEstOuverte = (nouvelleDateFin == null || nouvelleDateFin.isBlank());
        if (!nouvelleEstOuverte) {
            sql.append("AND date_debut <= ? ");
        }
        if (idAffectationAIgnorer != null) {
            sql.append("AND id_affectation != ? ");
        }

        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int index = 1;
            stmt.setInt(index++, id);
            stmt.setString(index++, nouvelleDateDebut);
            if (!nouvelleEstOuverte) {
                stmt.setString(index++, nouvelleDateFin);
            }
            if (idAffectationAIgnorer != null) {
                stmt.setInt(index, idAffectationAIgnorer);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public void ajouter(Affectation a) throws SQLException {
        String sql = "INSERT INTO affectations (id_vehicule, id_conducteur, date_debut, date_fin, remarque) " +
                "VALUES (?,?,?,?,?)";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, a.getIdVehicule());
            stmt.setInt(2, a.getIdConducteur());
            stmt.setString(3, a.getDateDebut());
            stmt.setString(4, a.getDateFin());
            stmt.setString(5, a.getRemarque());
            stmt.executeUpdate();
        }
    }

    public void modifier(Affectation a) throws SQLException {
        String sql = "UPDATE affectations SET id_vehicule=?, id_conducteur=?, date_debut=?, date_fin=?, remarque=? " +
                "WHERE id_affectation=?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, a.getIdVehicule());
            stmt.setInt(2, a.getIdConducteur());
            stmt.setString(3, a.getDateDebut());
            stmt.setString(4, a.getDateFin());
            stmt.setString(5, a.getRemarque());
            stmt.setInt(6, a.getIdAffectation());
            stmt.executeUpdate();
        }
    }

    public void supprimer(int idAffectation) throws SQLException {
        String sql = "DELETE FROM affectations WHERE id_affectation=?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAffectation);
            stmt.executeUpdate();
        }
    }

    private Affectation mapperLigne(ResultSet rs) throws SQLException {
        return new Affectation(
                rs.getInt("id_affectation"),
                rs.getInt("id_vehicule"),
                rs.getString("immatriculation"),
                rs.getInt("id_conducteur"),
                rs.getString("nom_conducteur"),
                rs.getString("date_debut"),
                rs.getString("date_fin"),
                rs.getString("remarque")
        );
    }
}
