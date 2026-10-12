CREATE TABLE IF NOT EXISTS vehicules (
    id_vehicule      INT AUTO_INCREMENT PRIMARY KEY,
    immatriculation  VARCHAR(20) NOT NULL UNIQUE,
    marque           VARCHAR(50) NOT NULL,
    modele           VARCHAR(50) NOT NULL,
    type_vehicule    VARCHAR(30) NOT NULL,
    annee            INT,
    kilometrage      INT NOT NULL DEFAULT 0,
    statut           VARCHAR(20) NOT NULL DEFAULT 'Disponible'
                     CHECK (statut IN ('Disponible', 'En service', 'En entretien', 'Hors service')),
    date_acquisition VARCHAR(10),
    prix_achat       DOUBLE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS conducteurs (
    id_conducteur   INT AUTO_INCREMENT PRIMARY KEY,
    nom             VARCHAR(50) NOT NULL,
    prenom          VARCHAR(50) NOT NULL,
    numero_permis   VARCHAR(30) NOT NULL UNIQUE,
    telephone       VARCHAR(20),
    date_embauche   VARCHAR(10)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS affectations (
    id_affectation  INT AUTO_INCREMENT PRIMARY KEY,
    id_vehicule     INT NOT NULL,
    id_conducteur   INT NOT NULL,
    date_debut      VARCHAR(10) NOT NULL,
    date_fin        VARCHAR(10),
    remarque        VARCHAR(255),
    FOREIGN KEY (id_vehicule) REFERENCES vehicules(id_vehicule) ON DELETE CASCADE,
    FOREIGN KEY (id_conducteur) REFERENCES conducteurs(id_conducteur) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS entretiens (
    id_entretien     INT AUTO_INCREMENT PRIMARY KEY,
    id_vehicule      INT NOT NULL,
    type_entretien   VARCHAR(50) NOT NULL,
    date_entretien   VARCHAR(10) NOT NULL,
    kilometrage      INT,
    cout             DOUBLE,
    description      VARCHAR(500),
    FOREIGN KEY (id_vehicule) REFERENCES vehicules(id_vehicule) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS carburant (
    id_carburant     INT AUTO_INCREMENT PRIMARY KEY,
    id_vehicule      INT NOT NULL,
    date_plein       VARCHAR(10) NOT NULL,
    quantite_litres  DOUBLE NOT NULL,
    cout             DOUBLE NOT NULL,
    kilometrage      INT,
    FOREIGN KEY (id_vehicule) REFERENCES vehicules(id_vehicule) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS documents_administratifs (
    id_document      INT AUTO_INCREMENT PRIMARY KEY,
    id_vehicule      INT NOT NULL,
    type_document    VARCHAR(30) NOT NULL
                     CHECK (type_document IN ('Assurance', 'Visite technique', 'Vignette', 'Carte grise', 'Autre')),
    date_debut       VARCHAR(10),
    date_expiration  VARCHAR(10) NOT NULL,
    reference        VARCHAR(50),
    FOREIGN KEY (id_vehicule) REFERENCES vehicules(id_vehicule) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE INDEX idx_vehicule_immat ON vehicules(immatriculation);
CREATE INDEX idx_vehicule_statut ON vehicules(statut);
CREATE INDEX idx_affectation_vehicule ON affectations(id_vehicule);
CREATE INDEX idx_entretien_vehicule ON entretiens(id_vehicule);
CREATE INDEX idx_carburant_vehicule ON carburant(id_vehicule);
CREATE INDEX idx_document_vehicule ON documents_administratifs(id_vehicule);
CREATE INDEX idx_document_expiration ON documents_administratifs(date_expiration);


INSERT INTO vehicules (immatriculation, marque, modele, type_vehicule, annee, kilometrage, statut, date_acquisition, prix_achat)
VALUES
('1234 TBA', 'Toyota', 'Hilux', 'Pick-up', 2021, 45000, 'Disponible', '2021-03-15', 35000000),
('5678 TAB', 'Renault', 'Kangoo', 'Utilitaire', 2019, 88000, 'En service', '2019-06-01', 18000000),
('9012 TAA', 'Nissan', 'Navara', 'Pick-up', 2022, 12000, 'En entretien', '2022-01-10', 40000000),
('3456 TCA', 'Toyota', 'Corolla', 'Berline', 2020, 62000, 'Disponible', '2020-07-20', 22000000);

INSERT INTO conducteurs (nom, prenom, numero_permis, telephone, date_embauche)
VALUES
('Rakoto', 'Jean', 'PC-001-2018', '0341234567', '2018-05-01'),
('Rasoa', 'Marie', 'PC-002-2020', '0347654321', '2020-09-15');

INSERT INTO affectations (id_vehicule, id_conducteur, date_debut, date_fin, remarque)
VALUES
(2, 1, '2024-01-05', NULL, 'Affectation courante');

INSERT INTO entretiens (id_vehicule, type_entretien, date_entretien, kilometrage, cout, description)
VALUES
(3, 'Révision', '2026-08-20', 12000, 250000, 'Révision des 10000km + changement plaquettes de frein');

INSERT INTO carburant (id_vehicule, date_plein, quantite_litres, cout, kilometrage)
VALUES
(1, '2026-09-01', 60, 210000, 44500),
(2, '2026-09-05', 45, 157500, 87500);

INSERT INTO documents_administratifs (id_vehicule, type_document, date_debut, date_expiration, reference)
VALUES
(1, 'Assurance', '2026-01-01', '2026-12-31', 'ASS-2026-0456'),
(1, 'Visite technique', '2026-03-15', '2026-10-01', 'VT-2026-1122'),
(2, 'Assurance', '2026-06-01', '2026-09-20', 'ASS-2026-0789');
