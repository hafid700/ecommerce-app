import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductService, Produit, Categorie } from '../../core/services/product.service';
import { AuthService } from '../../core/services/auth.service';
import {Router, RouterLink} from '@angular/router';
import {CartService} from "../../core/services/cart.service";

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './catalog.component.html'
})
export class CatalogComponent implements OnInit {
  produits: Produit[] = [];
  produitsFiltres: Produit[] = [];
  categories: Categorie[] = [];
  selectedCategorieId: number | null| undefined = null;
  loading = true;

  constructor(
    private productService: ProductService,
    public authService: AuthService,
    public cartService: CartService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.chargerDonnees();
  }

  chargerDonnees(): void {
    this.loading = true;
    this.productService.getCategories().subscribe({
      next: (cats) => this.categories = cats,
      error: (err) => console.error('Erreur chargement catégories', err)
    });

    this.productService.getProduits().subscribe({
      next: (prods) => {
        this.produits = prods;
        this.produitsFiltres = prods;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement produits', err);
        this.loading = false;
      }
    });
  }

  filtrer(categorieId: number | null | undefined): void {
    this.selectedCategorieId = categorieId;

    if (categorieId === null || categorieId === undefined) {
      this.produitsFiltres = this.produits;
    } else {
      this.produitsFiltres = this.produits.filter(
        p => p.categorie && p.categorie.id === categorieId
      );
    }
  }

  ajouterAuPanier(produit: Produit): void {
    this.cartService.ajouterProduit(produit);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
