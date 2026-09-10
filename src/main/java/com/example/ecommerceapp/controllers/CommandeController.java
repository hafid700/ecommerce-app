package com.example.ecommerceapp.controllers;


import com.example.ecommerceapp.model.Commande;
import com.example.ecommerceapp.service.CommandeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commandes")
public class CommandeController {

    private final CommandeService commandeService;

    public CommandeController(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    @PostMapping
    public Commande passerCommande(@RequestBody Commande commande){
        return commandeService.passerCommande(commande);
    }

    @GetMapping
    public List<Commande> obtenirToutesLesCommandes() {
        return commandeService.obtenirToutesLesCommandes();
    }

    //obtenir les commandes d'un client
    @GetMapping("/client/{clientId}")
    public List<Commande> obtenirCommandesClient(@PathVariable Long clientId){
        return commandeService.obtenirCommandesClient(clientId);
    }


}
