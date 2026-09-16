package com.example.ecommerceapp.agent.tools;

import com.example.ecommerceapp.model.Produit;
import com.example.ecommerceapp.repository.ProduitRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductTools {

    private final ProduitRepository produitRepository;

    public ProductTools(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }


    @Tool(
            name= "rechercherProduits" ,
            description = "Recherche des produits dans le catalogue par mot-clé (nom ou description)."
    )
    public List<Produit> rechercherProduits(String query){

        System.out.println("🤖 [Tool Call] Recherche de produits pour le mot-clé : " + query);
        if (query == null || query.isBlank()) {
            return produitRepository.findAll();
        }
        String q = query.toLowerCase();

        return produitRepository.findAll().stream()
                .filter(p -> (p.getNom() != null && p.getNom().toLowerCase().contains(q)) ||
                        (p.getDescription() != null && p.getDescription().toLowerCase().contains(q)))
                .toList();

    }

    @Tool(
            name = "obtenirDetailsProduit",
            description = "Obtient les détails complets d'un produit spécifique via son identifiant ID (prix, quantité en stock, description, image)."
    )
    public Produit obtenirDetailsProduit(Long productId) {
        System.out.println("🤖 [Tool Call] Obtention des détails du produit ID : " + productId);
        return produitRepository.findById(productId).orElse(null);
    }

    @Tool(
            name = "verifierStock",
            description = "Vérifie le niveau de stock exact disponible pour un produit donné via son ID."
    )
    public String verifierStock(Long productId) {
        System.out.println("🤖 [Tool Call] Vérification du stock pour le produit ID : " + productId);
        return produitRepository.findById(productId)
                .map(p -> "Le produit '" + p.getNom() + "' a actuellement " + p.getQuantiteStock() + " unités en stock.")
                .orElse("Produit non trouvé avec l'ID : " + productId);
    }

}
