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
* **Java 17** (ou version supérieure)
* **Maven** 3.8+
* **PostgreSQL** (Optionnel si vous utilisez le profil H2 pour le test rapide)
* **Git**

### 1️⃣ Cloner le dépôt

```bash
git clone [https://github.com/axlmrie/Corp-Planner.git](https://github.com/axlmrie/Corp-Planner.git)
cd Corp-Planner
```
### 2️⃣ Configuration (Variables d'environnement)

Pour des raisons de sécurité, les clés API et les mots de passe ne sont pas versionnés.
Créez un fichier src/main/resources/application.properties (ou utilisez des variables d'environnement) avec les informations suivantes :

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