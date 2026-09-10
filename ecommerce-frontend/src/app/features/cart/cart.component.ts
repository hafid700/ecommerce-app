import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CartService, CartItem } from '../../core/services/cart.service';
import { OrderService, CommandeRequest } from '../../core/services/order.service';
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
    private orderService: OrderService,
    public authService: AuthService, // 👈 Doit être public pour le template
    private router: Router
  ) {}

  modifierQuantite(item: CartItem, delta: number): void {
    if (item.produit.id !== undefined) {
      this.cartService.modifierQuantite(item.produit.id, item.quantite + delta);
    }
  }

  supprimer(item: CartItem): void {
    if (item.produit.id !== undefined) {
      this.cartService.supprimerProduit(item.produit.id);
    }
  }

  validerCommande(): void {
    // 1. Vérifier si l'utilisateur est connecté
    if (!this.authService.isLoggedIn()) {
      // ⚠️ On NE vide PAS le panier ! On le conserve et on redirige vers /login
      this.router.navigate(['/login']);
      return;
    }

    const items = localStorage.getItem('shopping_cart');
    const parsedItems: CartItem[] = items ? JSON.parse(items) : [];

    if (parsedItems.length === 0) {
      return;
    }

    this.loading = true;
    this.messageErreur = '';
    this.messageSucces = '';

    const commandePayload: CommandeRequest = {
      client: { id: 1 }, // ID dynamique ou client #1
      lignes: parsedItems
        .filter(item => item.produit.id !== undefined)
        .map(item => ({
          quantite: item.quantite,
          produit: { id: item.produit.id! }
        }))
    };

    this.orderService.passerCommande(commandePayload).subscribe({
      next: (res) => {
        this.loading = false;
        this.messageSucces = '🎉 Commande validée avec succès ! ID Commande: #' + res.id;
        this.cartService.viderPanier(); // Le panier se vide UNIQUEMENT quand la commande est réussie !
      },
      error: (err) => {
        this.loading = false;
        console.error('Erreur validation commande', err);
        this.messageErreur = err.error?.message || 'Une erreur est survenue lors de la commande.';
      }
    });
  }
}
