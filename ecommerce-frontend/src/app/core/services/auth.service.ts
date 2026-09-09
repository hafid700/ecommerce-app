import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';

export interface AuthResponse {
  token: string;
  username: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/api/auth';
  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  private isBrowser: boolean;

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) platformId: Object // 👈 Permet de savoir si on est dans le navigateur ou dans Node.js
  ) {
    this.isBrowser = isPlatformBrowser(platformId);

    // Ne lit localStorage QUE si nous sommes exécutés dans un vrai navigateur Web
    if (this.isBrowser) {
      const savedUser = localStorage.getItem('auth_user');
      if (savedUser) {
        this.currentUserSubject.next(JSON.parse(savedUser));
      }
    }
  }

  register(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user, { responseType: 'text' });
  }

  login(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        if (this.isBrowser) {
          localStorage.setItem('auth_token', response.token);
          localStorage.setItem('auth_user', JSON.stringify(response));
        }
        this.currentUserSubject.next(response);
      })
    );
  }

  logout(): void {
    if (this.isBrowser) {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('auth_user');
    }
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return this.isBrowser ? localStorage.getItem('auth_token') : null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    const user = this.currentUserSubject.value;
    return user ? user.role === 'ROLE_ADMIN' : false;
  }
}