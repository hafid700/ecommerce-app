package com.example.ecommerceapp.agent.dto;

public class CartItemDTO {
    private Long productId;
    private int quantite;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
}