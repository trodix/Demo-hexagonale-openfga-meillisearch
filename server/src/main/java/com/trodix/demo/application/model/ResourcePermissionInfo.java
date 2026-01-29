package com.trodix.demo.application.model;

import java.util.Map;

/**
 * Modèle de la couche application pour représenter les permissions sur une ressource
 */
public record ResourcePermissionInfo(
    String resourceType,
    String resourceId,
    String resourceName,
    Map<String, PermissionStatusInfo> permissions
) {
}
