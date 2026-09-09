package com.example.ecommerceapp.controllers;

import com.example.ecommerceapp.exception.ResourceNotFoundException;
import com.example.ecommerceapp.model.Produit;
import com.example.ecommerceapp.repository.ProduitRepository;
import com.example.ecommerceapp.service.ProduitService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produits")
public class ProduitController {

    private final ProduitService produitService;
    private final ProduitRepository produitRepository;

    public ProduitController(ProduitService produitService, ProduitRepository produitRepository){
        this.produitService=produitService;
        this.produitRepository = produitRepository;
    }

    @PostMapping
    public Produit creeProduit(@RequestBody Produit produit){
        return produitService.creeProduit(produit);
    }

    // Requête GET pour LIRE les produits d'une catégorie spécifique
    @GetMapping("/categorie/{id}")
    public List<Produit> obtenirProduitByCategorie(@PathVariable Long id){

        return produitService.ProduitsParCategorie(id);
    }

    @GetMapping
    public List<Produit> obtenirTousLesProduits() {
        return produitService.obtenirTousLesProduits();
    }

    @PutMapping("/{id}")
    public Produit modifierProduit(@PathVariable Long id,@RequestBody Produit produitModifie) {
        return produitRepository.findById(id)
                .map(produit -> {
                    produit.setNom(produitModifie.getNom());
                    produit.setPrix(produitModifie.getPrix());

                    // Mettre à jour la catégorie si elle est fournie
                    if (produitModifie.getCategorie() != null) {
                        produit.setCategorie(produitModifie.getCategorie());
                    }

                    // 👈 NOUVEAU : Mettre à jour le stock seulement s'il est précisé dans la requête
                    if (produitModifie.getQuantiteStock() != null) {
                        produit.setQuantiteStock(produitModifie.getQuantiteStock());
                    }

                    return produitRepository.save(produit);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable avec l'id : " + id));
    }

    @DeleteMapping("/{id}")
    public void supprimerProduit(@PathVariable Long id) {
        produitService.supprimerProduit(id);
    }

}
