package com.trodix.demo.application.usecase;

import com.trodix.demo.application.model.*;
import com.trodix.demo.application.service.ResourcePermissionChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GetEnrichedPermissionsUseCase {
    private final ResourcePermissionChecker resourcePermissionChecker;
    private final ListEntitiesUseCase listEntitiesUseCase;
    private final ListAdminTenantsUseCase listAdminTenantsUseCase;

    public EnrichedPermissionsInfo getEnrichedPermissions(String username, String tenantId) {
        Set<String> directPermissions = resourcePermissionChecker.getDirectPermissions(username);
        Map<String, List<ResourcePermissionInfo>> resourcesByType = new HashMap<>();

        Set<String> tenantIds = new HashSet<>();
        for (String permission : directPermissions) {
            if (permission.startsWith("tenant:")) {
                String tenantIdFromPerm = permission.substring(7, permission.indexOf('#'));
                tenantIds.add(tenantIdFromPerm);
            }
        }

        var tenants = tenantIds.stream()
            .map(id -> new TenantInfo(id, id))
            .toList();

        var tenantStatuses = tenants.stream()
            .map(tenant -> {
                String tenantObject = "tenant:" + tenant.id();
                Map<String, PermissionStatusInfo> permissions = new HashMap<>();

                String memberKey = tenantObject + "#member";
                String adminKey = tenantObject + "#admin";

                boolean isMember = directPermissions.contains(memberKey);
                boolean isAdmin = directPermissions.contains(adminKey);

                permissions.put("member", new PermissionStatusInfo(isMember, isMember));
                permissions.put("admin", new PermissionStatusInfo(isAdmin, isAdmin));

                return new ResourcePermissionInfo(
                    "tenant",
                    tenant.id(),
                    tenant.name(),
                    permissions
                );
            })
            .toList();
        resourcesByType.put("tenant", tenantStatuses);

        var entities = listEntitiesUseCase.listEntities();
        var entityResources = entities.stream()
            .map(ResourceInfo::fromEntity)
            .toList();
        resourcesByType.put("entity", resourcePermissionChecker.checkMultipleResources(
            username,
            "entity",
            entityResources,
            List.of("read", "write", "delete"),
            directPermissions
        ));

        var productIds = extractProductIdsFromDirectPermissions(directPermissions);
        var productResources = productIds.stream()
            .map(ResourceInfo::fromProductId)
            .toList();
        resourcesByType.put("product", resourcePermissionChecker.checkMultipleResources(
            username,
            "product",
            productResources,
            List.of("read", "write", "delete"),
            directPermissions
        ));

        return new EnrichedPermissionsInfo(username, tenantId, resourcesByType);
    }

    private List<String> extractProductIdsFromDirectPermissions(Set<String> directPermissions) {
        return directPermissions.stream()
            .filter(perm -> perm.startsWith("product:"))
            .map(perm -> perm.substring(8, perm.indexOf('#')))
            .distinct()
            .toList();
    }
}
