package com.example.ecommerceapp.repository;

import com.example.ecommerceapp.model.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {

    List<Commande> findByClientId(Long clientId);

    List<Commande> findByClientEmail(String clientEmail);

    Optional<Commande> findByIdAndClientEmail(Long id, String email);

}
