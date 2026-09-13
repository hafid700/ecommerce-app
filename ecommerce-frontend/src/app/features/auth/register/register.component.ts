import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  user = {
    username: '',
    email: '',
    password: ''
  };

  messageSucces = '';
  errorMessage = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {}

  onRegister(event: Event): void {
    event.preventDefault();

    if (!this.user.username || !this.user.email || !this.user.password) {
      this.errorMessage = 'Veuillez remplir tous les champs.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.messageSucces = '';

    this.authService.register(this.user).subscribe({
      next: () => {
        this.loading = false;
        this.messageSucces = 'Inscription réussie ! Redirection vers la page de connexion...';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 1500);
      },
      error: (err) => {
        this.loading = false;
        console.error('Erreur inscription :', err);
        this.errorMessage = err.error?.message || 'Erreur lors de l\'inscription. Nom d\'utilisateur ou email déjà utilisé.';
      }
    });
  }
}
