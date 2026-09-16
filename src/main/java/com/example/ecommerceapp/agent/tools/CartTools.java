package com.example.ecommerceapp.agent.tools;

import com.example.ecommerceapp.agent.dto.CartItemDTO;
import com.example.ecommerceapp.model.*;
import com.example.ecommerceapp.repository.ClientRepository;
import com.example.ecommerceapp.repository.CommandeRepository;
import com.example.ecommerceapp.repository.ProduitRepository;
import jakarta.transaction.Transactional;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartTools {

    private final ProduitRepository produitRepository;
    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;

    public CartTools(ProduitRepository produitRepository, CommandeRepository commandeRepository, ClientRepository clientRepository) {
        this.produitRepository = produitRepository;
        this.commandeRepository = commandeRepository;
        this.clientRepository = clientRepository;
    }

    @Tool(
            name = "calculerPrixTotal",
            description = "Calcule le prix total pour une quantité donnée d'un produit spécifique via son ID."
    )
    public String calculerPrixTotal(Long productId, int quantite) {
        System.out.println("🤖 [Tool Call] Calcul du prix total pour le produit #" + productId + " x" + quantite);
        Optional<Produit> produitOpt = produitRepository.findById(productId);
        if (produitOpt.isEmpty()) {
            return "Produit non trouvé avec l'ID : " + productId;
        }
        Produit p = produitOpt.get();
        double total = p.getPrix() * quantite;
        return "Le total pour " + quantite + " x '" + p.getNom() + "' est de " + total + " DHS.";
    }

    @Tool(
            name = "suivreCommande",
            description = "Récupère le statut et le détail d'une commande passée via son ID."
    )
    public String suivreCommande(Long commandeId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "NON_AUTORISE: Vous devez être connecté à votre compte client pour suivre une commande.";
        }

        String currentPrincipal = auth.getName(); // Contient "Imane" dans ton log

        Optional<Commande> commandeOpt = commandeRepository.findById(commandeId);
        if (commandeOpt.isEmpty()) {
            return "Aucune commande trouvée avec l'ID #" + commandeId;
        }

        Commande commande = commandeOpt.get();
        Client client = commande.getClient();

        // 🔑 DOUBLE VÉRIFICATION : On vérifie par Username ET par Email !
        boolean estProprietaire = false;

        if (client != null) {
            // 1. Comparaison avec l'email du client (ex: imane@gmail.com)
            boolean matchEmail = client.getEmail() != null && client.getEmail().equalsIgnoreCase(currentPrincipal);

            // 2. Comparaison avec le username de l'entité User liée au client (ex: Imane)
            boolean matchUsername = client.getUser() != null && client.getUser().getUsername() != null
                    && client.getUser().getUsername().equalsIgnoreCase(currentPrincipal);

            estProprietaire = matchEmail || matchUsername;
        }

        boolean estAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!estProprietaire && !estAdmin) {
            return "ACCES_REFUSE: Vous n'avez pas la permission de consulter la commande #" + commandeId + ". Elle n'appartient pas à votre compte.";
        }

        return String.format(java.util.Locale.US,
                "Commande #%d - Date : %s - Statut : %s - Montant Total : %.2f DHS.",
                commande.getId(), commande.getDateCommande(), commande.getStatut(), commande.getTotal()
        );
    }


    @Tool(
            name = "obtenirHistoriqueCommandes",
            description = "Récupère la liste globale de toutes les commandes passées par le client connecté."
    )
    public String obtenirHistoriqueCommandes() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "NON_AUTORISE: Vous devez être connecté à votre compte client pour consulter votre historique de commandes.";
        }

        String currentPrincipal = auth.getName();
        System.out.println("🤖 [Tool Call] Récupération de l'historique pour : " + currentPrincipal);

        // Récupération de toutes les commandes et filtrage par l'utilisateur/client connecté
        List<Commande> mesCommandes = commandeRepository.findAll().stream()
                .filter(c -> c.getClient() != null && (
                        (c.getClient().getEmail() != null && c.getClient().getEmail().equalsIgnoreCase(currentPrincipal)) ||
                                (c.getClient().getUser() != null && c.getClient().getUser().getUsername() != null &&
                                        c.getClient().getUser().getUsername().equalsIgnoreCase(currentPrincipal))
                ))
                .collect(Collectors.toList());

        if (mesCommandes.isEmpty()) {
            return "HISTORIQUE_VIDE: Vous n'avez encore passé aucune commande sur notre boutique.";
        }

        // Formatage clair des commandes pour le LLM
        StringBuilder sb = new StringBuilder("HISTORIQUE_COMMANDES:\n");
        for (Commande c : mesCommandes) {
            sb.append(String.format(java.util.Locale.US,
                    "- Commande #%d | Date : %s | Statut : %s | Total : %.2f DHS\n",
                    c.getId(), c.getDateCommande().toString(), c.getStatut(), c.getTotal()
            ));
        }
        return sb.toString();
    }

    @Tool(
            name = "preparerAjoutPanier",
            description = "Prépare l'ajout d'un produit au panier du client en vérifiant sa disponibilité et son ID."
    )
    public String preparerAjoutPanier(Long productId, int quantite) {
        System.out.println("🤖 [Tool Call] Préparation ajout panier : Produit #" + productId + " x" + quantite);
        return produitRepository.findById(productId)
                .map(p -> {
                    if (p.getQuantiteStock() < quantite) {
                        return "STOCK_INSUFFISANT: Il ne reste que " + p.getQuantiteStock() + " unités de " + p.getNom();
                    }

                    String catJson = (p.getCategorie() != null)
                            ? "{\"id\":" + p.getCategorie().getId() + ",\"nom\":\"" + p.getCategorie().getNom().replace("\"", "\\\"") + "\"}"
                            : "null";

                    String imgUrl = (p.getImageUrl() != null) ? p.getImageUrl() : "";

                    // 🔑 Utilisation de Locale.US pour forcer le POINT décimal (ex: 8500.00 et non 8500,00)
                    String produitJson = String.format(java.util.Locale.US,
                            "{\"id\":%d,\"nom\":\"%s\",\"prix\":%.2f,\"quantiteStock\":%d,\"imageUrl\":\"%s\",\"categorie\":%s}",
                            p.getId(), p.getNom().replace("\"", "\\\""), p.getPrix(), p.getQuantiteStock(), imgUrl, catJson
                    );

                    return "ADD_TO_CART_SUCCESS:" + produitJson + "|QUANTITE:" + quantite;
                })
                .orElse("PRODUIT_INTROUVABLE: Aucun produit trouvé avec l'ID #" + productId);
    }


    @Transactional
    @Tool(
            name = "creerCommandeDirecte",
            description = "Crée et enregistre définitivement une commande en base de données avec les coordonnées du client et la liste des produits commandés."
    )
    public String creerCommandeDirecte(String nom, String prenom, String adresse, String telephone, List<CartItemDTO> articles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("🤖 [Tool Call] Création de commande pour : " + (auth != null ? auth.getName() : "Anonyme"));

        if (articles == null || articles.isEmpty()) {
            return "ERREUR_PANIER_VIDE: Impossible de passer une commande avec un panier vide.";
        }

        Client client = null;

        // 1. RECHERCHE VIA L'UTILISATEUR CONNECTÉ (JWT)
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String currentPrincipal = auth.getName();

            client = clientRepository.findAll().stream()
                    .filter(c -> (c.getEmail() != null && c.getEmail().equalsIgnoreCase(currentPrincipal)) ||
                            (c.getUser() != null && c.getUser().getUsername() != null && c.getUser().getUsername().equalsIgnoreCase(currentPrincipal)) ||
                            (c.getUser() != null && c.getUser().getEmail() != null && c.getUser().getEmail().equalsIgnoreCase(currentPrincipal)))
                    .findFirst()
                    .orElse(null);
        }

        // 2. RECHERCHE DE SECOURS VIA LE TÉLÉPHONE OU LE NOM/PRÉNOM (Évite la création de doublon BDD)
        if (client == null && telephone != null && !telephone.isBlank()) {
            client = clientRepository.findAll().stream()
                    .filter(c -> (c.getTelephone() != null && c.getTelephone().replaceAll("\\s+", "").equals(telephone.replaceAll("\\s+", ""))) ||
                            (c.getNom() != null && c.getNom().equalsIgnoreCase(nom) && c.getPrenom() != null && c.getPrenom().equalsIgnoreCase(prenom)))
                    .findFirst()
                    .orElse(null);
        }

        // 3. SEULEMENT SI AUCUN CLIENT N'EXISTE, ON EN CRÉE UN NOUVEAU
        if (client == null) {
            System.out.println("➕ Aucun client existant trouvé. Création d'un nouveau profil Client.");
            client = new Client();
            client.setNom(nom);
            client.setPrenom(prenom);
            client.setAdresse(adresse);
            client.setTelephone(telephone);

            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                client.setEmail(auth.getName());
            }
            client = clientRepository.save(client);
        } else {
            // Mettre à jour l'adresse et le téléphone si besoin sur le client existant identifié
            System.out.println("✅ Client existant identifié (ID: " + client.getId() + ")");
            if (adresse != null && !adresse.isBlank()) client.setAdresse(adresse);
            if (telephone != null && !telephone.isBlank()) client.setTelephone(telephone);
            clientRepository.save(client);
        }

        // 4. CRÉATION DE LA COMMANDE
        Commande commande = new Commande();
        commande.setClient(client);
        commande.setDateCommande(LocalDateTime.now());
        commande.setStatut(StatusCommande.EN_ATTENTE);

        double totalGlobal = 0.0;
        List<LigneCommande> lignes = new ArrayList<>();

        for (CartItemDTO item : articles) {
            Optional<Produit> prodOpt = produitRepository.findById(item.getProductId());
            if (prodOpt.isPresent()) {
                Produit p = prodOpt.get();
                if (p.getQuantiteStock() < item.getQuantite()) {
                    return "STOCK_INSUFFISANT: Le produit '" + p.getNom() + "' n'a que " + p.getQuantiteStock() + " unités en stock.";
                }

                p.setQuantiteStock(p.getQuantiteStock() - item.getQuantite());
                produitRepository.save(p);

                LigneCommande lc = new LigneCommande();
                lc.setCommande(commande);
                lc.setProduit(p);
                lc.setQuantite(item.getQuantite());
                lc.setPrixUnitaire(p.getPrix());
                lignes.add(lc);

                totalGlobal += p.getPrix() * item.getQuantite();
            }
        }

        commande.setTotal(totalGlobal);
        commande.setLignes(lignes);
        Commande commandeSauvegardee = commandeRepository.save(commande);

        return String.format(java.util.Locale.US,
                "COMMANDE_SUCCES:{\"commandeId\":%d,\"total\":%.2f}",
                commandeSauvegardee.getId(), totalGlobal
        );
    }
}