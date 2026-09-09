import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Categorie {
  id?: number;
  nom: string;
}

export interface Produit {
  id?: number;
  nom: string;
  prix: number;
  quantiteStock: number;
  categorie?: Categorie;
}



@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = '/api';

  constructor(private http: HttpClient) {}

  getProduits(): Observable<Produit[]> {
    return this.http.get<Produit[]>(`${this.apiUrl}/produits`);
  }

  getCategories(): Observable<Categorie[]> {
    return this.http.get<Categorie[]>(`${this.apiUrl}/categories`);
  }

  ajouterProduit(produit: Produit): Observable<Produit> {
    return this.http.post<Produit>(`${this.apiUrl}/produits`, produit);
  }

  modifierProduit(id: number, produit: Produit): Observable<Produit> {
    return this.http.put<Produit>(`${this.apiUrl}/produits/${id}`, produit);
  }

  supprimerProduit(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/produits/${id}`);
  }

}
