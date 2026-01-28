import { Injectable, signal, inject } from '@angular/core';
import { Observable, forkJoin, throwError } from 'rxjs';
import { tap, map, switchMap, catchError } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PermissionService } from './permission.service';
import {
  EnrichedPermissionsResponse,
  PermissionTuple,
  AddPermissionRequest
} from '../models/permission.model';

@Injectable({
  providedIn: 'root'
})
export class PermissionManagerService {
  private readonly permissionService = inject(PermissionService);
  private readonly snackBar = inject(MatSnackBar);

  private readonly _enrichedPermissions = signal<EnrichedPermissionsResponse | null>(null);
  private readonly _userPermissions = signal<PermissionTuple[]>([]);
  private readonly _loading = signal(false);

  readonly enrichedPermissions = this._enrichedPermissions.asReadonly();
  readonly userPermissions = this._userPermissions.asReadonly();
  readonly loading = this._loading.asReadonly();
  loadPermissions(username: string): Observable<void> {
    this._loading.set(true);

    return forkJoin({
      basic: this.permissionService.getUserPermissions(username),
      enriched: this.permissionService.getEnrichedPermissions(username)
    }).pipe(
      tap(({ basic, enriched }) => {
        this._userPermissions.set(basic.permissions);
        this._enrichedPermissions.set(enriched);
        this._loading.set(false);
      }),
      map(() => undefined),
      catchError(err => {
        this._loading.set(false);
        return throwError(() => err);
      })
    );
  }

  togglePermission(
    username: string,
    objectType: string,
    objectId: string,
    relation: string,
    currentValue: boolean
  ): Observable<void> {
    const request: AddPermissionRequest = {
      username,
      objectType,
      objectId,
      relation
    };

    const action = currentValue
      ? this.permissionService.removePermission(request)
      : this.permissionService.addPermission(request);

    return action.pipe(
      switchMap(() => this.permissionService.getEnrichedPermissions(username)),
      tap(enriched => {
        this._enrichedPermissions.set(enriched);
        this.showNotification('Permission mise à jour');
      }),
      map(() => undefined),
      catchError(err => {
        this.showNotification('Erreur lors de la modification');
        return throwError(() => err);
      })
    );
  }

  reset(): void {
    this._enrichedPermissions.set(null);
    this._userPermissions.set([]);
    this._loading.set(false);
  }

  private showNotification(message: string): void {
    this.snackBar.open(message, 'Fermer', { duration: 3000 });
  }
}
