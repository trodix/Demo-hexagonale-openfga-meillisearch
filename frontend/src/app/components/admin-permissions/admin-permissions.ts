import { Component, signal, computed, inject, ChangeDetectionStrategy, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PermissionService } from '../../services/permission.service';
import {
  UserInfo,
  PermissionTuple,
  AddPermissionRequest,
  EntityInfo,
  TenantInfo,
  TenantMembershipRow,
  EntityPermissionRow,
  ProductPermissionRow
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
    MatTableModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatCheckboxModule
  ],
  templateUrl: './admin-permissions.html',
  styleUrl: './admin-permissions.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminPermissions implements OnInit {
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  // État de chargement
  protected readonly loading = signal(false);
  protected readonly loadingPermissions = signal(false);

  // Données de base
  protected readonly users = signal<UserInfo[]>([]);
  protected readonly selectedUser = signal<UserInfo | null>(null);
  protected readonly tenants = signal<TenantInfo[]>([]);
  protected readonly entities = signal<EntityInfo[]>([]);

  // Permissions brutes de l'utilisateur
  protected readonly userPermissions = signal<PermissionTuple[]>([]);

  protected readonly searchControl = new FormControl('');
  protected readonly productIdControl = new FormControl('');

  protected readonly displayedColumns = ['username', 'displayName', 'actions'];
  protected readonly tenantColumns = ['name', 'member'];
  protected readonly entityColumns = ['name', 'read', 'write', 'delete'];
  protected readonly productColumns = ['productId', 'read', 'write', 'delete', 'actions'];

  protected readonly filteredUsers = computed(() => {
    const search = this.searchControl.value?.toLowerCase() || '';
    return this.users().filter(u =>
      u.username.toLowerCase().includes(search) ||
      u.displayName.toLowerCase().includes(search)
    );
  });

  // Tableaux structurés pour l'affichage
  protected readonly tenantRows = computed<TenantMembershipRow[]>(() => {
    const tenants = this.tenants();
    const permissions = this.userPermissions();

    return tenants.map(tenant => ({
      tenant,
      isMember: permissions.some(p =>
        p.objectType === 'tenant' &&
        p.objectId === tenant.id &&
        p.relation === 'member'
      )
    }));
  });

  protected readonly entityRows = computed<EntityPermissionRow[]>(() => {
    const entities = this.entities();
    const permissions = this.userPermissions();

    return entities.map(entity => ({
      entity,
      hasRead: permissions.some(p =>
        p.objectType === 'entity' &&
        p.objectId === entity.id &&
        p.relation === 'read'
      ),
      hasWrite: permissions.some(p =>
        p.objectType === 'entity' &&
        p.objectId === entity.id &&
        p.relation === 'write'
      ),
      hasDelete: permissions.some(p =>
        p.objectType === 'entity' &&
        p.objectId === entity.id &&
        p.relation === 'delete'
      )
    }));
  });

  protected readonly productRows = computed<ProductPermissionRow[]>(() => {
    const permissions = this.userPermissions();

    // Grouper les permissions produits par productId
    const productMap = new Map<string, ProductPermissionRow>();

    permissions
      .filter(p => p.objectType === 'product')
      .forEach(p => {
        if (!productMap.has(p.objectId)) {
          productMap.set(p.objectId, {
            productId: p.objectId,
            hasRead: false,
            hasWrite: false,
            hasDelete: false
          });
        }

        const row = productMap.get(p.objectId)!;
        if (p.relation === 'read') row.hasRead = true;
        if (p.relation === 'write') row.hasWrite = true;
        if (p.relation === 'delete') row.hasDelete = true;
      });

    return Array.from(productMap.values());
  });

  ngOnInit(): void {
    this.loadUsers();
    this.loadEntities();
    this.loadAdminTenants();
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

  private loadAdminTenants(): void {
    this.permissionService.listAdminTenants().subscribe({
      next: (response) => {
        this.tenants.set(response.tenants);
      },
      error: (err) => console.error('Error loading tenants:', err)
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

  protected toggleTenantMembership(tenantId: string, currentValue: boolean): void {
    const user = this.selectedUser();
    if (!user) return;

    const request: AddPermissionRequest = {
      username: user.username,
      objectType: 'tenant',
      objectId: tenantId,
      relation: 'member'
    };

    const action = currentValue
      ? this.permissionService.removePermission(request)
      : this.permissionService.addPermission(request);

    action.subscribe({
      next: () => {
        // Mise à jour optimiste locale
        if (currentValue) {
          // Retirer la permission
          this.userPermissions.update(perms =>
            perms.filter(p =>
              !(p.objectType === 'tenant' && p.objectId === tenantId && p.relation === 'member')
            )
          );
        } else {
          // Ajouter la permission
          this.userPermissions.update(perms => [
            ...perms,
            { objectType: 'tenant', objectId: tenantId, relation: 'member' }
          ]);
        }
        this.showNotification(currentValue ? 'Retiré du tenant' : 'Ajouté au tenant');
      },
      error: (err) => {
        console.error('Error toggling tenant membership:', err);
        this.showNotification('Erreur lors de la modification');
        // En cas d'erreur, recharger pour être sûr d'avoir l'état correct
        this.loadUserPermissions(user.username);
      }
    });
  }

  protected toggleEntityPermission(
    entityId: string,
    relation: 'read' | 'write' | 'delete',
    currentValue: boolean
  ): void {
    const user = this.selectedUser();
    if (!user) return;

    const request: AddPermissionRequest = {
      username: user.username,
      objectType: 'entity',
      objectId: entityId,
      relation
    };

    const action = currentValue
      ? this.permissionService.removePermission(request)
      : this.permissionService.addPermission(request);

    action.subscribe({
      next: () => {
        // Mise à jour optimiste locale
        if (currentValue) {
          // Retirer la permission
          this.userPermissions.update(perms =>
            perms.filter(p =>
              !(p.objectType === 'entity' && p.objectId === entityId && p.relation === relation)
            )
          );
        } else {
          // Ajouter la permission
          this.userPermissions.update(perms => [
            ...perms,
            { objectType: 'entity', objectId: entityId, relation }
          ]);
        }
        this.showNotification('Permission mise à jour');
      },
      error: (err) => {
        console.error('Error toggling entity permission:', err);
        this.showNotification('Erreur lors de la modification');
        // En cas d'erreur, recharger pour être sûr d'avoir l'état correct
        this.loadUserPermissions(user.username);
      }
    });
  }

  protected toggleProductPermission(
    productId: string,
    relation: 'read' | 'write' | 'delete',
    currentValue: boolean
  ): void {
    const user = this.selectedUser();
    if (!user) return;

    const request: AddPermissionRequest = {
      username: user.username,
      objectType: 'product',
      objectId: productId,
      relation
    };

    const action = currentValue
      ? this.permissionService.removePermission(request)
      : this.permissionService.addPermission(request);

    action.subscribe({
      next: () => {
        // Mise à jour optimiste locale
        if (currentValue) {
          // Retirer la permission
          this.userPermissions.update(perms =>
            perms.filter(p =>
              !(p.objectType === 'product' && p.objectId === productId && p.relation === relation)
            )
          );
        } else {
          // Ajouter la permission
          this.userPermissions.update(perms => [
            ...perms,
            { objectType: 'product', objectId: productId, relation }
          ]);
        }
        this.showNotification('Permission mise à jour');
      },
      error: (err) => {
        console.error('Error toggling product permission:', err);
        this.showNotification('Erreur lors de la modification');
        // En cas d'erreur, recharger pour être sûr d'avoir l'état correct
        this.loadUserPermissions(user.username);
      }
    });
  }

  protected addProduct(): void {
    const user = this.selectedUser();
    const productId = this.productIdControl.value;
    if (!user || !productId) return;

    // Ajouter une permission read par défaut
    const request: AddPermissionRequest = {
      username: user.username,
      objectType: 'product',
      objectId: productId,
      relation: 'read'
    };

    this.permissionService.addPermission(request).subscribe({
      next: () => {
        // Mise à jour optimiste locale
        this.userPermissions.update(perms => [
          ...perms,
          { objectType: 'product', objectId: productId, relation: 'read' }
        ]);
        this.productIdControl.setValue('');
        this.showNotification('Produit ajouté');
      },
      error: (err) => {
        console.error('Error adding product:', err);
        this.showNotification('Erreur lors de l\'ajout');
        // En cas d'erreur, recharger pour être sûr d'avoir l'état correct
        this.loadUserPermissions(user.username);
      }
    });
  }

  protected removeProduct(productId: string): void {
    const user = this.selectedUser();
    if (!user) return;

    // Supprimer toutes les permissions pour ce produit
    const permissions = this.userPermissions().filter(
      p => p.objectType === 'product' && p.objectId === productId
    );

    // Créer des observables pour chaque suppression
    const deletions = permissions.map(p =>
      this.permissionService.removePermission({
        username: user.username,
        objectType: p.objectType,
        objectId: p.objectId,
        relation: p.relation
      })
    );

    // Exécuter toutes les suppressions en parallèle
    forkJoin(deletions).subscribe({
      next: () => {
        // Mise à jour optimiste locale - retirer toutes les permissions du produit
        this.userPermissions.update(perms =>
          perms.filter(p => !(p.objectType === 'product' && p.objectId === productId))
        );
        this.showNotification('Produit supprimé');
      },
      error: (err) => {
        console.error('Error removing product:', err);
        this.showNotification('Erreur lors de la suppression');
        // En cas d'erreur, recharger pour être sûr d'avoir l'état correct
        this.loadUserPermissions(user.username);
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
