import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthCredentials, AuthState } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);
  private readonly authState = signal<AuthState>({
    isAuthenticated: false,
    username: null,
    credentials: null,
    tenantId: null,
    isAdmin: false
  });

  readonly isAuthenticated = computed(() => this.authState().isAuthenticated);
  readonly username = computed(() => this.authState().username);
  readonly tenantId = computed(() => this.authState().tenantId);
  readonly isAdmin = computed(() => this.authState().isAdmin);

  constructor() {
    this.loadFromStorage();
  }

  login(credentials: AuthCredentials): void {
    const encodedCredentials = btoa(`${credentials.username}:${credentials.password}`);

    this.authState.set({
      isAuthenticated: true,
      username: credentials.username,
      credentials: encodedCredentials,
      tenantId: credentials.tenantId,
      isAdmin: false
    });

    // Vérifier le rôle admin
    this.checkAdminRole(credentials.tenantId);

    this.saveToStorage(credentials.username, encodedCredentials, credentials.tenantId);
    this.router.navigate(['/search']);
  }

  private checkAdminRole(tenantId: string): void {
    const checkRequest = {
      checks: [
        {
          object: `tenant:${tenantId}`,
          relation: 'admin'
        }
      ]
    };

    console.log('[AUTH] Vérification du rôle admin pour tenant:', tenantId);
    this.http.post<{ results: { [key: string]: boolean } }>('/api/permissions/check', checkRequest)
      .subscribe({
        next: (response) => {
          console.log('[AUTH] Réponse du check admin:', response);
          const isAdmin = response.results[`tenant:${tenantId} admin`] || false;
          console.log('[AUTH] isAdmin défini à:', isAdmin);
          this.authState.update(state => ({ ...state, isAdmin }));
          sessionStorage.setItem('auth_is_admin', String(isAdmin));
        },
        error: (err) => {
          console.error('[AUTH] Erreur lors du check admin:', err);
          this.authState.update(state => ({ ...state, isAdmin: false }));
          sessionStorage.setItem('auth_is_admin', 'false');
        }
      });
  }

  logout(): void {
    this.authState.set({
      isAuthenticated: false,
      username: null,
      credentials: null,
      tenantId: null,
      isAdmin: false
    });

    this.clearStorage();
    this.router.navigate(['/login']);
  }

  getAuthHeader(): string | null {
    return this.authState().credentials;
  }

  getTenantId(): string | null {
    return this.authState().tenantId;
  }

  private saveToStorage(username: string, credentials: string, tenantId: string): void {
    sessionStorage.setItem('auth_username', username);
    sessionStorage.setItem('auth_credentials', credentials);
    sessionStorage.setItem('auth_tenant_id', tenantId);
  }

  private loadIsAdminFromStorage(): boolean {
    return sessionStorage.getItem('auth_is_admin') === 'true';
  }

  private loadFromStorage(): void {
    const username = sessionStorage.getItem('auth_username');
    const credentials = sessionStorage.getItem('auth_credentials');
    const tenantId = sessionStorage.getItem('auth_tenant_id');
    const isAdmin = this.loadIsAdminFromStorage();

    if (username && credentials && tenantId) {
      this.authState.set({
        isAuthenticated: true,
        username,
        credentials,
        tenantId,
        isAdmin
      });

      // Re-vérifier le rôle admin au chargement
      if (tenantId) {
        this.checkAdminRole(tenantId);
      }
    }
  }

  private clearStorage(): void {
    sessionStorage.removeItem('auth_username');
    sessionStorage.removeItem('auth_credentials');
    sessionStorage.removeItem('auth_tenant_id');
    sessionStorage.removeItem('auth_is_admin');
  }
}
