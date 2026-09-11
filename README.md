# 🛒 Full-Stack E-Commerce Platform (Spring Boot 3 & Angular 17)

Plateforme e-commerce professionnelle, robuste et sécurisée. Elle se compose d'une **API REST Spring Boot 3** reliée à **PostgreSQL** et d'une application **Angular 17 Standalone** avec rendu fluide et réactif.

---

## 🛠️ Tech Stack & Architecture

### **Back-End (Spring Boot 3)**
* **Java 17+** & **Spring Boot 3** (Spring Web, Spring Security)
* **Spring Data JPA** & **Hibernate**
* **PostgreSQL** (Base de données relationnelle)
* **JWT (JSON Web Token)** (Authentification stateless & Role-Based Access Control)
* **Maven**

### **Front-End (Angular 17+)**
* **Angular 17+** (Architecture **Standalone Components**)
* **RxJS** & **BehaviorSubject** (Gestion de l'état réactif et panier)
* **Angular Router** & **HTTP Interceptor** (Injection automatique du Token JWT)
* **SSR / Platform Checks** (Compatibilité Node.js et navigateur)

---

## 📐 Modèle de Données & Entités Core

L'application repose sur une architecture N-Tiers (`Controller -> Service -> Repository -> Entity`) :

* **User / AppUser** : Informations de compte, rôle (`ROLE_USER`, `ROLE_ADMIN`) et authentification.
* **Categorie** : Libellé et regroupement logique des produits.
* **Produit** : Nom, Prix, Quantité en Stock, et association à une `Categorie`.
* **Client** : Profil client avec coordonnées et historique d'achats.
* **Commande** : Date, Statut, Total et association au `Client`.
* **LigneCommande** : Snapshot du produit, prix unitaire et quantité demandée lors du checkout.

---

## ✨ Fonctionnalités Clés Implémentées

### 🌐 **Espace Public & Client**
* **Navigation Libre :** Consultation du catalogue produits et filtrage par catégorie sans authentification obligatoire.
* **Recherche & Pagination :** Recherche dynamique par mot-clé et navigation paginée par paquets de produits.
* **Panier Réactif :** Ajout d'articles, modification des quantités, contrôle des stocks max et persistance dans le `localStorage`.
* **Checkout Sécurisé :** Redirection vers l'authentification/inscription avant validation finale de la commande et décrémentation automatique du stock en BDD.
* **Dashboard Client :** Consultation du profil et historique détaillé de toutes les commandes passées.

### ⚙️ **Espace Administrateur**
* **Protection par Guard (`adminGuard`) :** Accès strictement restreint aux utilisateurs ayant le rôle `ROLE_ADMIN`.
* **Gestion du Catalogue (CRUD) :** Ajout, modification, réapprovisionnement des stocks et suppression de produits.

---

## 🌐 Endpoints de l'API REST Back-End

### 🔐 Authentification (`/auth`)
* `POST /auth/register` : Inscription d'un nouveau compte client.
* `POST /auth/login` : Authentification et génération du Token JWT.

### 📁 Catégories (`/categories`)
* `GET /categories` : Lister toutes les catégories (Public).
* `POST /categories` : Créer une catégorie (`ROLE_ADMIN`).
* `PUT /categories/{id}` : Modifier une catégorie (`ROLE_ADMIN`).
* `DELETE /categories/{id}` : Supprimer une catégorie (`ROLE_ADMIN`).

### 📦 Produits (`/produits`)
* `GET /produits` : Lister tous les produits (Public).
* `GET /produits/categorie/{id}` : Filtrer les produits par catégorie (Public).
* `POST /produits` : Créer un nouveau produit (`ROLE_ADMIN`).
* `PUT /produits/{id}` : Mettre à jour un produit / ajuster le stock (`ROLE_ADMIN`).
* `DELETE /produits/{id}` : Supprimer un produit (`ROLE_ADMIN`).

### 🛒 Commandes & Clients (`/commandes`, `/clients`)
* `POST /commandes` : Valider un panier et enregistrer la commande (Authentifié).
* `GET /commandes/client/{clientId}` : Obtenir l'historique des commandes d'un client.
* `GET /commandes` : Consulter toutes les commandes du système (`ROLE_ADMIN`).

---

## 🧪 Exemple de Payload JSON : Passer une Commande
e

`POST /api/commandes` (Header : `Authorization: Bearer <JWT_TOKEN>`)

```json
{
  "client": {
    "id": 1
  },
  "lignes": [
    {
      "quantite": 2,
      "produit": {
        "id": 1
      }
    },
    {
      "quantite": 1,
      "produit": {
        "id": 4
      }
    }
  ]
}