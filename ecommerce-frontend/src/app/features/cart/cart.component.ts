import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CartService, CartItem } from '../../core/services/cart.service';
import { AuthService } from '../../core/services/auth.service';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './cart.component.html'
})
export class CartComponent {
  loading = false;
  messageSucces = '';
  messageErreur = '';

  constructor(
    public cartService: CartService,
    private authService: AuthService,
    private router: Router
  ) {}

  modifierQuantite(item: CartItem, delta: number): void {
    // 👈 Sécurisation : vérifie que l'ID existe
    if (item.produit.id !== undefined) {
      this.cartService.modifierQuantite(item.produit.id, item.quantite + delta);
    }
  }

  supprimer(item: CartItem): void {
    // 👈 Sécurisation : vérifie que l'ID existe
    if (item.produit.id !== undefined) {
      this.cartService.supprimerProduit(item.produit.id);
    }
  }


}
