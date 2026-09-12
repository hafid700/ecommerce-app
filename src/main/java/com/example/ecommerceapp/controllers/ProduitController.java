package com.example.ecommerceapp.controllers;

import com.example.ecommerceapp.exception.ResourceNotFoundException;
import com.example.ecommerceapp.model.Produit;
import com.example.ecommerceapp.repository.ProduitRepository;
import com.example.ecommerceapp.service.ProduitService;
import org.springframework.http.ResponseEntity;
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

    // Requête GET pour LIRE un produit spécifique par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Produit> obtenirProduitParId(@PathVariable Long id) {
        Produit produit = produitService.obtenirProduitParId(id);
        return ResponseEntity.ok(produit);
    }

    @GetMapping
    public List<Produit> obtenirTousLesProduits() {
        return produitService.obtenirTousLesProduits();
    }

    @PutMapping("/{id}")
    public Produit modifierProduit(@PathVariable Long id, @RequestBody Produit produitModifie) {
        return produitRepository.findById(id)
                .map(produit -> {
                    // 1. Nom & Prix
                    if (produitModifie.getNom() != null) {
                        produit.setNom(produitModifie.getNom());
                    }
                    if (produitModifie.getPrix() != null) {
                        produit.setPrix(produitModifie.getPrix());
                    }

                    // 2. 👈 NOUVEAU : Description
                    if (produitModifie.getDescription() != null) {
                        produit.setDescription(produitModifie.getDescription());
                    }

                    // 3. 👈 NOUVEAU : URL de l'image
                    if (produitModifie.getImageUrl() != null) {
                        produit.setImageUrl(produitModifie.getImageUrl());
                    }

                    // 4. Stock
                    if (produitModifie.getQuantiteStock() != null) {
                        produit.setQuantiteStock(produitModifie.getQuantiteStock());
                    }

                    // 5. Catégorie
                    if (produitModifie.getCategorie() != null) {
                        produit.setCategorie(produitModifie.getCategorie());
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
