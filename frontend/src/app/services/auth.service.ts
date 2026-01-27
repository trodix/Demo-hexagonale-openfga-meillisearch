import { Injectable, signal, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthCredentials, AuthState } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly router = inject(Router);
  private readonly authState = signal<AuthState>({
    isAuthenticated: false,
    username: null,
    credentials: null,
    tenantId: null
  });

  readonly isAuthenticated = computed(() => this.authState().isAuthenticated);
  readonly username = computed(() => this.authState().username);
  readonly tenantId = computed(() => this.authState().tenantId);

  constructor() {
    this.loadFromStorage();
  }

  login(credentials: AuthCredentials): void {
    const encodedCredentials = btoa(`${credentials.username}:${credentials.password}`);

    this.authState.set({
      isAuthenticated: true,
      username: credentials.username,
      credentials: encodedCredentials,
      tenantId: credentials.tenantId
    });

    this.saveToStorage(credentials.username, encodedCredentials, credentials.tenantId);
    this.router.navigate(['/search']);
  }

  logout(): void {
    this.authState.set({
      isAuthenticated: false,
      username: null,
      credentials: null,
      tenantId: null
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

  private loadFromStorage(): void {
    const username = sessionStorage.getItem('auth_username');
    const credentials = sessionStorage.getItem('auth_credentials');
    const tenantId = sessionStorage.getItem('auth_tenant_id');

    if (username && credentials && tenantId) {
      this.authState.set({
        isAuthenticated: true,
        username,
        credentials,
        tenantId
      });
    }
  }

  private clearStorage(): void {
    sessionStorage.removeItem('auth_username');
    sessionStorage.removeItem('auth_credentials');
    sessionStorage.removeItem('auth_tenant_id');
  }
}
