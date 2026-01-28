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
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PermissionService } from '../../services/permission.service';
import { AuthService } from '../../services/auth.service';
import {
  UserInfo,
  PermissionTuple,
  AddPermissionRequest,
  EntityInfo,
  TenantInfo,
  TenantMembershipRow,
  EntityPermissionRow,
  ProductPermissionRow,
  EnrichedPermissionsResponse
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
    MatCheckboxModule,
    MatTooltipModule
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

  // État de chargement
  protected readonly loading = signal(false);
  protected readonly loadingPermissions = signal(false);

  // Données de base
  protected readonly users = signal<UserInfo[]>([]);
  protected readonly selectedUser = signal<UserInfo | null>(null);
  protected readonly tenants = signal<TenantInfo[]>([]);
  protected readonly entities = signal<EntityInfo[]>([]);
  protected readonly currentTenantId = this.authService.tenantId;

  // Permissions brutes de l'utilisateur (pour les updates optimistes des tenants)
  protected readonly userPermissions = signal<PermissionTuple[]>([]);

  // Permissions enrichies (avec détection des indirectes)
  protected readonly enrichedPermissions = signal<EnrichedPermissionsResponse | null>(null);

  protected readonly searchControl = new FormControl('');
  protected readonly productIdControl = new FormControl('');

  protected readonly displayedColumns = ['username', 'displayName', 'actions'];
  protected readonly tenantColumns = ['name', 'member', 'admin'];
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
    const enriched = this.enrichedPermissions();
    const tenants = this.tenants();
    const selectedUser = this.selectedUser();
    const currentUser = this.authService.username();
    const currentTenant = this.currentTenantId();

    if (!enriched) {
      // Pas encore de données enrichies, retourner des données vides
      return tenants.map(tenant => ({
        tenant,
        isMember: false,
        isAdmin: false,
        isCurrentTenantForCurrentUser: false
      }));
    }

    // Mapper les données enrichies du backend vers nos rows
    return tenants.map(tenant => {
      const enrichedTenant = enriched.tenants.find(t => t.tenantId === tenant.id);
      const isCurrentTenantForCurrentUser =
        selectedUser?.username === currentUser &&
        tenant.id === currentTenant;

      if (!enrichedTenant) {
        return {
          tenant,
          isMember: false,
          isAdmin: false,
          isCurrentTenantForCurrentUser
        };
      }

      return {
        tenant,
        isMember: enrichedTenant.isMember,
        isAdmin: enrichedTenant.isAdmin,
        isCurrentTenantForCurrentUser
      };
    });
  });

  protected readonly entityRows = computed<EntityPermissionRow[]>(() => {
    const enriched = this.enrichedPermissions();
    const entities = this.entities();

    if (!enriched) {
      // Pas encore de données enrichies, retourner des données vides
      return entities.map(entity => ({
        entity,
        hasRead: false,
        hasWrite: false,
        hasDelete: false,
        readIsIndirect: false,
        writeIsIndirect: false,
        deleteIsIndirect: false
      }));
    }

    // Mapper les données enrichies du backend vers nos rows
    return entities.map(entity => {
      const enrichedEntity = enriched.entities.find(e => e.entityId === entity.id);

      if (!enrichedEntity) {
        return {
          entity,
          hasRead: false,
          hasWrite: false,
          hasDelete: false,
          readIsIndirect: false,
          writeIsIndirect: false,
          deleteIsIndirect: false
        };
      }

      return {
        entity,
        hasRead: enrichedEntity.read.hasPermission,
        hasWrite: enrichedEntity.write.hasPermission,
        hasDelete: enrichedEntity.delete.hasPermission,
        readIsIndirect: enrichedEntity.read.hasPermission && !enrichedEntity.read.isDirect,
        writeIsIndirect: enrichedEntity.write.hasPermission && !enrichedEntity.write.isDirect,
        deleteIsIndirect: enrichedEntity.delete.hasPermission && !enrichedEntity.delete.isDirect
      };
    });
  });

  protected readonly productRows = computed<ProductPermissionRow[]>(() => {
    const enriched = this.enrichedPermissions();

    if (!enriched) {
      return [];
    }

    // Mapper les données enrichies du backend vers nos rows
    return enriched.products.map(product => ({
      productId: product.productId,
      hasRead: product.read.hasPermission,
      hasWrite: product.write.hasPermission,
      hasDelete: product.delete.hasPermission,
      readIsIndirect: product.read.hasPermission && !product.read.isDirect,
      writeIsIndirect: product.write.hasPermission && !product.write.isDirect,
      deleteIsIndirect: product.delete.hasPermission && !product.delete.isDirect
    }));
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

    // Charger les deux endpoints en parallèle
    const basicPerms$ = this.permissionService.getUserPermissions(username);
    const enrichedPerms$ = this.permissionService.getEnrichedPermissions(username);

    // Combiner les résultats
    basicPerms$.subscribe({
      next: (response) => {
        this.userPermissions.set(response.permissions);
      },
      error: (err) => {
        console.error('Error loading basic permissions:', err);
      }
    });

    enrichedPerms$.subscribe({
      next: (response) => {
        this.enrichedPermissions.set(response);
        this.loadingPermissions.set(false);
      },
      error: (err) => {
        console.error('Error loading enriched permissions:', err);
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
        // Recharger les permissions enrichies
        this.permissionService.getEnrichedPermissions(user.username).subscribe({
          next: (response) => {
            this.enrichedPermissions.set(response);
            this.showNotification(currentValue ? 'Retiré du tenant' : 'Ajouté au tenant');
          },
          error: (err) => {
            console.error('Error reloading enriched permissions:', err);
            this.showNotification('Membership mis à jour (rechargement échoué)');
          }
        });
      },
      error: (err) => {
        console.error('Error toggling tenant membership:', err);
        this.showNotification('Erreur lors de la modification');
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
        // Recharger les permissions enrichies pour obtenir l'état calculé par OpenFGA
        this.permissionService.getEnrichedPermissions(user.username).subscribe({
          next: (response) => {
            this.enrichedPermissions.set(response);
            this.showNotification('Permission mise à jour');
          },
          error: (err) => {
            console.error('Error reloading enriched permissions:', err);
            this.showNotification('Permission mise à jour (rechargement échoué)');
          }
        });
      },
      error: (err) => {
        console.error('Error toggling entity permission:', err);
        this.showNotification('Erreur lors de la modification');
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
        // Recharger les permissions enrichies pour obtenir l'état calculé par OpenFGA
        this.permissionService.getEnrichedPermissions(user.username).subscribe({
          next: (response) => {
            this.enrichedPermissions.set(response);
            this.showNotification('Permission mise à jour');
          },
          error: (err) => {
            console.error('Error reloading enriched permissions:', err);
            this.showNotification('Permission mise à jour (rechargement échoué)');
          }
        });
      },
      error: (err) => {
        console.error('Error toggling product permission:', err);
        this.showNotification('Erreur lors de la modification');
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
        // Recharger les permissions enrichies
        this.permissionService.getEnrichedPermissions(user.username).subscribe({
          next: (response) => {
            this.enrichedPermissions.set(response);
            this.productIdControl.setValue('');
            this.showNotification('Produit ajouté');
          },
          error: (err) => {
            console.error('Error reloading enriched permissions:', err);
            this.productIdControl.setValue('');
            this.showNotification('Produit ajouté (rechargement échoué)');
          }
        });
      },
      error: (err) => {
        console.error('Error adding product:', err);
        this.showNotification('Erreur lors de l\'ajout');
      }
    });
  }

  protected removeProduct(productId: string): void {
    const user = this.selectedUser();
    if (!user) return;

    // Trouver toutes les permissions directes pour ce produit dans les permissions basiques
    const permissions = this.userPermissions().filter(
      p => p.objectType === 'product' && p.objectId === productId
    );

    if (permissions.length === 0) {
      this.showNotification('Aucune permission directe à supprimer');
      return;
    }

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
        // Recharger les permissions enrichies
        this.permissionService.getEnrichedPermissions(user.username).subscribe({
          next: (response) => {
            this.enrichedPermissions.set(response);
            this.showNotification('Produit supprimé');
          },
          error: (err) => {
            console.error('Error reloading enriched permissions:', err);
            this.showNotification('Produit supprimé (rechargement échoué)');
          }
        });
      },
      error: (err) => {
        console.error('Error removing product:', err);
        this.showNotification('Erreur lors de la suppression');
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
