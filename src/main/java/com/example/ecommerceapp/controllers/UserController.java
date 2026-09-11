package com.example.ecommerceapp.controllers;

import com.example.ecommerceapp.model.Utilisateur;
import com.example.ecommerceapp.repository.UtilisateurRepository;
import jdk.jshell.execution.Util;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UtilisateurRepository userRepository;

    public UserController(UtilisateurRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Utilisateur> obtenirTousLesUtilisateurs() {
        return userRepository.findAll();
    }
}