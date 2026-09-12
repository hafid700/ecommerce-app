import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Produit } from '../../core/services/product.service';
import { ProductService } from '../../core/services/product.service';
import { CartService } from '../../core/services/cart.service';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './product-detail.component.html'
})
export class ProductDetailComponent implements OnInit {
  produit?: Produit;
  produitsSimilaires: Produit[] = [];
  quantite: number = 1;
  loading: boolean = true;
  messageAjout: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService
  ) {}

  ngOnInit(): void {
    // S'abonne aux changements de paramètres de route (pour recharger lors du clic sur un produit similaire)
    this.route.params.subscribe(params => {
      const id = +params['id'];
      if (id) {
        this.chargerProduit(id);
      }
    });
  }

  chargerProduit(id: number): void {
    this.loading = true;
    this.productService.getProduitById(id).subscribe({
      next: (prod) => {
        this.produit = prod;
        this.loading = false;
        if (prod.categorie?.id) {
          this.chargerProduitsSimilaires(prod.categorie.id, prod.id!);
        }
      },
      error: (err) => {
        console.error('Erreur chargement produit:', err);
        this.loading = false;
      }
    });
  }

  chargerProduitsSimilaires(categorieId: number, produitIdActuel: number): void {
    this.productService.getProduits().subscribe(produits => {
      // Filtre les produits de la même catégorie en excluant le produit actuellement affiché
      this.produitsSimilaires = produits
        .filter(p => p.categorie?.id === categorieId && p.id !== produitIdActuel)
        .slice(0, 4); // Limite à 4 produits similaires
    });
  }

  ajouterAuPanier(): void {
    if (this.produit) {
      this.cartService.ajouterProduit(this.produit, this.quantite);
      this.messageAjout = `✅ ${this.quantite} x "${this.produit.nom}" ajouté(s) au panier !`;
      setTimeout(() => this.messageAjout = '', 3000);
    }
  }

  voirDetail(id: number): void {
    this.router.navigate(['/product', id]);
  }
}
