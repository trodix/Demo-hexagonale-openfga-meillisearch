package com.trodix.demo.adapter.in.dto;

import java.util.Map;

/**
 * Generic DTO for representing permissions on any type of resource.
 * Replaces the need for separate TenantPermissionStatus, EntityPermissionStatus, ProductPermissionStatus.
 *
 * Example usage:
 * - For a tenant: resourceType="tenant", permissions={"member": PermissionStatus, "admin": PermissionStatus}
 * - For an entity: resourceType="entity", permissions={"read": PermissionStatus, "write": PermissionStatus, ...}
 * - For a product: resourceType="product", permissions={"read": PermissionStatus, "write": PermissionStatus, ...}
 */
public record ResourcePermissionStatus(
    String resourceType,      // "tenant", "entity", "product", "workspace", etc.
    String resourceId,
    String resourceName,      // optional, may be null for products
    Map<String, PermissionStatus> permissions  // key = relation ("read", "write", "member"), value = status
) {}
