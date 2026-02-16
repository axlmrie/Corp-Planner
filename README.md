# 🏢 Corp Planner API

[![codecov](https://codecov.io/gh/axlmrie/Corp-Planner/graph/badge.svg?token=FT4AEV8ON8)](https://codecov.io/gh/axlmrie/Corp-Planner)
[![Java CI with Maven](https://github.com/axlmrie/Corp-Planner/actions/workflows/maven.yml/badge.svg)](https://github.com/axlmrie/Corp-Planner/actions/workflows/maven.yml)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-green)

> **Une solution backend robuste pour la gestion centralisée et la réservation de ressources d'entreprise.**

## 📋 À propos du projet

**Corp Planner** est une API REST sécurisée conçue pour simplifier la logistique interne des entreprises. Elle permet aux employés de réserver des ressources partagées (salles de réunion, véhicules, matériel) tout en offrant aux administrateurs un contrôle total sur le parc matériel.

Ce projet met l'accent sur la **qualité du code**, la **sécurité** et l'**automatisation** via une chaîne CI/CD complète.

### ✨ Fonctionnalités Clés

* 🔒 **Sécurité Avancée :** Authentification JWT via Cookies `HttpOnly` (protection XSS) et gestion fine des rôles (`USER` vs `ADMIN`).
* 📅 **Moteur de Réservation :** Algorithme de gestion des conflits de créneaux et validation temporelle stricte.
* 📧 **Notifications :** Système d'envoi d'emails transactionnels (activation de compte, confirmation) via API tierce.
* ✅ **Qualité Industrielle :** Couverture de tests élevée (**>90%**) sur les modules critiques (Réservation, Utilisateurs), validée par **JaCoCo** et **Codecov**.

---

## 🛠️ Stack Technique

Ce projet utilise les standards modernes de l'écosystème Java :

| Domaine | Technologies |
| :--- | :--- |
| **Langage & Framework** | Java 17, Spring Boot 3, Spring Security |
| **Base de Données** | PostgreSQL (Prod), H2 (Test), Spring Data JPA |
| **Tests & Qualité** | JUnit 5, Mockito, **JaCoCo**, MockMvc |
| **CI / CD** | **GitHub Actions**, Codecov |
| **Outils** | Maven, Lombok, Swagger (OpenAPI 3), Docker |

---

## 🚀 Installation et Démarrage

Suivez ces étapes pour lancer l'API localement en quelques minutes.

### 📋 Prérequis

Assurez-vous d'avoir installé :
- **Java 17** (ou version supérieure)
- **Maven** 3.8+
- **PostgreSQL** (optionnel si vous utilisez le profil H2 pour le test rapide)
- **Git**

### 1️⃣ Cloner le dépôt

```bash
git clone https://github.com/axlmrie/Corp-Planner.git
cd Corp-Planner
```


### 2️⃣ Configuration (Variables d'environnement)

Pour des raisons de sécurité, les clés API et les mots de passe ne sont pas versionnés.

Créez un fichier `src/main/resources/application.properties` (ou utilisez des variables d'environnement) avec les informations suivantes :

```properties
# --- Base de données (PostgreSQL) ---
spring.datasource.url=jdbc:postgresql://localhost:5432/corp_planner_db
spring.datasource.username=postgres
spring.datasource.password=VOTRE_MOT_DE_PASSE

# --- Sécurité (JWT) ---
application.security.jwt.secret-key=VOTRE_CLE_SECRETE_TRES_LONGUE
application.security.jwt.expiration=86400000

# --- Emailing (Mailjet) ---
mailjet.api-key=VOTRE_API_KEY_MAILJET
mailjet.secret-key=VOTRE_SECRET_KEY_MAILJET
```

> **Note :** Pour les tests unitaires (`mvn test`), un fichier `application-test.properties` utilisant une base de données en mémoire (H2) est déjà configuré automatiquement. Vous n'avez rien à faire pour lancer les tests.

### 3️⃣ Compiler et Lancer

Une fois configuré, installez les dépendances et démarrez le serveur :

```bash
mvn clean install
mvn spring-boot:run
```

L'application sera accessible sur **http://localhost:8080**.

---

## 📚 Documentation API (Swagger UI)

Une interface interactive est disponible pour explorer et tester les endpoints sans avoir à configurer Postman.

Une fois l'application lancée, accédez à :

👉 **http://localhost:8080/swagger-ui/index.html**

### Fonctionnalités du Swagger

- 🟢 Visualiser tous les contrôleurs (Auth, Users, Bookings, Resources)
- 🔓 S'authentifier via le bouton **Authorize** (simule le Token JWT)
- 🧪 Tester les requêtes en temps réel

```

Les modifications principales :
- Utilisation de `-` au lieu de `*` pour les listes (plus standard en markdown)
- Correction du lien GitHub (suppression des crochets inutiles)
- Mise en forme du chemin de fichier avec des backticks
- Transformation de la note en blockquote avec `>`
- Structuration de la section Swagger avec des sous-listes
- Mise en évidence des URLs en gras
- Formatage cohérent des commandes bash

---


## 🧪 Tests et Qualité

La fiabilité de l'API est garantie par une suite de tests automatisés couvrant plus de **90%** du code métier critique.

### Lancer les tests

Pour exécuter l'intégralité des tests unitaires et d'intégration :

```bash
mvn clean verify

```

> **Note :** Cette commande génère également le rapport de couverture de code **JaCoCo**.

### Consulter le rapport de couverture

Une fois les tests terminés, vous pouvez consulter le rapport détaillé HTML localement :
Ouvrez le fichier : `target/site/jacoco/index.html` dans votre navigateur.

La stratégie de test repose sur :

* **JUnit 5 & Mockito** : Pour les tests unitaires isolés (Services).
* **MockMvc** : Pour les tests d'intégration des contrôleurs (API).
* **H2 Database** : Pour simuler la base de données sans impacter l'environnement de développement.

---

## 🏗️ Architecture du Projet

Le projet suit une architecture en couches classique et maintenable (**Layered Architecture**), facilitant l'évolution future.

```text
src/main/java/fr/axel/corpplanner
├── auth          # Logique d'authentification & Inscription
├── config        # Configuration globale (Swagger, Cors, Beans)
├── security      # Filtres JWT et Configuration Spring Security
├── exception     # Gestionnaire global d'erreurs (GlobalExceptionHandler)
├── user          # Domaine Utilisateur (Profile, Rôles)
├── resource      # Domaine Ressources (Salles, Matériel)
├── booking       # Domaine Réservation (Logique métier complexe)
└── email         # Service d'envoi d'emails (Mailjet)

```

Chaque module métier (`user`, `booking`, `resource`) est structuré ainsi :

* **Controller** : Point d'entrée REST.
* **Service** : Logique métier.
* **Repository** : Accès aux données (JPA).
* **DTO** : Objets de transfert de données (Records Java).
* **Mapper** : Conversion Entity <-> DTO.

---

## 👤 Auteur

**Axel Marie**

* [GitHub](https://github.com/axlmrie)
* [LinkedIn](https://www.linkedin.com/in/axel-marie/)

---
