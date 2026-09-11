import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LigneCommande {
  id?: number;
  quantite: number;
  produit: {
    id: number;
    nom: string;
    prix: number;
  };
}

export interface ClientInfo {
  id?: number;
  nom?: string;
  email?: string;
}

export interface Commande {
  id: number;
  dateCommande: string;
  statut?: string;
  status?: string;
  client?:ClientInfo;
  lignes: LigneCommande[];
}

export interface LigneCommandeRequest {
  quantite: number;
  produit: { id: number };
}

export interface CommandeRequest {
  client: {
    id?: number
    nom?: string;
    email?: string;
  };
  lignes: LigneCommandeRequest[];
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = '/api/commandes';

  constructor(private http: HttpClient) {}

  passerCommande(commande: CommandeRequest): Observable<any> {
    return this.http.post<any>(this.apiUrl, commande);
  }

  getCommandesParClient(clientId: number): Observable<Commande[]> {
    return this.http.get<Commande[]>(`${this.apiUrl}/client/${clientId}`);
  }

  getToutesLesCommandes(): Observable<Commande[]> {
    return this.http.get<Commande[]>(this.apiUrl);
  }

  changerStatutCommande(commandeId: number, statut: string): Observable<Commande> {
    return this.http.put<Commande>(`${this.apiUrl}/${commandeId}/statut`, { statut });
  }

  getCommandesParClientEmail(email: string): Observable<Commande[]> {
    return this.http.get<Commande[]>(`${this.apiUrl}/client/email/${email}`);
  }



}
