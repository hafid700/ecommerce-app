package com.example.ecommerceapp.agent.tools;

import com.example.ecommerceapp.model.Client;
import com.example.ecommerceapp.model.Utilisateur;
import com.example.ecommerceapp.repository.ClientRepository;
import com.example.ecommerceapp.repository.UtilisateurRepository; // Ajustez le nom selon votre Repository Utilisateur
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserTools {

    private final UtilisateurRepository utilisateurRepository;
    private final ClientRepository clientRepository;


    public UserTools(UtilisateurRepository utilisateurRepository, ClientRepository clientRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.clientRepository = clientRepository;
    }

    @Tool(
            name = "obtenirProfilClientActuel",
            description = "Récupère les informations complètes du profil du client actuellement authentifié (nom, prénom, adresse, téléphone, email)."
    )
    public String obtenirProfilClientActuel() {
        System.out.println("🤖 [Tool Call] Consultation du profil client connecté");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "CLIENT_ANONYME: Le client est actuellement un visiteur non connecté.";
        }

        String username = auth.getName();

        // Recherche de l'entité Client associée au Username ou Email authentifié
        Optional<Client> clientOpt = clientRepository.findAll().stream()
                .filter(c -> (c.getEmail() != null && c.getEmail().equalsIgnoreCase(username)) ||
                        (c.getUser() != null && c.getUser().getUsername() != null && c.getUser().getUsername().equalsIgnoreCase(username)) ||
                        (c.getUser() != null && c.getUser().getEmail() != null && c.getUser().getEmail().equalsIgnoreCase(username)))
                .findFirst();

        if (clientOpt.isPresent()) {
            Client c = clientOpt.get();

            boolean estComplet = c.getNom() != null && !c.getNom().isBlank() &&
                    c.getPrenom() != null && !c.getPrenom().isBlank() &&
                    c.getAdresse() != null && !c.getAdresse().isBlank() &&
                    c.getTelephone() != null && !c.getTelephone().isBlank();

            if (!estComplet) {
                return "PROFIL_INCOMPLET: Le client est connecté (" + username + ") mais certaines coordonnées de livraison (adresse, téléphone...) manquent.";
            }

            return String.format(java.util.Locale.US,
                    "PROFIL_TROUVE:{\"nom\":\"%s\",\"prenom\":\"%s\",\"adresse\":\"%s\",\"telephone\":\"%s\"}",
                    c.getNom(), c.getPrenom(), c.getAdresse(), c.getTelephone()
            );
        }

        return "PROFIL_INCOMPLET: Utilisateur connecté (" + username + ") sans fiche Client associée.";
    }
}
