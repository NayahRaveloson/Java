package com.parcauto.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public final class DatabaseConnection {

    private static final String HOTE = "localhost";
    private static final String PORT = "3306";
    private static final String NOM_BASE = "parc_auto";
    private static final String UTILISATEUR = "root";
    private static final String MOT_DE_PASSE = "";   

    private static final String URL_SANS_BASE =
            "jdbc:mysql://" + HOTE + ":" + PORT + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String URL_AVEC_BASE =
            "jdbc:mysql://" + HOTE + ":" + PORT + "/" + NOM_BASE + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static Connection connection;

    private DatabaseConnection() {

    }


    public static Connection getConnection() {
        if (connection == null) {
            try {
                creerBaseSiAbsente();
                connection = DriverManager.getConnection(URL_AVEC_BASE, UTILISATEUR, MOT_DE_PASSE);

                if (!tablesExistent()) {
                    initialiserBase();
                }
            } catch (SQLException | IOException e) {
                throw new RuntimeException(
                        "Impossible de se connecter à MySQL.\n" +
                        "Vérifiez que :\n" +
                        "  1) le serveur MySQL est démarré (WAMP/XAMPP ou service MySQL),\n" +
                        "  2) l'utilisateur/mot de passe dans DatabaseConnection.java sont corrects,\n" +
                        "  3) le pilote MySQL (mysql-connector-j) est bien dans le classpath.", e);
            }
        }
        return connection;
    }

    private static void creerBaseSiAbsente() throws SQLException {
        try (Connection connexionTemporaire = DriverManager.getConnection(URL_SANS_BASE, UTILISATEUR, MOT_DE_PASSE);
             Statement stmt = connexionTemporaire.createStatement()) {
            stmt.execute("CREATE DATABASE IF NOT EXISTS " + NOM_BASE + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
    }

    private static boolean tablesExistent() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'vehicules'")) {
            return rs.next();
        }
    }


    private static void initialiserBase() throws SQLException, IOException {
        try (InputStream flux = DatabaseConnection.class.getResourceAsStream("/db/schema.sql")) {
            if (flux == null) {
                throw new IOException("Fichier /db/schema.sql introuvable dans les ressources.");
            }
            String script = new String(flux.readAllBytes(), StandardCharsets.UTF_8);

            try (Statement stmt = connection.createStatement()) {
                for (String instruction : script.split(";")) {
                    String propre = instruction.trim();
                    if (!propre.isEmpty()) {
                        stmt.execute(propre);
                    }
                }
            }
        }
    }
}
