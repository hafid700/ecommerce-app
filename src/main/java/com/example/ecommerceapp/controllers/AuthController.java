package com.example.ecommerceapp.controllers;


import com.example.ecommerceapp.dto.AuthResponse;
import com.example.ecommerceapp.dto.LoginRequest;
import com.example.ecommerceapp.dto.RegisterRequest;
import com.example.ecommerceapp.model.Client;
import com.example.ecommerceapp.model.Role;
import com.example.ecommerceapp.model.Utilisateur;
import com.example.ecommerceapp.repository.ClientRepository;
import com.example.ecommerceapp.repository.UtilisateurRepository;
import com.example.ecommerceapp.security.JwtUtils;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {


    private final UtilisateurRepository utilisateurRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthController(UtilisateurRepository utilisateurRepository, ClientRepository clientRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.utilisateurRepository = utilisateurRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (utilisateurRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Erreur: Ce nom d'utilisateur est déjà pris.");
        }

        // Gestion propre du rôle
        Role roleUtilisateur = Role.ROLE_USER;
        if (request.getRole() != null) {
            roleUtilisateur = request.getRole();
        }

        // Instanciation de l'entité Utilisateur
        Utilisateur user = new Utilisateur(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getEmail(),
                roleUtilisateur
        );

        utilisateurRepository.save(user);
        return ResponseEntity.ok("Utilisateur enregistré avec succès !");
    }

    @Transactional
    public Utilisateur inscrireUtilisateur(RegisterRequest request) {
        if (utilisateurRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Ce nom d'utilisateur est déjà pris.");
        }
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet e-mail est déjà utilisé.");
        }

        // 1. Création de l'utilisateur
        Utilisateur user = new Utilisateur();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);

        Utilisateur userSauvegarde = utilisateurRepository.save(user);

        // 2. Création automatique du profil Client associé
        Client client = new Client();
        client.setNom(userSauvegarde.getUsername());
        client.setEmail(userSauvegarde.getEmail());
        client.setUser(userSauvegarde); // 👈 Associe immédiatement l'ID de l'utilisateur !

        clientRepository.save(client);

        return userSauvegarde;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // 1. Recherche de l'utilisateur par son username
        Utilisateur user = utilisateurRepository.findByUsername(request.getUsername())
                .orElse(null);

        // 2. Vérification de l'existence et du mot de passe
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Nom d'utilisateur ou mot de passe incorrect.");
        }



        // 3. Génération du Token JWT avec les données de 'user'
        String token = jwtUtils.generateToken(user.getUsername(), user.getRole().name());

        // 4. Renvoi du DTO d'authentification avec l'EMAIL RÉEL de l'entité 'user'
        return ResponseEntity.ok(new AuthResponse(
                token,
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        ));
    }


}
