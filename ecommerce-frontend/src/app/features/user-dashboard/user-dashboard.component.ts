import { Component,OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderService, Commande } from '../../core/services/order.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './user-dashboard.component.html'
})
export class UserDashboardComponent implements OnInit {
  commandes: Commande[] = [];
  loading = true;
  private isBrowser: boolean;

  constructor(
    public authService: AuthService,
    private orderService: OrderService,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
      this.isBrowser = isPlatformBrowser(platformId);
}

  ngOnInit(): void {
    // 👈 N'exécute l'appel HTTP que si nous sommes dans le navigateur
    if (this.isBrowser) {
      this.chargerCommandes();
    }
  }

  chargerCommandes(): void {
    // Dans une version complète, on utilise l'ID de l'utilisateur connecté
    const clientId = 1;

    this.orderService.getCommandesParClient(clientId).subscribe({
      next: (data) => {
        this.commandes = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des commandes :', err);
        this.loading = false;
      }
    });
  }

  calculerTotalCommande(commande: Commande): number {
    if (!commande.lignes) return 0;
    return commande.lignes.reduce((sum, ligne) => sum + (ligne.produit.prix * ligne.quantite), 0);
  }
}
