import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject } from 'rxjs';
import { Produit } from './product.service';

export interface CartItem {
  produit: Produit;
  quantite: number;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private itemsSubject = new BehaviorSubject<CartItem[]>([]);
  public items$ = this.itemsSubject.asObservable();
  private isBrowser: boolean;

  constructor(@Inject(PLATFORM_ID) platformId: Object) {
    this.isBrowser = isPlatformBrowser(platformId);
    if (this.isBrowser) {
      const savedCart = localStorage.getItem('shopping_cart');
      if (savedCart) {
        this.itemsSubject.next(JSON.parse(savedCart));
      }
    }
  }

  ajouterProduit(produit: Produit, quantite: number = 1): void {
    const currentItems = [...this.itemsSubject.value];
    const index = currentItems.findIndex(item => item.produit.id === produit.id);

    if (index > -1) {
      // Vérifier de ne pas dépasser le stock
      const nouvelleQuantite = currentItems[index].quantite + quantite;
      if (nouvelleQuantite <= produit.quantiteStock) {
        currentItems[index].quantite = nouvelleQuantite;
      } else {
        alert(`Stock maximum atteint (${produit.quantiteStock} disponibles)`);
        return;
      }
    } else {
      if (quantite <= produit.quantiteStock) {
        currentItems.push({ produit, quantite });
      } else {
        alert('Stock insuffisant');
        return;
      }
    }

    this.sauvegarderEtNotifier(currentItems);
  }

  modifierQuantite(produitId: number, quantite: number): void {
    let currentItems = [...this.itemsSubject.value];
    const index = currentItems.findIndex(item => item.produit.id === produitId);

    if (index > -1) {
      if (quantite <= 0) {
        this.supprimerProduit(produitId);
        return;
      }

      if (quantite <= currentItems[index].produit.quantiteStock) {
        currentItems[index].quantite = quantite;
        this.sauvegarderEtNotifier(currentItems);
      } else {
        alert('Quantité supérieure au stock disponible');
      }
    }
  }

  supprimerProduit(produitId: number): void {
    const currentItems = this.itemsSubject.value.filter(item => item.produit.id !== produitId);
    this.sauvegarderEtNotifier(currentItems);
  }

  viderPanier(): void {
    this.sauvegarderEtNotifier([]);
  }

  getCalculerTotal(): number {
    return this.itemsSubject.value.reduce((total, item) => total + (item.produit.prix * item.quantite), 0);
  }

  getNombreArticles(): number {
    return this.itemsSubject.value.reduce((count, item) => count + item.quantite, 0);
  }

  private sauvegarderEtNotifier(items: CartItem[]): void {
    if (this.isBrowser) {
      localStorage.setItem('shopping_cart', JSON.stringify(items));
    }
    this.itemsSubject.next(items);
  }
}
