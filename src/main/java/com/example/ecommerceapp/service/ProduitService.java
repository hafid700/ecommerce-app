package com.example.ecommerceapp.service;

import com.example.ecommerceapp.exception.ResourceNotFoundException;
import com.example.ecommerceapp.model.Categorie;
import com.example.ecommerceapp.model.Produit;
import com.example.ecommerceapp.repository.CategorieRepository;
import com.example.ecommerceapp.repository.ProduitRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;

    public ProduitService(ProduitRepository produitRepository, CategorieRepository categorieRepository){
        this.produitRepository=produitRepository;
        this.categorieRepository = categorieRepository;
    }

    public Produit creeProduit(Produit produit){
        return produitRepository.save(produit);
    }

    public List<Produit> ProduitsParCategorie(Long categorieId){
        return produitRepository.findByCategorieId(categorieId);
    }

    public List<Produit> obtenirTousLesProduits() {
        return produitRepository.findAll();
    }


    // Modifier un produit existant
    public Produit modifierProduit(Long id, Produit produitModifie) {
        return produitRepository.findById(id)
                .map(produit -> {
                    if (produitModifie.getNom() != null) {
                        produit.setNom(produitModifie.getNom());
                    }
                    if (produitModifie.getPrix() != null) {
                        produit.setPrix(produitModifie.getPrix());
                    }
                    if (produitModifie.getQuantiteStock() != null) {
                        produit.setQuantiteStock(produitModifie.getQuantiteStock());
                    }

                    // 👈 Vérification stricte sur la catégorie et son ID
                    if (produitModifie.getCategorie() != null && produitModifie.getCategorie().getId() != null) {
                        Long catId = produitModifie.getCategorie().getId();
                        Categorie cat = categorieRepository.findById(catId)
                                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée avec l'id : " + catId));
                        produit.setCategorie(cat);
                    }

                    return produitRepository.save(produit);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable avec l'id : " + id));
    }

    // Supprimer un produit par son ID
    public void supprimerProduit(Long id) {
        if (!produitRepository.existsById(id)) {
            throw new RuntimeException("Impossible de supprimer : Produit introuvable avec l'id " + id);
        }
        produitRepository.deleteById(id);
    }

    public Produit obtenirProduitParId(Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID : " + id));
    }


}
