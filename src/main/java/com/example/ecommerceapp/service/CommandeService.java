package com.example.ecommerceapp.service;


import com.example.ecommerceapp.exception.ResourceNotFoundException;
import com.example.ecommerceapp.exception.StockInsuffisantException;
import com.example.ecommerceapp.model.*;
import com.example.ecommerceapp.repository.ClientRepository;
import com.example.ecommerceapp.repository.CommandeRepository;
import com.example.ecommerceapp.repository.ProduitRepository;
import jakarta.transaction.Transactional;
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

    @Transactional // Garantie l'atomicité : Tout passe ou Tout s'annule
    public Commande passerCommande(Commande commande) {
        commande.setDateCommande(LocalDateTime.now());

        if (commande.getStatut() == null) {
            commande.setStatut(StatusCommande.EN_ATTENTE);
        }

        // 1. GESTION AUTOMATIQUE DU CLIENT
        if (commande.getClient() != null) {
            Client clientPersiste;

            // CAS A : Recherche par ID si fourni
            if (commande.getClient().getId() != null) {
                Long clientId = commande.getClient().getId();
                clientPersiste = clientRepository.findById(clientId)
                        .orElseThrow(() -> new ResourceNotFoundException("Client introuvable avec l'id : " + clientId));
            }
            // CAS B : Recherche ou création automatique par Email si l'ID n'est pas fourni
            else if (commande.getClient().getEmail() != null) {
                String email = commande.getClient().getEmail();
                String nom = commande.getClient().getNom() != null ? commande.getClient().getNom() : "Client Anonyme";

                clientPersiste = clientRepository.findByEmail(email)
                        .orElseGet(() -> clientRepository.save(new Client(nom, email)));
            } else {
                throw new IllegalArgumentException("La commande doit comporter un client avec un ID ou un Email valide.");
            }

            commande.setClient(clientPersiste);
        } else {
            throw new IllegalArgumentException("Informations du client manquantes dans la commande.");
        }

        double totalCalcul = 0.0;

        // 2. Traitement et Décrémentation du Stock
        if (commande.getLignes() != null) {
            for (LigneCommande ligne : commande.getLignes()) {
                if (ligne.getProduit() == null || ligne.getProduit().getId() == null) {
                    throw new IllegalArgumentException("Chaque ligne doit contenir un produit avec un ID valide.");
                }

                Long produitId = ligne.getProduit().getId();
                Produit produitPersiste = produitRepository.findById(produitId)
                        .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable avec l'id : " + produitId));

                // VERIFICATION DU STOCK
                if (produitPersiste.getQuantiteStock() < ligne.getQuantite()) {
                    throw new StockInsuffisantException(
                            "Stock insuffisant pour le produit '" + produitPersiste.getNom() +
                                    "'. Stock disponible: " + produitPersiste.getQuantiteStock() +
                                    ", quantité demandée: " + ligne.getQuantite()
                    );
                }

                // DECREMENTATION DU STOCK
                produitPersiste.setQuantiteStock(produitPersiste.getQuantiteStock() - ligne.getQuantite());
                produitRepository.save(produitPersiste); // Mise à jour du stock en BDD

                // Liaisons et calculs
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


    public List<Commande> obtenirCommandesClient(Long clientId) {

        // Vérifier que le client existe
        clientRepository.findById(clientId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Client introuvable avec l'id : " + clientId
                        )
                );

        return commandeRepository.findByClientId(clientId);
    }


    public List<Commande> findByClientEmail(String email) {
        return commandeRepository.findByClientEmail(email);
    }

     @Transactional
     public Commande changerStatus(Long commandeId,StatusCommande nouveauStatus){
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'id : " + commandeId));

        commande.setStatut(nouveauStatus);

        return commandeRepository.save(commande);
     }

}
