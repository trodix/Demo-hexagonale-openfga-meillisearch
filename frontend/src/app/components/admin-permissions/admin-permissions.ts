import { Component, signal, computed, inject, ChangeDetectionStrategy, OnInit, OnDestroy } from '@angular/core';
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
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PermissionService } from '../../services/permission.service';
import { PermissionManagerService } from '../../services/permission-manager.service';
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
  EnrichedPermissionsResponse,
  GenericPermissionCheckResponse,
  ResourcePermissionStatus
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
    MatTooltipModule,
    MatSelectModule
  ],
  templateUrl: './admin-permissions.html',
  styleUrl: './admin-permissions.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminPermissions implements OnInit, OnDestroy {
  private readonly permissionService = inject(PermissionService);
  private readonly permissionManager = inject(PermissionManagerService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  // État de chargement local
  protected readonly loadingUsers = signal(false);

  // Données de base
  protected readonly users = signal<UserInfo[]>([]);
  protected readonly selectedUser = signal<UserInfo | null>(null);
  protected readonly tenants = signal<TenantInfo[]>([]);
  protected readonly entities = signal<EntityInfo[]>([]);
  protected readonly currentTenantId = this.authService.tenantId;

  protected readonly loading = computed(() =>
    this.loadingUsers() || this.permissionManager.loading()
  );
  protected readonly enrichedPermissions = this.permissionManager.enrichedPermissions;
  protected readonly userPermissions = this.permissionManager.userPermissions;

  protected readonly searchControl = new FormControl('');
  protected readonly productIdControl = new FormControl('');
  protected readonly checkProductIdControl = new FormControl('');
  protected readonly checkRelationControl = new FormControl('read');
  protected readonly checkResult = signal<GenericPermissionCheckResponse | null>(null);
  protected readonly checkLoading = signal(false);

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

  protected readonly tenantRows = computed<TenantMembershipRow[]>(() => {
    const enriched = this.enrichedPermissions();
    const tenants = this.tenants();
    const selectedUser = this.selectedUser();
    const currentUser = this.authService.username();
    const currentTenant = this.currentTenantId();

    if (!enriched) {
      return tenants.map(tenant => ({
        tenant,
        isMember: false,
        isAdmin: false,
        isCurrentTenantForCurrentUser: false
      }));
    }

    const tenantResources = enriched.resourcesByType?.['tenant'] || [];

    return tenants.map(tenant => {
      const enrichedTenant = tenantResources.find(t => t.resourceId === tenant.id);
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
        isMember: enrichedTenant.permissions['member']?.isDirect || false,
        isAdmin: enrichedTenant.permissions['admin']?.isDirect || false,
        isCurrentTenantForCurrentUser
      };
    });
  });

  protected readonly entityRows = computed<EntityPermissionRow[]>(() => {
    const enriched = this.enrichedPermissions();
    const entities = this.entities();

    if (!enriched) {
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

    const entityResources = enriched.resourcesByType?.['entity'] || [];

    return entities.map(entity => {
      const enrichedEntity = entityResources.find(e => e.resourceId === entity.id);

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

      const read = enrichedEntity.permissions['read'];
      const write = enrichedEntity.permissions['write'];
      const deletePermission = enrichedEntity.permissions['delete'];

      return {
        entity,
        hasRead: read?.hasPermission || false,
        hasWrite: write?.hasPermission || false,
        hasDelete: deletePermission?.hasPermission || false,
        readIsIndirect: (read?.hasPermission && !read?.isDirect) || false,
        writeIsIndirect: (write?.hasPermission && !write?.isDirect) || false,
        deleteIsIndirect: (deletePermission?.hasPermission && !deletePermission?.isDirect) || false
      };
    });
  });

  protected readonly productRows = computed<ProductPermissionRow[]>(() => {
    const enriched = this.enrichedPermissions();

    if (!enriched) {
      return [];
    }

    const productResources = enriched.resourcesByType?.['product'] || [];

    return productResources.map(product => {
      const read = product.permissions['read'];
      const write = product.permissions['write'];
      const deletePermission = product.permissions['delete'];

      return {
        productId: product.resourceId,
        hasRead: read?.hasPermission || false,
        hasWrite: write?.hasPermission || false,
        hasDelete: deletePermission?.hasPermission || false,
        readIsIndirect: (read?.hasPermission && !read?.isDirect) || false,
        writeIsIndirect: (write?.hasPermission && !write?.isDirect) || false,
        deleteIsIndirect: (deletePermission?.hasPermission && !deletePermission?.isDirect) || false
      };
    });
  });

  ngOnInit(): void {
    this.loadUsers();
    this.loadEntities();
    this.loadAdminTenants();
  }

  ngOnDestroy(): void {
    this.permissionManager.reset();
  }

  private loadUsers(): void {
    this.loadingUsers.set(true);
    this.permissionService.listUsers().subscribe({
      next: (response) => {
        this.users.set(response.users);
        this.loadingUsers.set(false);
      },
      error: () => {
        this.loadingUsers.set(false);
      }
    });
  }

  private loadEntities(): void {
    this.permissionService.listEntities().subscribe({
      next: (response) => {
        this.entities.set(response.entities);
      }
    });
  }

  private loadAdminTenants(): void {
    this.permissionService.listAdminTenants().subscribe({
      next: (response) => {
        this.tenants.set(response.tenants);
      }
    });
  }

  protected selectUser(user: UserInfo): void {
    this.selectedUser.set(user);
    this.permissionManager.loadPermissions(user.username).subscribe();
  }

  protected toggleTenantMembership(tenantId: string, currentValue: boolean): void {
    const user = this.selectedUser();
    if (!user) return;

    this.permissionManager.togglePermission(
      user.username,
      'tenant',
      tenantId,
      'member',
      currentValue
    ).subscribe();
  }

  protected toggleEntityPermission(
    entityId: string,
    relation: 'read' | 'write' | 'delete',
    currentValue: boolean
  ): void {
    const user = this.selectedUser();
    if (!user) return;

    this.permissionManager.togglePermission(
      user.username,
      'entity',
      entityId,
      relation,
      currentValue
    ).subscribe();
  }

  protected toggleProductPermission(
    productId: string,
    relation: 'read' | 'write' | 'delete',
    currentValue: boolean
  ): void {
    const user = this.selectedUser();
    if (!user) return;

    this.permissionManager.togglePermission(
      user.username,
      'product',
      productId,
      relation,
      currentValue
    ).subscribe();
  }

  protected addProduct(): void {
    const user = this.selectedUser();
    const productId = this.productIdControl.value;
    if (!user || !productId) return;

    // Ajouter une permission read par défaut
    this.permissionManager.togglePermission(
      user.username,
      'product',
      productId,
      'read',
      false
    ).subscribe({
      next: () => {
        this.productIdControl.setValue('');
      }
    });
  }

  protected removeProduct(productId: string): void {
    const user = this.selectedUser();
    if (!user) return;

    const enriched = this.enrichedPermissions();
    if (!enriched) return;

    const productResources = enriched.resourcesByType?.['product'] || [];
    const product = productResources.find(p => p.resourceId === productId);

    if (!product) {
      this.showNotification('Produit non trouvé');
      return;
    }

    const directRelations: string[] = [];
    for (const [relation, status] of Object.entries(product.permissions)) {
      if (status.isDirect) {
        directRelations.push(relation);
      }
    }

    if (directRelations.length === 0) {
      this.showNotification('Aucune permission directe à supprimer');
      return;
    }

    const deletions = directRelations.map(relation =>
      this.permissionService.removePermission({
        username: user.username,
        objectType: 'product',
        objectId: productId,
        relation: relation
      })
    );

    forkJoin(deletions).subscribe({
      next: () => {
        this.permissionManager.loadPermissions(user.username).subscribe({
          next: () => {
            this.showNotification('Produit supprimé');
          }
        });
      },
      error: () => {
        this.showNotification('Erreur lors de la suppression');
      }
    });
  }

  private showNotification(message: string): void {
    this.snackBar.open(message, 'Fermer', { duration: 3000 });
  }

  protected checkProductPermission(): void {
    const user = this.selectedUser();
    const productId = this.checkProductIdControl.value;
    const relation = this.checkRelationControl.value;

    if (!user || !productId || !relation) {
      this.showNotification('Veuillez sélectionner un utilisateur, saisir un ID produit et choisir une relation');
      return;
    }

    this.checkLoading.set(true);
    this.checkResult.set(null);

    this.permissionService.checkResourcePermission(user.username, 'product', productId, relation).subscribe({
      next: (response) => {
        this.checkResult.set(response);
        this.checkLoading.set(false);
      },
      error: () => {
        this.showNotification('Erreur lors de la vérification');
        this.checkLoading.set(false);
      }
    });
  }

  protected goBack(): void {
    this.router.navigate(['/search']);
  }
}
