# 🛒 E-Commerce Back-End API (Spring Boot 3 & PostgreSQL)

API REST complète et professionnelle pour une plateforme d'e-commerce, développée avec **Spring Boot 3**, **Spring Data JPA**, et **PostgreSQL**.

---

## 🛠️ Tech Stack

* **Java 17+**
* **Spring Boot 3** (Spring Web, Spring Data JPA)
* **PostgreSQL**
* **Hibernate / JPA**
* **Jackson JSON** (Gestion des relations bidirectionnelles avec `@JsonManagedReference` & `@JsonBackReference`)
* **Maven**

---

## 📐 Architecture & Modèle de Données

L'application repose sur une architecture N-Tiers (Controller -> Service -> Repository -> Entity) et gère les entités suivantes :

* **`Categorie`** : Libellé et regroupement des produits.
* **`Produit`** : Nom, Prix, et association à une Categorie.
* **`Client`** : Nom, Email, et historique des commandes.
* **`Commande`** : Date, Statut (`EN_ATTENTE`, `VALIDEE`, `PAYEE`, `EXPEDIEE`, `LIVREE`, `ANNULEE`), Total, et association au Client.
* **`LigneCommande`** : Détail des articles commandés, quantités et prix unitaires enregistrés lors de la transaction.

---

## 🌐 Endpoints de l'API

### 📁 Catégories
* `GET /categories` - Lister toutes les catégories
* `POST /categories` - Créer une catégorie
* `PUT /categories/{id}` - Modifier une catégorie
* `DELETE /categories/{id}` - Supprimer une catégorie

### 📦 Produits
* `GET /produits` - Lister tous les produits
* `GET /produits/categorie/{id}` - Obtenir les produits d'une catégorie
* `POST /produits` - Créer un produit
* `PUT /produits/{id}` - Mettre à jour un produit
* `DELETE /produits/{id}` - Supprimer un produit

### 👤 Clients
* `POST /clients` - Enregistrer un nouveau client
* `GET /clients` - Lister tous les clients

### 🛒 Commandes
* `POST /commandes` - Validation du panier et enregistrement de la commande
* `GET /commandes` - Consulter l'historique de toutes les commandes

---

## 🧪 Exemple de Requête : Passer une Commande

**`POST /commandes`**

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
        }
    ]
}
