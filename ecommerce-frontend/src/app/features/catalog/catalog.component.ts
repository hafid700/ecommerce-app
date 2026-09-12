import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService, Produit, Categorie } from '../../core/services/product.service';
import { AuthService } from '../../core/services/auth.service';
import {Router, RouterLink} from '@angular/router';
import {CartService} from "../../core/services/cart.service";

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [CommonModule,FormsModule, RouterLink],
  templateUrl: './catalog.component.html'
})
export class CatalogComponent implements OnInit {
  produits: Produit[] = [];
  produitsFiltres: Produit[] = [];
  categories: Categorie[] = [];
  selectedCategorieId: number | null| undefined = null;
  loading = true;

  // 🔍 Variables de Recherche & Pagination
  searchTerm: string = '';
  currentPage: number = 1;
  itemsPerPage: number = 6; // Nombre de produits par page

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

  // 🔍 Applique à la fois le filtre par catégorie ET la recherche par mot-clé
  appliquerFiltres(): void {
    this.currentPage = 1; // Réinitialise à la 1ère page lors d'une recherche/filtrage

    this.produitsFiltres = this.produits.filter(p => {
      // 1. Filtre Categorie
      const matchCategorie = !this.selectedCategorieId ||
        (p.categorie && p.categorie.id === this.selectedCategorieId);

      // 2. Filtre Recherche texte
      const matchRecherche = !this.searchTerm ||
        p.nom.toLowerCase().includes(this.searchTerm.toLowerCase());

      return matchCategorie && matchRecherche;
    });
  }

  filtrerCategorie(categorieId: number | null | undefined): void {
    this.selectedCategorieId = categorieId;
    this.appliquerFiltres();
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

  onSearchChange(): void {
    this.appliquerFiltres();
  }

  voirDetail(id: number): void {
    if (id) {
      this.router.navigate(['/product', id]);
    }
  }

  // 📄 Getters pour la Pagination
  get produitsPagines(): Produit[] {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.produitsFiltres.slice(startIndex, startIndex + this.itemsPerPage);
  }

  get totalPages(): number {
    return Math.ceil(this.produitsFiltres.length / this.itemsPerPage) || 1;
  }

  changerPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
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
