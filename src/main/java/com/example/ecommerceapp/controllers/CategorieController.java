package com.example.ecommerceapp.controllers;


import com.example.ecommerceapp.model.Categorie;
import com.example.ecommerceapp.service.CategorieService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategorieController {

   private final CategorieService categorieService;

   public CategorieController(CategorieService categorieService){
       this.categorieService=categorieService;
   }

   @PostMapping
    public Categorie creeCategorie(@RequestBody Categorie categorie){
       return categorieService.creeCategorie(categorie);
   }

   @GetMapping
    public List<Categorie> obtenirCategories(){
       return categorieService.ShowAllCategorie();
   }

    // Mettre à jour une catégorie (ex: PUT /categories/1)
    @PutMapping("/{id}")
    public Categorie modifierCategorie(@PathVariable Long id, @RequestBody Categorie categorie) {
        return categorieService.modifierCategorie(id, categorie);
    }

    // Supprimer une catégorie (ex: DELETE /categories/1)
    @DeleteMapping("/{id}")
    public void supprimerCategorie(@PathVariable Long id) {
        categorieService.supprimerCategorie(id);
    }


}
