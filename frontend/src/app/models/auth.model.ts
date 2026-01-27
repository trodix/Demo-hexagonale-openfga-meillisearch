export interface AuthCredentials {
  username: string;
  password: string;
  tenantId: string;
}

export interface AuthState {
  isAuthenticated: boolean;
  username: string | null;
  credentials: string | null;
  tenantId: string | null;
}

export interface Tenant {
  id: string;
  name: string;
}
