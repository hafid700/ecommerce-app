package com.example.ecommerceapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String prenom;
    private String nom;
    private String email;
    private String telephone;

    @Column(columnDefinition = "TEXT")
    private String adresse;

    // 👈 Liaison One-to-One avec le compte de sécurité/authentification
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", unique = true)
    @JsonIgnoreProperties({"password", "roles"}) // Évite d'exposer le mot de passe hashé dans le JSON
    private Utilisateur user;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    @JsonIgnore // Évite la boucle infinie JSON lors de la récupération du client
    private List<Commande> commandes = new ArrayList<>();


    public Client() {}

    public Client(String prenom, String nom, String email, String telephone, String adresse) {
        this.prenom = prenom;
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public Utilisateur getUser() { return user; }
    public void setUser(Utilisateur user) { this.user = user; }

    public List<Commande> getCommandes() { return commandes; }
    public void setCommandes(List<Commande> commandes) { this.commandes = commandes; }
}
