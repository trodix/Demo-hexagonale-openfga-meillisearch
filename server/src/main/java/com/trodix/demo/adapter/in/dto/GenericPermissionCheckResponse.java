package com.trodix.demo.adapter.in.dto;

/**
 * Generic response for checking a permission on any resource type.
 * Replaces resource-specific check responses like ProductPermissionCheckResponse.
 */
public record GenericPermissionCheckResponse(
    String username,
    String resourceType,      // "tenant", "entity", "product", etc.
    String resourceId,
    String relation,          // "read", "write", "member", "admin", etc.
    boolean allowed
) {}
