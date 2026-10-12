# Gestion d'un Parc Automobile

Application desktop Java (JavaFX) de gestion d'un parc de véhicules d'entreprise —
projet réalisé dans le cadre de l'UE Java L2 (Sujet n°28).

## Présentation

L'application permet de gérer l'ensemble du cycle de vie d'un parc automobile :
véhicules, conducteurs, affectations (qui conduit quoi), historique des
entretiens et des pleins de carburant, et suivi des documents administratifs
(assurance, contrôle technique, vignette...) avec alertes de péremption.

## Fonctionnalités

- **Tableau de bord** : statistiques en temps réel, graphique de répartition
  des statuts, alertes sur les documents arrivant à échéance sous 30 jours.
- **Véhicules** : liste, recherche, filtre par statut, ajout / modification /
  suppression avec validation complète des champs.
- **Conducteurs** : liste, recherche, ajout / modification / suppression.
- **Affectations** : attribution d'un véhicule à un conducteur, avec
  vérification automatique des disponibilités (voir "Règles métier" ci-dessous).
- **Entretiens & Carburant** : historique des interventions et des pleins,
  avec calcul automatique des coûts totaux.
- **Documents administratifs** : suivi des échéances avec code couleur selon
  l'urgence (vert = valide, orange = bientôt expiré, rouge = expiré).
- Recherche instantanée, messages de confirmation/erreur, persistance
  complète des données dans une base MySQL.

## Règles métier et statuts automatiques

Le statut d'un véhicule (`Disponible`, `En service`, `En entretien`,
`Hors service`) n'est **plus un champ du formulaire Véhicule** : il n'est
donc plus possible de se tromper en le saisissant à la main.

- À la création, un véhicule est **toujours "Disponible" par défaut**.
- **Un véhicule "En entretien" ou "Hors service" ne peut pas être affecté**
  à un conducteur tant que son statut n'a pas été remis à "Disponible".
- **Un véhicule déjà en mission** (affectation en cours, sans date de fin)
  ne peut pas être affecté une seconde fois : il faut d'abord clôturer
  l'affectation existante (renseigner sa date de fin).
- **Un conducteur déjà en mission** ne peut pas être affecté à un autre
  véhicule en même temps.
- Quand une affectation devient active (pas de date de fin), le véhicule
  concerné passe **automatiquement** à `En service`.
- Quand une affectation est clôturée (date de fin renseignée) ou supprimée,
  le véhicule concerné repasse **automatiquement** à `Disponible`.
- Modifier un véhicule existant (marque, kilométrage, etc.) via son
  formulaire ne touche jamais à son statut : celui-ci reste entièrement
  piloté par le cycle de vie des affectations.
- **Le kilométrage du véhicule se synchronise automatiquement** : à chaque
  entretien ou plein de carburant enregistré, si le kilométrage saisi est
  supérieur à celui actuellement connu pour le véhicule, ce dernier est mis
  à jour automatiquement (le compteur ne peut jamais reculer : une saisie
  inférieure au kilométrage actuel est rejetée avec un message explicite).
- **Une affectation ne peut pas démarrer avant la date d'acquisition** du
  véhicule concerné (contrôle relationnel entre les écrans Véhicule et
  Affectation).

## Validation des champs

Chaque formulaire contrôle ses champs obligatoires, leurs formats (nombres,
dates), et applique des règles de dates **différentes selon le contexte** :

| Formulaire | Règle de date appliquée |
|---|---|
| Véhicule (date d'acquisition) | Ne peut pas être une date déjà passée (aujourd'hui ou plus tard). Sélection bloquée directement dans le calendrier. |
| Conducteur (date d'embauche) | Ne peut pas être une date déjà passée (aujourd'hui ou plus tard). Sélection bloquée directement dans le calendrier. |
| Affectation (date de début) | Doit être postérieure à l'an 2000, et pas à plus d'un an dans le futur |
| Entretien / Carburant (date) | Ne peut pas être dans le futur (on ne logue que des événements passés) |
| Document administratif (expiration) | Peut être dans le passé (c'est ce qui déclenche l'alerte), bornée entre 1950 et +20 ans |

Les doublons (immatriculation, numéro de permis) sont détectés et affichent
un message clair plutôt qu'une erreur technique. De la même façon, le
kilométrage saisi lors d'un entretien ou d'un plein ne peut pas être
inférieur au kilométrage déjà enregistré pour le véhicule, et le numéro de
téléphone d'un conducteur doit contenir exactement 10 chiffres.

## Technologies utilisées

- **Java 17**
- **JavaFX 21** (FXML + CSS) pour l'interface graphique
- **MySQL** (via le pilote JDBC `mysql-connector-j`) pour la persistance des données
- **Maven** pour la gestion du projet et des dépendances
- Architecture **MVC** : `model` (objets métier), `dao` (accès base de données
  + règles de cohérence comme les vérifications de disponibilité),
  `controller` (logique des écrans FXML, validations, statuts automatiques)

## Prérequis

- JDK 17 ou supérieur installé
- Maven 3.8+ installé
- **Un serveur MySQL démarré** (voir ci-dessous)
- Une connexion Internet lors du premier `mvn` (téléchargement des dépendances
  JavaFX et MySQL Connector/J depuis Maven Central)

## Installer et démarrer MySQL

Le plus simple pour un projet étudiant : installer **XAMPP**
(https://www.apachefriends.org) ou **WAMP** (Windows), qui incluent MySQL
(ou MariaDB, compatible) préconfiguré avec un utilisateur `root` sans mot
de passe — exactement ce que le projet attend par défaut.

1. Installez XAMPP / WAMP (ou MySQL Server seul si vous préférez)
2. Démarrez le service **MySQL** depuis le panneau de contrôle XAMPP/WAMP
3. C'est tout — l'application crée elle-même la base et les tables au
   premier lancement (voir plus bas)

Si votre utilisateur MySQL n'est pas `root` sans mot de passe, modifiez les
constantes en haut de
`src/main/java/com/parcauto/dao/DatabaseConnection.java` :

```java
private static final String UTILISATEUR = "root";
private static final String MOT_DE_PASSE = "";
```

## Installation et lancement de l'application

```bash
# 1. Se placer dans le dossier du projet
cd gestion-parc-automobile

# 2. Vérifier que MySQL est démarré (XAMPP/WAMP -> bouton "Start" sur MySQL)

# 3. Compiler et lancer l'application
mvn clean javafx:run
```

Au premier lancement, l'application se connecte à MySQL, **crée
automatiquement la base `parc_auto`** si elle n'existe pas, puis crée les
6 tables à partir du script `src/main/resources/db/schema.sql` (structure +
quelques données d'exemple pour tester l'application immédiatement).

### Générer un exécutable (JAR)

```bash
mvn clean package
java -jar target/gestion-parc-automobile.jar
```

## Structure du projet

```
src/main/java/com/parcauto/
├── MainApp.java              → point d'entrée de l'application
├── model/                    → objets métier (Vehicule, Conducteur, ...)
├── dao/                      → accès base de données + règles de cohérence
│                                (ex : AffectationDAO détecte les conflits
│                                de disponibilité véhicule/conducteur)
└── controller/                → logique des écrans (1 contrôleur par écran
                                 FXML), validations, mise à jour automatique
                                 des statuts

src/main/resources/
├── fxml/                     → mise en page de chaque écran
├── css/style.css             → thème graphique de l'application
└── db/schema.sql             → script de création de la base de données
```

## Base de données

6 tables : `vehicules`, `conducteurs`, `affectations`, `entretiens`,
`carburant`, `documents_administratifs`, dans une base MySQL nommée
`parc_auto`. Le détail des colonnes et des relations se trouve dans
`src/main/resources/db/schema.sql`. Vous pouvez aussi explorer la base avec
phpMyAdmin (inclus dans XAMPP/WAMP) une fois l'application lancée au moins
une fois.

## Difficultés rencontrées / choix techniques

- **Dates en VARCHAR plutôt qu'en type DATE MySQL** : pour garder le code
  Java simple (les dates restent de simples chaînes `"YYYY-MM-DD"` partout,
  sans conversion vers `java.sql.Date`).
- **Statuts synchronisés automatiquement** : plutôt que de laisser le champ
  "statut" d'un véhicule librement modifiable (source d'incohérences si on
  oublie de le remettre à jour), sa valeur "En service" est désormais
  entièrement pilotée par le cycle de vie des affectations.
- **Contrôle de disponibilité au niveau du DAO** : `AffectationDAO` expose
  des méthodes dédiées (`vehiculeADejaUneAffectationActive`,
  `conducteurADejaUneAffectationActive`) interrogées par le formulaire avant
  toute validation, pour éviter les doubles affectations.
- **Création automatique de la base au premier lancement** : `DatabaseConnection`
  se connecte d'abord à MySQL sans préciser de base, exécute
  `CREATE DATABASE IF NOT EXISTS`, puis crée les tables si elles n'existent
  pas encore.
- **Boîtes de dialogue modales pour les formulaires** : plutôt que de changer
  d'écran pour ajouter/modifier une entrée, chaque formulaire s'ouvre dans une
  fenêtre modale (`Stage`), ce qui garde le tableau visible en arrière-plan.

