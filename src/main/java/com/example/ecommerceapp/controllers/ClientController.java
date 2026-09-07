package com.example.ecommerceapp.controllers;

import com.example.ecommerceapp.model.Client;
import com.example.ecommerceapp.service.ClientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public Client creerClient(@RequestBody Client client) {
        return clientService.creerClient(client);
    }

    @GetMapping
    public List<Client> obtenirTousLesClients() {
        return clientService.obtenirTousLesClients();
    }
}