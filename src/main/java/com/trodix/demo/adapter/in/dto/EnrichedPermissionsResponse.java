package com.trodix.demo.adapter.in.dto;

import java.util.List;

public record EnrichedPermissionsResponse(
    String username,
    String tenantId,
    List<EntityPermissionStatus> entities,
    List<ProductPermissionStatus> products
) {}
