package com.example.ecommerceapp.repository;

import com.example.ecommerceapp.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {


    Optional<Client> findByEmail(String clientId);
}
