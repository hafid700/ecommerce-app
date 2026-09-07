package com.example.ecommerceapp.service;

import com.example.ecommerceapp.model.Produit;
import com.example.ecommerceapp.repository.ProduitRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProduitService {

    private final ProduitRepository produitRepository;

    public ProduitService(ProduitRepository produitRepository){
        this.produitRepository=produitRepository;
    }

    public Produit creeProduit(Produit produit){
        return produitRepository.save(produit);
    }

    public List<Produit> ProduitsParCategorie(Long categorieId){
        return produitRepository.findByCategorieId(categorieId);
    }


    // Modifier un produit existant
    public Produit modifierProduit(Long id, Produit produitModifie) {
        return produitRepository.findById(id)
                .map(produit -> {
                    produit.setNom(produitModifie.getNom());
                    produit.setPrix(produitModifie.getPrix());
                    if (produitModifie.getCategorie() != null) {
                        produit.setCategorie(produitModifie.getCategorie());
                    }
                    return produitRepository.save(produit);
                })
                .orElseThrow(() -> new RuntimeException("Produit introuvable avec l'id : " + id));
    }

    // Supprimer un produit par son ID
    public void supprimerProduit(Long id) {
        if (!produitRepository.existsById(id)) {
            throw new RuntimeException("Impossible de supprimer : Produit introuvable avec l'id " + id);
        }
        produitRepository.deleteById(id);
    }


}
