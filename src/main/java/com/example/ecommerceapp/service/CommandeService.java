package com.example.ecommerceapp.service;


import com.example.ecommerceapp.model.*;
import com.example.ecommerceapp.repository.ClientRepository;
import com.example.ecommerceapp.repository.CommandeRepository;
import com.example.ecommerceapp.repository.ProduitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ProduitRepository produitRepository;
    private final ClientRepository clientRepository;

    public CommandeService(CommandeRepository commandeRepository, ProduitRepository produitRepository, ClientRepository clientRepository){
        this.commandeRepository=commandeRepository;
        this.produitRepository = produitRepository;
        this.clientRepository = clientRepository;
    }

    public Commande passerCommande(Commande commande) {
        commande.setDateCommande(LocalDateTime.now());
        commande.setStatut(StatusCommande.VALIDEE);

        // 1. Charger et lier le Client si un ID est fourni
        if (commande.getClient() != null && commande.getClient().getId() != null) {
            Long clientId = commande.getClient().getId();
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client introuvable avec l'id : " + clientId));
            commande.setClient(client);
        }

        // 2. Traitement des lignes de commande
        double totalCalcul = 0.0;
        if (commande.getLignes() != null) {
            for (LigneCommande ligne : commande.getLignes()) {
                if (ligne.getProduit() == null || ligne.getProduit().getId() == null) {
                    throw new IllegalArgumentException("Chaque ligne doit contenir un produit avec un ID valide.");
                }

                Long produitId = ligne.getProduit().getId();
                Produit produitPersiste = produitRepository.findById(produitId)
                        .orElseThrow(() -> new RuntimeException("Produit introuvable avec l'id : " + produitId));

                ligne.setProduit(produitPersiste);
                ligne.setPrixUnitaire(produitPersiste.getPrix());
                ligne.setCommande(commande);

                totalCalcul += ligne.getPrixUnitaire() * ligne.getQuantite();
            }
        }

        commande.setTotal(totalCalcul);
        return commandeRepository.save(commande);
    }

    public List<Commande> obtenirToutesLesCommandes() {
        return commandeRepository.findAll();
    }



}
