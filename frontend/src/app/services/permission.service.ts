import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  PermissionCheckRequest,
  PermissionCheckResponse,
  UserListResponse,
  UserPermissionsResponse,
  AddPermissionRequest,
  EntityListResponse
} from '../models/permission.model';

@Injectable({
  providedIn: 'root'
})
export class PermissionService {
  private readonly http = inject(HttpClient);

  checkPermissions(request: PermissionCheckRequest): Observable<PermissionCheckResponse> {
    return this.http.post<PermissionCheckResponse>('/api/permissions/check', request);
  }

  listUsers(): Observable<UserListResponse> {
    return this.http.get<UserListResponse>('/api/admin/users');
  }

  getUserPermissions(username: string): Observable<UserPermissionsResponse> {
    return this.http.get<UserPermissionsResponse>(`/api/admin/users/${username}/permissions`);
  }

  addPermission(request: AddPermissionRequest): Observable<void> {
    return this.http.post<void>('/api/admin/permissions', request);
  }

  removePermission(request: AddPermissionRequest): Observable<void> {
    return this.http.request<void>('DELETE', '/api/admin/permissions', { body: request });
  }

  listEntities(): Observable<EntityListResponse> {
    return this.http.get<EntityListResponse>('/api/admin/entities');
  }
}
