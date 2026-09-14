package com.example.ecommerceapp.agent.tools;

import com.example.ecommerceapp.model.Commande;
import com.example.ecommerceapp.model.Produit;
import com.example.ecommerceapp.repository.CommandeRepository;
import com.example.ecommerceapp.repository.ProduitRepository;
import org.springframework.ai.tool.annotation.Tool;
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
            description = "Récupère le statut actuel et le détail d'une commande passée via son ID de commande."
    )
    public String suivreCommande(Long commandeId) {
        System.out.println("🤖 [Tool Call] Suivi de la commande #" + commandeId);
        Optional<Commande> commandeOpt = commandeRepository.findById(commandeId);
        if (commandeOpt.isEmpty()) {
            return "Aucune commande trouvée avec l'ID #" + commandeId;
        }
        Commande c = commandeOpt.get();
        return "Commande #" + c.getId() + " - Date : " + c.getDateCommande() +
                " - Statut : " + c.getStatut() + " - Montant Total : " + c.getTotal() + " DHS.";
    }
}