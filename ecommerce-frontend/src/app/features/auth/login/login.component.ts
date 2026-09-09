import { Component, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  credentials = { username: '', password: '' };
  errorMessage = '';

  constructor(
    private authService: AuthService, 
    private router: Router,
    private ngZone: NgZone
  ) {}

  seConnecter(): void {
    if (!this.credentials.username || !this.credentials.password) {
      this.errorMessage = 'Veuillez remplir tous les champs.';
      return;
    }

    console.log('1. Envoi de la requête au backend avec :', this.credentials);

    this.authService.login(this.credentials).subscribe({
      next: (res) => {
        console.log('2. Authentification réussie ! Role =', res.role);

        this.ngZone.run(() => {
          if (res.role === 'ROLE_ADMIN') {
            console.log('3. Redirection vers /admin...');
            this.router.navigate(['/admin']);
          } else {
            console.log('3. Redirection vers /catalog...');
            this.router.navigate(['/catalog']);
          }
        });
      },
      error: (err) => {
        console.error('Erreur lors du login :', err);
        this.errorMessage = 'Nom d\'utilisateur ou mot de passe incorrect.';
      }
    });
  }
}