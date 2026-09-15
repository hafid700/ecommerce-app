import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService, ChatMessage } from '../../core/services/chat.service';
import { CartService } from '../../core/services/cart.service';


@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.scss']
})
export class ChatbotComponent {
  isOpen = false;
  loading = false;
  inputMessage = '';
  messages: ChatMessage[] = [
    {
      text: 'Bonjour ! 🤖 Je suis votre assistant e-commerce. Comment puis-je vous aider aujourd\'hui ?',
      sender: 'bot',
      timestamp: new Date()
    }
  ];

  constructor(
    private chatService: ChatService,
    private cartService: CartService
  ) {}

  toggleChat(): void {
    this.isOpen = !this.isOpen;
  }

  envoyerMessage(): void {
    const text = this.inputMessage.trim();
    if (!text || this.loading) return;

    this.messages.push({ text, sender: 'user', timestamp: new Date() });
    this.inputMessage = '';
    this.loading = true;

    this.chatService.poserQuestion(text).subscribe({
      next: (res) => {
        let reponseNettoyee = res.reponse;

        const matchAction = res.reponse.match(/\[ACTION:ADD_TO_CART:(.*?):(\d+)\]/s);

        if (matchAction) {
          try {
            let jsonString = matchAction[1].trim();

            // 🔑 Correction automatique des virgules décimales ("prix": 8500,00 -> "prix": 8500.00)
            jsonString = jsonString.replace(/("prix"\s*:\s*\d+),(\d+)/g, '$1.$2');

            const produit = JSON.parse(jsonString);
            const quantite = parseInt(matchAction[2], 10) || 1;

            console.log('✅ Produit valide parsé avec succès :', produit);

            // Ajout au panier Angular
            this.cartService.ajouterProduit(produit, quantite);

            // Suppression du tag d'action de l'affichage
            reponseNettoyee = reponseNettoyee.replace(matchAction[0], '').trim();
          } catch (e) {
            console.error('❌ Erreur lors du parsing JSON de l\'action :', e);
          }
        }

        this.messages.push({ text: reponseNettoyee, sender: 'bot', timestamp: new Date() });
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.messages.push({ text: '⚠️ Erreur lors du traitement.', sender: 'bot', timestamp: new Date() });
        this.loading = false;
      }
    });
  }



}
