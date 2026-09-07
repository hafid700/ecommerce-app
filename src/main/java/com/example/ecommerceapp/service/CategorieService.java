package com.example.ecommerceapp.service;


import com.example.ecommerceapp.model.Categorie;
import com.example.ecommerceapp.repository.CategorieRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategorieService {

    private final CategorieRepository categorieRepository;

    public CategorieService(CategorieRepository categorieRepository){
        this.categorieRepository=categorieRepository;
    }

    public Categorie creeCategorie(Categorie categorie){
        return categorieRepository.save(categorie);
    }

    public List<Categorie> ShowAllCategorie(){
        return categorieRepository.findAll();
    }

    // Modifier une catégorie
    public Categorie modifierCategorie(Long id, Categorie categorieModifiee) {
        return categorieRepository.findById(id)
                .map(categorie -> {
                    categorie.setNom(categorieModifiee.getNom());
                    return categorieRepository.save(categorie);
                })
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable avec l'id : " + id));
    }

    // Supprimer une catégorie par son ID
    public void supprimerCategorie(Long id) {
        if (!categorieRepository.existsById(id)) {
            throw new RuntimeException("Impossible de supprimer : Catégorie introuvable avec l'id " + id);
        }
        categorieRepository.deleteById(id);
    }

}
