package com.example.ecommerceapp.model;

import jakarta.persistence.*;

@Entity
public class Produit {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private Double prix=0.0;

    @ManyToOne
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    public Produit(){}

    public Produit(String nom,Double prix,Categorie categorie){
        this.nom=nom;
        this.prix=prix;
        this.categorie=categorie;
    }
    // Génère les Getters et Setters via ton IDE
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { this.prix = prix; }
    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

}
