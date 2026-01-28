package com.trodix.demo.adapter.in.dto;

public record TenantPermissionStatus(
    String tenantId,
    String tenantName,
    boolean isMember,
    boolean isAdmin
) {}
