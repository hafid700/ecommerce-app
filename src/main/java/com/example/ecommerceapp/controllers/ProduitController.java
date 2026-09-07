package com.example.ecommerceapp.controllers;

import com.example.ecommerceapp.model.Produit;
import com.example.ecommerceapp.service.ProduitService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produits")
public class ProduitController {

    private final ProduitService produitService;

    public ProduitController(ProduitService produitService){
        this.produitService=produitService;
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

    @PutMapping("/{id}")
    public Produit modifierProduit(@PathVariable Long id, @RequestBody Produit produit) {
        return produitService.modifierProduit(id, produit);
    }

    @DeleteMapping("/{id}")
    public void supprimerProduit(@PathVariable Long id) {
        produitService.supprimerProduit(id);
    }

}
