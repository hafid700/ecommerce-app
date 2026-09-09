package com.example.ecommerceapp.controllers;


import com.example.ecommerceapp.dto.AuthResponse;
import com.example.ecommerceapp.dto.LoginRequest;
import com.example.ecommerceapp.dto.RegisterRequest;
import com.example.ecommerceapp.model.Role;
import com.example.ecommerceapp.model.Utilisateur;
import com.example.ecommerceapp.repository.UtilisateurRepository;
import com.example.ecommerceapp.security.JwtUtils;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthController(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.utilisateurRepository = utilisateurRepository;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Utilisateur user = utilisateurRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Nom d'utilisateur ou mot de passe incorrect.");
        }

        String token = jwtUtils.generateToken(user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole().name()));
    }


}
