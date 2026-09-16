package com.example.ecommerceapp.agent.tools;

import com.example.ecommerceapp.model.Client;
import com.example.ecommerceapp.model.Commande;
import com.example.ecommerceapp.model.Produit;
import com.example.ecommerceapp.repository.CommandeRepository;
import com.example.ecommerceapp.repository.ProduitRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartTools {

    private final ProduitRepository produitRepository;
    private final CommandeRepository commandeRepository;

    public CartTools(ProduitRepository produitRepository, CommandeRepository commandeRepository) {
        this.produitRepository = produitRepository;
        this.commandeRepository = commandeRepository;
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



}