import {Component, Inject, OnInit, PLATFORM_ID} from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ProductService, Produit, Categorie } from '../../core/services/product.service';
import { OrderService, Commande } from '../../core/services/order.service';
import {AuthService, UserApp} from '../../core/services/auth.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './admin.component.html'
})
export class AdminComponent implements OnInit {
  activeTab: string = 'produits';

  produits: Produit[] = [];
  categories: Categorie[] = [];
  commandes: Commande[] = [];
  users: UserApp[] = [];
  loading = true;

  private isBrowser: boolean;

  // Formulaire Produit
  nouveauProduit: Produit = {
    nom: '',
    prix: 0,
    quantiteStock: 0,
    categorie: { id: undefined, nom: '' }
  };

  nouvelleCategorie: Categorie = { nom: '' };

  modeEdition = false;
  produitEnEditionId: number | null = null;
  selectedCategorieId: number | null = null;

  messageSucces = '';
  messageErreur = '';

  statusDisponible= ['EN_ATTENTE', 'VALIDEE', 'EXPEDIEE', 'LIVREE', 'ANNULEE'];

  constructor(
    private productService: ProductService,
    private orderService: OrderService,
    public authService: AuthService,
    private router: Router,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
  this.isBrowser = isPlatformBrowser(platformId);
  }

  ngOnInit(): void {
    if (this.isBrowser) {
      this.chargerDonnees();
    }
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

    this.orderService.getToutesLesCommandes().subscribe({
      next: (cmds) => this.commandes = cmds,
      error: (err) => console.error('Erreur chargement commandes', err)
    });

    this.authService.getTousLesUtilisateurs().subscribe({
      next: (u) => this.users = u,
      error: (err) => console.error('Erreur chargement utilisateurs', err)
    });

  }

  // --- GESTION DES COMMANDES ---
  changerStatut(commandeId: number, nouveauStatut: string): void {
    // 1. Demande de confirmation à l'administrateur
    const confirmation = confirm(
      `Êtes-vous sûr de vouloir passer la commande #${commandeId} au statut "${nouveauStatut}" ?`
    );

    // Si l'administrateur annule, on rafraîchit pour remettre le <select> à sa valeur d'origine
    if (!confirmation) {
      this.chargerDonnees();
      return;
    }

    // 2. Si confirmé, envoi de la requête au serveur
    this.orderService.changerStatutCommande(commandeId, nouveauStatut).subscribe({
      next: (commandeMiseAJour) => {
        this.messageSucces = `🎉 Le statut de la commande #${commandeId} a été mis à jour avec succès : ${nouveauStatut}`;
        this.messageErreur = '';

        // Mise à jour réactive immédiate de l'objet local
        const cmd = this.commandes.find(c => c.id === commandeId);
        if (cmd) {
          cmd.status = nouveauStatut;
        }

        // Rechargement global pour garder les listes et les KPIs synchronisés
        this.chargerDonnees();
      },
      error: (err) => {
        console.error('Erreur changement statut :', err);
        this.messageErreur = 'Impossible de modifier le statut de la commande.';
        this.chargerDonnees(); // Réinitialise l'affichage sur la valeur réelle en BDD
      }
    });
  }

  calculerTotalCommande(commande: Commande): number {
    if (!commande.lignes) return 0;
    return commande.lignes.reduce((sum, line) => sum + (line.produit.prix * line.quantite), 0);
  }


  // --- GESTION DES CATÉGORIES ---
  enregistrerCategorie(): void {
    if (!this.nouvelleCategorie.nom.trim()) {
      this.messageErreur = 'Le nom de la catégorie est obligatoire.';
      return;
    }

    this.messageErreur = '';
    this.productService.ajouterCategorie(this.nouvelleCategorie).subscribe({
      next: () => {
        this.messageSucces = 'Catégorie ajoutée avec succès !';
        this.nouvelleCategorie = { nom: '' };
        this.chargerDonnees();
      },
      error: (err) => {
        console.error('Erreur ajout catégorie', err);
        this.messageErreur = 'Impossible d\'ajouter la catégorie.';
      }
    });
  }

  supprimerCategorie(id: number | undefined): void {
    if (!id) return;
    if (confirm('Êtes-vous sûr de vouloir supprimer cette catégorie ?')) {
      this.productService.supprimerCategorie(id).subscribe({
        next: () => {
          this.messageSucces = 'Catégorie supprimée avec succès.';
          this.chargerDonnees();
        },
        error: () => this.messageErreur = 'Impossible de supprimer cette catégorie.'
      });
    }
  }

  // --- GESTION DES PRODUITS ---
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

  // 📊 --- CALCULS DYNAMIQUES DES KPIs ---

// 1. Chiffre d'affaires total (Commandes VALIDEE, EXPEDIEE ou LIVREE)
  get chiffreAffairesTotal(): number {
    return this.commandes
      .filter(c => c.status !== 'ANNULEE')
      .reduce((total, c) => total + this.calculerTotalCommande(c), 0);
  }

// 2. Nombre de commandes par statut
  get nbCommandesValidees(): number {
    return this.commandes.filter(c => c.status === 'VALIDEE' || c.status === 'LIVREE' || c.status === 'EXPEDIEE').length;
  }

  get nbCommandesEnAttente(): number {
    return this.commandes.filter(c => c.status === 'EN_ATTENTE').length;
  }

  get nbCommandesAnnulees(): number {
    return this.commandes.filter(c => c.status === 'ANNULEE').length;
  }

// 3. Alerte Stock Faible (Moins de 5 articles)
  get produitsAlerteStock(): Produit[] {
    return this.produits.filter(p => p.quantiteStock <= 5);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
