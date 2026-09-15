import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface ChatMessage {
  text: string;
  sender: 'user' | 'bot';
  timestamp: Date;
}

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = 'http://localhost:8080/api/agent/ask';
  private conversationId: string;

  constructor(private http: HttpClient, private authService: AuthService) {
    this.conversationId = this.getOrCreateConversationId();
  }

  private getOrCreateConversationId(): string {
    if (this.authService.isLoggedIn()) {
      return `user_${this.authService.getCurrentUser()}`;
    }
    let guestId = sessionStorage.getItem('chat_conversation_id');
    if (!guestId) {
      guestId = `guest_${crypto.randomUUID()}`;
      sessionStorage.setItem('chat_conversation_id', guestId);
    }
    return guestId;
  }

  poserQuestion(question: string): Observable<{ conversationId: string; reponse: string }> {
    return this.http.post<{ conversationId: string; reponse: string }>(this.apiUrl, {
      conversationId: this.conversationId,
      question
    });
  }
}
