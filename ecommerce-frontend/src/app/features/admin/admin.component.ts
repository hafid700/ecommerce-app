import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ProductService, Produit, Categorie } from '../../core/services/product.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './admin.component.html'
})
export class AdminComponent implements OnInit {
  produits: Produit[] = [];
  categories: Categorie[] = [];
  loading = true;

  // Formulaire Produit
  nouveauProduit: Produit = {
    nom: '',
    prix: 0,
    quantiteStock: 0,
    categorie: { id: undefined, nom: '' }
  };

  modeEdition = false;
  produitEnEditionId: number | null = null;
  selectedCategorieId: number | null = null;

  messageSucces = '';
  messageErreur = '';

  constructor(
    private productService: ProductService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.chargerDonnees();
  }

  chargerDonnees(): void {
    this.loading = true;
    this.productService.getCategories().subscribe({
      next: (cats) => this.categories = cats,
      error: (err) => console.error('Erreur catégories', err)
    });

    this.productService.getProduits().subscribe({
      next: (prods) => {
        this.produits = prods;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur produits', err);
        this.loading = false;
      }
    });
  }

  enregistrerProduit(): void {
    if (!this.nouveauProduit.nom || this.nouveauProduit.prix <= 0 || !this.selectedCategorieId) {
      this.messageErreur = 'Veuillez remplir tous les champs obligatoires.';
      return;
    }

    this.messageErreur = '';
    const payload: Produit = {
      ...this.nouveauProduit,
      categorie: { id: Number(this.selectedCategorieId), nom: '' }
    };

    if (this.modeEdition && this.produitEnEditionId) {
      this.productService.modifierProduit(this.produitEnEditionId, payload).subscribe({
        next: () => {
          this.messageSucces = 'Produit modifié avec succès !';
          this.reinitialiserFormulaire();
          this.chargerDonnees();
        },
        error: (err) => this.messageErreur = 'Erreur lors de la modification.'
      });
    } else {
      this.productService.ajouterProduit(payload).subscribe({
        next: () => {
          this.messageSucces = 'Produit ajouté avec succès !';
          this.reinitialiserFormulaire();
          this.chargerDonnees();
        },
        error: (err) => this.messageErreur = 'Erreur lors de l\'ajout.'
      });
    }
  }

  editerProduit(produit: Produit): void {
    this.modeEdition = true;
    this.produitEnEditionId = produit.id || null;
    this.nouveauProduit = { ...produit };
    this.selectedCategorieId = produit.categorie?.id || null;
  }

  supprimerProduit(id: number | undefined): void {
    if (!id) return;
    if (confirm('Êtes-vous sûr de vouloir supprimer ce produit ?')) {
      this.productService.supprimerProduit(id).subscribe({
        next: () => {
          this.messageSucces = 'Produit supprimé.';
          this.chargerDonnees();
        },
        error: (err) => this.messageErreur = 'Impossible de supprimer le produit.'
      });
    }
  }

  reinitialiserFormulaire(): void {
    this.modeEdition = false;
    this.produitEnEditionId = null;
    this.selectedCategorieId = null;
    this.nouveauProduit = {
      nom: '',
      prix: 0,
      quantiteStock: 0
    };
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
