package com.trodix.demo.adapter.in.dto;

import java.util.List;
import java.util.Map;

/**
 * Enhanced version using generic ResourcePermissionStatus.
 * The resourcesByType map groups all resources by their type (tenant, entity, product, etc.).
 * This makes the response extensible - adding new resource types doesn't require schema changes.
 */
public record EnrichedPermissionsResponse(
    String username,
    String tenantId,
    Map<String, List<ResourcePermissionStatus>> resourcesByType
) {
    // Convenience methods for backward compatibility
    public List<ResourcePermissionStatus> getTenants() {
        return resourcesByType.getOrDefault("tenant", List.of());
    }

    public List<ResourcePermissionStatus> getEntities() {
        return resourcesByType.getOrDefault("entity", List.of());
    }

    public List<ResourcePermissionStatus> getProducts() {
        return resourcesByType.getOrDefault("product", List.of());
    }
}
