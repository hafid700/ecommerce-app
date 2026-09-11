package com.example.ecommerceapp.controllers;


import com.example.ecommerceapp.model.Commande;
import com.example.ecommerceapp.model.StatusCommande;
import com.example.ecommerceapp.service.CommandeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/client/email/{email}")
    public List<Commande> getCommandesParClientEmail(@PathVariable String email) {
        return commandeService.findByClientEmail(email);
    }

    // DTO interne pour recevoir la requête proprement
    public static class StatutRequest {
        private String statut;
        public String getStatut() { return statut; }
        public void setStatut(String statut) { this.statut = statut; }
    }

    @PutMapping("/{id}/statut")
    public Commande changerStatut(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String statutStr = payload.get("statut");
        StatusCommande statut = StatusCommande.valueOf(statutStr);
        return commandeService.changerStatus(id, statut);
    }


}
