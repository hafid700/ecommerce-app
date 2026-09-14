package com.example.ecommerceapp.agent.tools;

import com.example.ecommerceapp.model.Utilisateur;
import com.example.ecommerceapp.repository.UtilisateurRepository; // Ajustez le nom selon votre Repository Utilisateur
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserTools {

    private final UtilisateurRepository utilisateurRepository;

    public UserTools(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Tool(
            name = "obtenirProfilClientActuel",
            description = "Récupère les informations du profil du client actuellement authentifié (nom, email, rôle)."
    )
    public String obtenirProfilClientActuel() {
        System.out.println("🤖 [Tool Call] Consultation du profil client connecté");

        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : null;

        if (username == null || "anonymousUser".equals(username)) {
            return "Le client est actuellement un visiteur anonyme (non connecté).";
        }

        Optional<Utilisateur> userOpt = utilisateurRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            Utilisateur u = userOpt.get();
            return "Client connecté - Username: " + u.getUsername() + " | Rôle: " + u.getRole();
        }

        return "Informations de profil introuvables pour : " + username;
    }
}
