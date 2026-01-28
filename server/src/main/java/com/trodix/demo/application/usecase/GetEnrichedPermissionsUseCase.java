package com.trodix.demo.application.usecase;

import com.trodix.demo.adapter.in.dto.*;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import dev.openfga.sdk.api.client.model.ClientReadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class GetEnrichedPermissionsUseCase {
    private final OpenFgaClient fgaClient;
    private final ListEntitiesUseCase listEntitiesUseCase;
    private final ListAdminTenantsUseCase listAdminTenantsUseCase;

    public EnrichedPermissionsResponse getEnrichedPermissions(String username, String tenantId) {
        try {
            // 1. Récupérer tous les tuples directs de l'utilisateur
            Set<String> directPermissions = getDirectPermissions(username);

            // 2. Récupérer la liste des tenants où l'utilisateur est admin
            TenantListResponse tenantsResponse = listAdminTenantsUseCase.listAdminTenants(username);

            // 3. Vérifier les permissions pour chaque tenant
            List<TenantPermissionStatus> tenantStatuses = new ArrayList<>();
            for (TenantInfo tenant : tenantsResponse.tenants()) {
                tenantStatuses.add(checkTenantPermissions(username, tenant, directPermissions));
            }

            // 4. Récupérer la liste des entités disponibles
            EntityListResponse entitiesResponse = listEntitiesUseCase.listEntities();

            // 5. Vérifier les permissions pour chaque entité
            List<EntityPermissionStatus> entityStatuses = new ArrayList<>();
            for (EntityInfo entity : entitiesResponse.getEntities()) {
                entityStatuses.add(checkEntityPermissions(username, entity, directPermissions));
            }

            // 6. Récupérer tous les produits pour lesquels l'utilisateur a des permissions
            List<ProductPermissionStatus> productStatuses = checkProductPermissions(username, directPermissions);

            return new EnrichedPermissionsResponse(username, tenantId, tenantStatuses, entityStatuses, productStatuses);
        } catch (Exception e) {
            throw new RuntimeException("Error getting enriched permissions", e);
        }
    }

    private Set<String> getDirectPermissions(String username) throws Exception {
        Set<String> directPermissions = new HashSet<>();
        ClientReadRequest readRequest = new ClientReadRequest();
        var tuples = fgaClient.read(readRequest).get().getTuples();

        String user = "user:" + username;
        for (var tuple : tuples) {
            if (user.equals(tuple.getKey().getUser())) {
                String key = tuple.getKey().getObject() + "#" + tuple.getKey().getRelation();
                directPermissions.add(key);
            }
        }

        return directPermissions;
    }

    private TenantPermissionStatus checkTenantPermissions(
        String username,
        TenantInfo tenant,
        Set<String> directPermissions
    ) throws Exception {
        String tenantObject = "tenant:" + tenant.id();

        // Vérifier member (toujours direct selon le modèle FGA)
        boolean isMember = directPermissions.contains(tenantObject + "#member");

        // Vérifier admin (toujours direct selon le modèle FGA)
        boolean isAdmin = directPermissions.contains(tenantObject + "#admin");

        return new TenantPermissionStatus(
            tenant.id(),
            tenant.name(),
            isMember,
            isAdmin
        );
    }

    private EntityPermissionStatus checkEntityPermissions(
        String username,
        EntityInfo entity,
        Set<String> directPermissions
    ) throws Exception {
        String user = "user:" + username;
        String entityObject = "entity:" + entity.getId();

        // Vérifier read
        boolean hasRead = checkPermission(user, entityObject, "read");
        boolean readIsDirect = directPermissions.contains(entityObject + "#read");

        // Vérifier write
        boolean hasWrite = checkPermission(user, entityObject, "write");
        boolean writeIsDirect = directPermissions.contains(entityObject + "#write");

        // Vérifier delete
        boolean hasDelete = checkPermission(user, entityObject, "delete");
        boolean deleteIsDirect = directPermissions.contains(entityObject + "#delete");

        return new EntityPermissionStatus(
            entity.getId(),
            entity.getName(),
            new PermissionStatus(hasRead, readIsDirect),
            new PermissionStatus(hasWrite, writeIsDirect),
            new PermissionStatus(hasDelete, deleteIsDirect)
        );
    }

    private List<ProductPermissionStatus> checkProductPermissions(
        String username,
        Set<String> directPermissions
    ) throws Exception {
        List<ProductPermissionStatus> result = new ArrayList<>();
        String user = "user:" + username;

        // Trouver tous les produits pour lesquels l'utilisateur a des tuples directs
        Set<String> productIds = new HashSet<>();
        for (String permission : directPermissions) {
            if (permission.startsWith("product:")) {
                String productId = permission.substring(8, permission.indexOf('#'));
                productIds.add(productId);
            }
        }

        // Pour chaque produit, vérifier les permissions effectives
        for (String productId : productIds) {
            String productObject = "product:" + productId;

            boolean hasRead = checkPermission(user, productObject, "read");
            boolean readIsDirect = directPermissions.contains(productObject + "#read");

            boolean hasWrite = checkPermission(user, productObject, "write");
            boolean writeIsDirect = directPermissions.contains(productObject + "#write");

            boolean hasDelete = checkPermission(user, productObject, "delete");
            boolean deleteIsDirect = directPermissions.contains(productObject + "#delete");

            result.add(new ProductPermissionStatus(
                productId,
                new PermissionStatus(hasRead, readIsDirect),
                new PermissionStatus(hasWrite, writeIsDirect),
                new PermissionStatus(hasDelete, deleteIsDirect)
            ));
        }

        return result;
    }

    private boolean checkPermission(String user, String object, String relation)
        throws Exception {
        ClientCheckRequest checkRequest = new ClientCheckRequest()
            .user(user)
            .relation(relation)
            ._object(object);

        return fgaClient.check(checkRequest).get().getAllowed();
    }
}
