# 🛒 Full-Stack E-Commerce Platform with AI Agent & RAG (Spring Boot 3 & Angular 17)

Plateforme e-commerce professionnelle, robuste et intelligente. Elle combine une API REST Spring Boot 3, une base de données relationnelle PostgreSQL, une base vectorielle Qdrant dédiée au RAG (Retrieval-Augmented Generation), et un Agent Virtuel intelligent (Spring AI) couplé à une application Angular 17 Standalone avec rendu fluide et réactif.

## 🛠️ Tech Stack & Architecture

### Back-End (Spring Boot 3 & AI Framework)
- Java 17+ & Spring Boot 3 (Spring Web, Spring Security)
- Spring AI (Architecture d'agents conversationnels et Function Calling / `@Tool`)
- OpenRouter API (Support d'embeddings `nvidia/nemotron-3-embed-1b` & LLM conversationnel)
- Qdrant Vector Store (Base de données vectorielle hébergée via Docker pour la recherche sémantique RAG)
- Spring Data JPA & Hibernate
- PostgreSQL (Base de données relationnelle)
- Spring AI PDF Reader (`PagePdfDocumentReader` & `TokenTextSplitter` pour l'ingestion automatique de la documentation)
- JWT (JSON Web Token) (Authentification stateless & Role-Based Access Control)
- Maven

### Front-End (Angular 17+)
- Angular 17+ (Architecture Standalone Components)
- RxJS & BehaviorSubject (Gestion de l'état réactif, du panier local et du Chatbot)
- Chatbot UI Interactive (Prise en charge des balises d'action `[ACTION:ADD_TO_CART]` et `[ACTION:CLEAR_CART]`)
- Angular Router & HTTP Interceptor (Injection automatique du Token JWT)
- SSR / Platform Checks (Compatibilité Node.js et navigateur)

## 🤖 Architecture de l'Agent IA & RAG (Spring AI)

L'application intègre un Agent Virtuel conversationnel e-commerce capable à la fois d'exécuter des actions métier (panier, profil, création de commande) et d'interroger la base de connaissances documentaire en temps réel.



[ 💬 Question / Ordre du Client ]
│
▼
[ 🤖 Agent Spring AI ]
│
┌──────────────────────┴──────────────────────┐
▼                                             ▼
┌───────────────────────────────┐             ┌───────────────────────────────┐
│     🛠️ Action Tools           │             │      📚 Knowledge Tools       │
│  (CartTools, UserTools)       │             │       (RAG Tool)              │
├───────────────────────────────┤             ├───────────────────────────────┤
│ • obtenirProfilClientActuel    │             │ • rechercherDocumentation     │
│ • preparerAjoutPanier         │             │   └── Similarity Search       │
│ • suivreCommande              │             │       (Qdrant VectorStore)    │
│ • creerCommandeDirecte (COD)  │             └───────────────┬───────────────┘
└───────────┬───────────────────┘                             │
│                                                 │
▼                                                 ▼
[ 🗄️ PostgreSQL (JPA) ]                          [ ⚡ Qdrant (Embeddings) ]


### Principes Clés de l'Agent :

- **RAG en tant que Tool** (`rechercherDocumentation`) : Au lieu d'interroger la base vectorielle à chaque message, le RAG est encapsulé sous forme d'un `@Tool`. Le LLM décide d'invoquer la base vectorielle uniquement lorsqu'une question concerne les politiques de livraison, les délais, les garanties ou les retours.
- **Prévention des Doublons Clients** : Lors de la création d'une commande via `creerCommandeDirecte`, l'outil vérifie d'abord l'identité du client connecté (`SecurityContextHolder`) ou recherche une correspondance par téléphone/nom avant toute tentative d'insertion en BDD.
- **Paiement Cash on Delivery (COD Maroc)** : Gestion spécifique des commandes avec paiement à la livraison via AMANA (Barid Al-Maghrib) sous 48h, support client dédié au 0657484489, et gratuité des frais de port dès 500 MAD d'achat.

## 📐 Modèle de Données & Entités Core

L'application repose sur une architecture N-Tiers (Controller -> Service -> Repository -> Entity) :

- **User / AppUser** : Informations de compte, rôle (`ROLE_USER`, `ROLE_ADMIN`) et authentification JWT.
- **Categorie** : Libellé et regroupement logique des produits.
- **Produit** : Nom, Prix, Quantité en Stock, description (TEXT), imageUrl et association à une Categorie.
- **Client** : Profil client (Nom, Prénom, Adresse, Téléphone, Email) relié 1:1 à un User.
- **Commande** : Date, Statut (`EN_ATTENTE`, `EXPÉDIÉ`, etc.), Total et association au Client.
- **LigneCommande** : Snapshot du produit, prix unitaire et quantité demandée lors du checkout.
- **Qdrant Collection** (`ecommerce_docs`) : Découpage sémantique (chunks PDF) et représentation vectorielle (4096 dimensions via Nemotron-3-Embed).

## ✨ Fonctionnalités Clés Implémentées

### 🤖 Assistant Virtuel Conversationnel & RAG

- **Consultation RAG Instantanée** : Réponses précises sur les règles de livraison AMANA (48h), la politique de retour (14 jours), les produits endommagés (signalement 24h) et le numéro d'assistance (0657484489).
- **Prise de Commande Conversationnelle** :
  - Récupération automatique du profil enregistré via `obtenirProfilClientActuel`.
  - Demande de confirmation de l'adresse sans ressaisie inutile.
  - Validation de la commande directement depuis le tchat via `creerCommandeDirecte`.
- **Vidage Automatique du Panier** : Envoi de la balise `[ACTION:CLEAR_CART]` au frontend Angular dès la confirmation d'une commande pour réinitialiser le panier local.
- **Ajout au Panier Guidé** : Envoi de la balise `[ACTION:ADD_TO_CART:PRODUIT_JSON:QUANTITE]` pour incrémenter le panier utilisateur depuis le tchat.

### 🌐 Espace Public & Client

- **Navigation Libre** : Catalogue produits avec affichage dynamique, filtres par catégorie et barre de recherche.
- **Panier Réactif** : Synchronisation BehaviorSubject et persistance dans le localStorage.
- **Checkout Sécurisé (COD)** : Passage de commande en espèces à la livraison avec décrémentation automatique des stocks et validation d'adresse.
- **Dashboard Client** : Consultation du profil et suivi des commandes passées (`suivreCommande`).

### ⚙️ Espace Administrateur

- **Protection par Guard** (`adminGuard`) : Accès restreint au rôle `ROLE_ADMIN`.
- **Gestion du Catalogue (CRUD)** : Ajout, modification, gestion des stocks, téléversement d'images (`/uploads`) avec aperçu dynamique.

## 🌐 Endpoints de l'API REST Back-End

### 🤖 Agent IA (`/api/chat`)
| Méthode | Endpoint | Description |
|---|---|---|
| `POST` | `/api/chat` | Interroger l'agent virtuel (Prend une question textuelle et un `conversationId`, retourne la réponse du LLM enrichie des appels d'outils BDD/RAG). |

### 🔐 Authentification (`/auth`)
| Méthode | Endpoint | Description |
|---|---|---|
| `POST` | `/auth/register` | Inscription d'un nouveau compte client. |
| `POST` | `/auth/login` | Authentification et génération du Token JWT. |

### 🖼️ Gestion des Images & Médias (`/api/images`)
| Méthode | Endpoint | Description |
|---|---|---|
| `POST` | `/api/images/upload` | Téléverser une image locale (`multipart/form-data`). |
| `GET` | `/api/images/{fileName}` | Accéder et afficher l'image stockée physiquement (Public). |

### 📁 Catégories (`/categories`)
| Méthode | Endpoint | Rôle |
|---|---|---|
| `GET` | `/categories` | Public |
| `POST` / `PUT` / `DELETE` | `/categories/{id}` | `ROLE_ADMIN` |

### 📦 Produits (`/produits`)
| Méthode | Endpoint | Rôle |
|---|---|---|
| `GET` | `/produits` | Lister tous les produits |
| `GET` | `/produits/categorie/{id}` | Filtrer les produits par catégorie |
| `POST` / `PUT` / `DELETE` | `/produits/{id}` | `ROLE_ADMIN` |

### 🛒 Commandes & Clients (`/commandes`, `/clients`)
| Méthode | Endpoint | Description |
|---|---|---|
| `POST` | `/commandes` | Enregistrer une commande (Authentifié). |
| `GET` | `/commandes/client/{clientId}` | Obtenir l'historique des commandes d'un client. |
| `GET` | `/commandes` | Consulter toutes les commandes du système (`ROLE_ADMIN`). |

## 🚀 Démarrage Rapide du Projet

Pour exécuter l'ensemble de la plateforme en environnement de développement :

### 1. Démarrer Qdrant (Base Vectorielle)

```bash
docker run -d --name qdrant -p 6333:6333 -p 6334:6334 qdrant/qdrant
```

*(Si le conteneur a déjà été créé, faites simplement `docker start qdrant`)*

### 2. Lancer le Back-End (Spring Boot)

Assurez-vous d'avoir configuré vos clés OpenRouter et l'accès PostgreSQL dans `application.properties`.

Vérifiez que le fichier `Politiques_Livraison_COD_Maroc.pdf` est présent sous `src/main/resources/docs/`.

Lancez l'application via votre IDE ou en ligne de commande :

```bash
mvn spring-boot:run
```

*(Au démarrage, `VectorStoreInitializer` va lire le PDF, générer les embeddings et alimenter Qdrant)*

### 3. Lancer le Front-End (Angular)

```bash
cd ecommerce-frontend
npm install
ng serve
```

Rendez-vous sur **http://localhost:4200** pour interagir avec l'application et l'agent conversationnel !