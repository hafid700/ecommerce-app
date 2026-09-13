import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CartService, CartItem } from '../../core/services/cart.service';
import { OrderService, CommandeRequest } from '../../core/services/order.service';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.scss']
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

  livraison = {
    prenom: '',
    nom: '',
    telephone: '',
    adresse: ''
  };

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
      // ⚠️ On NE vide PAS le panier ! On redirige vers /login
      this.router.navigate(['/login']);
      return;
    }

    // 2. Récupération dynamique de l'utilisateur connecté
    const currentUser = this.authService.getCurrentUser();

    // 🛑 Sécurité : Bloquer si le profil ou l'email est manquant
    if (!currentUser || !currentUser.email) {
      this.messageErreur = "Veuillez vous reconnecter : votre adresse email n'est pas disponible sur votre profil.";
      return;
    }

    // 3. Validation des champs du formulaire de livraison COD
    if (!this.livraison.prenom?.trim() ||
      !this.livraison.nom?.trim() ||
      !this.livraison.telephone?.trim() ||
      !this.livraison.adresse?.trim()) {
      this.messageErreur = "Veuillez remplir tous les champs obligatoires (*) pour la livraison.";
      return;
    }

    const items = localStorage.getItem('shopping_cart');
    const parsedItems: CartItem[] = items ? JSON.parse(items) : [];

    if (parsedItems.length === 0) {
      this.messageErreur = "Votre panier est vide.";
      return;
    }

    this.loading = true;
    this.messageErreur = '';
    this.messageSucces = '';

    // 4. Construction du payload avec les données réelles et les infos COD
    const commandePayload: CommandeRequest = {
      client: {
        prenom: this.livraison.prenom.trim(),
        nom: this.livraison.nom.trim(),
        email: currentUser.email.trim().toLowerCase(),
        telephone: this.livraison.telephone.trim(),
        adresse: this.livraison.adresse.trim()
      },
      lignes: parsedItems
        .filter(item => item.produit && item.produit.id !== undefined)
        .map(item => ({
          quantite: item.quantite,
          produit: { id: item.produit.id! }
        }))
    };

    // 5. Envoi au serveur Spring Boot
    this.orderService.passerCommande(commandePayload).subscribe({
      next: (res) => {
        this.loading = false;
        this.messageSucces = `🎉 Commande #${res.id} enregistrée avec succès ! Notre agent vous contactera sur le ${this.livraison.telephone} pour la confirmation.`;

        // Réinitialiser le formulaire de livraison
        this.livraison = { prenom: '', nom: '', telephone: '', adresse: '' };

        // Le panier se vide UNIQUEMENT quand la commande est réussie !
        this.cartService.viderPanier();
      },
      error: (err) => {
        this.loading = false;
        console.error('Erreur validation commande', err);
        this.messageErreur = err.error?.message || err.error || 'Une erreur est survenue lors de la commande.';
      }
    });
  }
}
