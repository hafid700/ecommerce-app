package com.example.ecommerceapp.repository;

import com.example.ecommerceapp.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProduitRepository extends JpaRepository<Produit,Long> {

    List<Produit> findByCategorieId(Long categorieId);


}

