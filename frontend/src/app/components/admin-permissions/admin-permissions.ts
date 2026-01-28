import { Component, signal, computed, inject, ChangeDetectionStrategy, OnInit } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PermissionService } from '../../services/permission.service';
import { AuthService } from '../../services/auth.service';
import {
  UserInfo,
  PermissionTuple,
  AddPermissionRequest,
  EntityInfo
} from '../../models/permission.model';

@Component({
  selector: 'app-admin-permissions',
  imports: [
    ReactiveFormsModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTableModule,
    MatCardModule,
    MatChipsModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './admin-permissions.html',
  styleUrl: './admin-permissions.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminPermissions implements OnInit {
  private readonly permissionService = inject(PermissionService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly users = signal<UserInfo[]>([]);
  protected readonly selectedUser = signal<UserInfo | null>(null);
  protected readonly userPermissions = signal<PermissionTuple[]>([]);
  protected readonly entities = signal<EntityInfo[]>([]);
  protected readonly loading = signal(false);
  protected readonly loadingPermissions = signal(false);

  protected readonly searchControl = new FormControl('');
  protected readonly entityControl = new FormControl<string | null>(null);
  protected readonly entityRelationControl = new FormControl<string | null>(null);
  protected readonly productIdControl = new FormControl('');
  protected readonly productRelationControl = new FormControl<string | null>(null);

  // Convertir les valueChanges en signals pour la réactivité
  protected readonly selectedEntityId = toSignal(this.entityControl.valueChanges, { initialValue: null });

  protected readonly displayedColumns = ['username', 'displayName', 'actions'];
  protected readonly productRelations = ['read', 'write', 'delete'];

  protected readonly filteredUsers = computed(() => {
    const search = this.searchControl.value?.toLowerCase() || '';
    return this.users().filter(u =>
      u.username.toLowerCase().includes(search) ||
      u.displayName.toLowerCase().includes(search)
    );
  });

  protected readonly availableEntityRelations = computed(() => {
    const entityId = this.selectedEntityId();
    if (!entityId) {
      return [];
    }
    const entity = this.entities().find(e => e.id === entityId);
    return entity?.availableRelations || [];
  });

  protected readonly isTenantMember = computed(() => {
    const user = this.selectedUser();
    const tenantId = this.authService.tenantId();
    if (!user || !tenantId) return false;
    return this.userPermissions().some(p =>
      p.objectType === 'tenant' &&
      p.objectId === tenantId &&
      p.relation === 'member'
    );
  });

  ngOnInit(): void {
    this.loadUsers();
    this.loadEntities();
  }

  private loadUsers(): void {
    this.loading.set(true);
    this.permissionService.listUsers().subscribe({
      next: (response) => {
        this.users.set(response.users);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading users:', err);
        this.loading.set(false);
      }
    });
  }

  private loadEntities(): void {
    this.permissionService.listEntities().subscribe({
      next: (response) => {
        this.entities.set(response.entities);
      },
      error: (err) => console.error('Error loading entities:', err)
    });
  }

  protected selectUser(user: UserInfo): void {
    this.selectedUser.set(user);
    this.loadUserPermissions(user.username);
  }

  private loadUserPermissions(username: string): void {
    this.loadingPermissions.set(true);
    this.permissionService.getUserPermissions(username).subscribe({
      next: (response) => {
        this.userPermissions.set(response.permissions);
        this.loadingPermissions.set(false);
      },
      error: (err) => {
        console.error('Error loading permissions:', err);
        this.loadingPermissions.set(false);
      }
    });
  }

  protected toggleTenantMembership(): void {
    const user = this.selectedUser();
    const tenantId = this.authService.tenantId();
    if (!user || !tenantId) return;

    const request: AddPermissionRequest = {
      username: user.username,
      objectType: 'tenant',
      objectId: tenantId,
      relation: 'member'
    };

    const action = this.isTenantMember()
      ? this.permissionService.removePermission(request)
      : this.permissionService.addPermission(request);

    action.subscribe({
      next: () => {
        this.loadUserPermissions(user.username);
        this.showNotification('Permission mise à jour');
      },
      error: (err) => {
        console.error('Error updating permission:', err);
        this.showNotification('Erreur lors de la mise à jour');
      }
    });
  }

  protected addEntityPermission(): void {
    const user = this.selectedUser();
    const entityId = this.entityControl.value;
    const relation = this.entityRelationControl.value;
    if (!user || !entityId || !relation) return;

    const request: AddPermissionRequest = {
      username: user.username,
      objectType: 'entity',
      objectId: entityId,
      relation: relation
    };

    this.permissionService.addPermission(request).subscribe({
      next: () => {
        this.loadUserPermissions(user.username);
        this.entityControl.setValue(null);
        this.entityRelationControl.setValue(null);
        this.showNotification('Permission ajoutée');
      },
      error: (err) => {
        console.error('Error adding permission:', err);
        this.showNotification('Erreur lors de l\'ajout');
      }
    });
  }

  protected removePermission(permission: PermissionTuple): void {
    const user = this.selectedUser();
    if (!user) return;

    const request: AddPermissionRequest = {
      username: user.username,
      objectType: permission.objectType,
      objectId: permission.objectId,
      relation: permission.relation
    };

    this.permissionService.removePermission(request).subscribe({
      next: () => {
        this.loadUserPermissions(user.username);
        this.showNotification('Permission supprimée');
      },
      error: (err) => {
        console.error('Error removing permission:', err);
        this.showNotification('Erreur lors de la suppression');
      }
    });
  }

  protected addProductPermission(): void {
    const user = this.selectedUser();
    const productId = this.productIdControl.value;
    const relation = this.productRelationControl.value;
    if (!user || !productId || !relation) return;

    const request: AddPermissionRequest = {
      username: user.username,
      objectType: 'product',
      objectId: productId,
      relation: relation
    };

    this.permissionService.addPermission(request).subscribe({
      next: () => {
        this.loadUserPermissions(user.username);
        this.productIdControl.setValue('');
        this.productRelationControl.setValue(null);
        this.showNotification('Permission ajoutée');
      },
      error: (err) => {
        console.error('Error adding permission:', err);
        this.showNotification('Erreur lors de l\'ajout');
      }
    });
  }

  private showNotification(message: string): void {
    this.snackBar.open(message, 'Fermer', { duration: 3000 });
  }

  protected goBack(): void {
    this.router.navigate(['/search']);
  }
}
