package com.example.ecommerceapp.agent.tools;

import com.example.ecommerceapp.model.Categorie;
import com.example.ecommerceapp.model.Produit;
import com.example.ecommerceapp.repository.CategorieRepository;
import com.example.ecommerceapp.repository.ProduitRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryTools {

    private final CategorieRepository categorieRepository;
    private final ProduitRepository produitRepository;

    public CategoryTools(CategorieRepository categorieRepository, ProduitRepository produitRepository) {
        this.categorieRepository = categorieRepository;
        this.produitRepository = produitRepository;
    }

    @Tool(
            name = "listerCategories",
            description = "Retourne la liste complète des catégories de produits disponibles dans la boutique."
    )
    public List<String> listerCategories() {
        System.out.println("🤖 [Tool Call] Récupération de toutes les catégories.");
        return categorieRepository.findAll().stream()
                .map(Categorie::getNom)
                .toList();
    }

    @Tool(
            name = "obtenirsProduitsParCategorie",
            description = "Retourne tous les produits appartenant à une catégorie spécifique via l'ID de la catégorie."
    )
    public List<Produit> obtenirsProduitsParCategorie(Long categorieId) {
        System.out.println("🤖 [Tool Call] Recherche des produits pour la catégorie ID : " + categorieId);
        return produitRepository.findAll().stream()
                .filter(p -> p.getCategorie() != null && p.getCategorie().getId().equals(categorieId))
                .toList();
    }

    @Tool(
            name = "recommanderProduits",
            description = "Recommande des produits similaires ou pertinents en se basant sur une tranche de prix maximale ou un domaine d'intérêt."
    )
    public List<Produit> recommanderProduits(Double prixMax) {
        System.out.println("🤖 [Tool Call] Recommandation de produits sous le budget de : " + prixMax + " DHS");
        return produitRepository.findAll().stream()
                .filter(p -> p.getPrix() <= prixMax && p.getQuantiteStock() > 0)
                .limit(4)
                .toList();
    }
}