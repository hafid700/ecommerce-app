package com.example.ecommerceapp.service;

import com.example.ecommerceapp.model.Client;
import com.example.ecommerceapp.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

    @Service
    public class ClientService {

        private final ClientRepository clientRepository;

        public ClientService(ClientRepository clientRepository) {
            this.clientRepository = clientRepository;
        }

        public Client creerClient(Client client) {
            return clientRepository.save(client);
        }

        public List<Client> obtenirTousLesClients() {
            return clientRepository.findAll();
        }

        public Client obtenirClientParId(Long id) {
            return clientRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Client introuvable avec l'id : " + id));
        }
    }

