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