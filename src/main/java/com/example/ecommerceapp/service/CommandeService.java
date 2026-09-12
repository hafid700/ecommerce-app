package com.example.ecommerceapp.service;


import com.example.ecommerceapp.exception.ResourceNotFoundException;
import com.example.ecommerceapp.exception.StockInsuffisantException;
import com.example.ecommerceapp.model.*;
import com.example.ecommerceapp.repository.ClientRepository;
import com.example.ecommerceapp.repository.CommandeRepository;
import com.example.ecommerceapp.repository.ProduitRepository;
import com.example.ecommerceapp.repository.UtilisateurRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ProduitRepository produitRepository;
    private final ClientRepository clientRepository;
    private final UtilisateurRepository utilisateurRepository;

    public CommandeService(CommandeRepository commandeRepository, ProduitRepository produitRepository, ClientRepository clientRepository, UtilisateurRepository utilisateurRepository){
        this.commandeRepository=commandeRepository;
        this.produitRepository = produitRepository;
        this.clientRepository = clientRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional
    public Commande passerCommande(Commande commande) {
        commande.setDateCommande(LocalDateTime.now());
        if (commande.getStatut() == null) {
            commande.setStatut(StatusCommande.EN_ATTENTE);
        }

        if (commande.getClient() != null) {
            Client clientReq = commande.getClient();
            String email = clientReq.getEmail() != null ? clientReq.getEmail().trim().toLowerCase() : null;

            if (email == null || email.contains("undefined") || email.isBlank()) {
                throw new IllegalArgumentException("Une adresse email valide est requise pour passer la commande.");
            }

            // Recherche ou création du client
            Client clientPersiste = clientRepository.findByEmail(email)
                    .orElseGet(() -> {
                        Client c = new Client();
                        c.setEmail(email);
                        utilisateurRepository.findByEmail(email).ifPresent(c::setUser);
                        return c;
                    });

            // Mise à jour des coordonnées de livraison (COD)
            clientPersiste.setPrenom(clientReq.getPrenom());
            clientPersiste.setNom(clientReq.getNom());
            clientPersiste.setTelephone(clientReq.getTelephone());
            clientPersiste.setAdresse(clientReq.getAdresse());

            if (clientPersiste.getUser() == null) {
                utilisateurRepository.findByEmail(email).ifPresent(clientPersiste::setUser);
            }

            clientPersiste = clientRepository.save(clientPersiste);
            commande.setClient(clientPersiste);
        } else {
            throw new IllegalArgumentException("Informations client requises.");
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
