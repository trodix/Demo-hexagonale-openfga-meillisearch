package com.trodix.demo.application.model;

import java.util.List;
import java.util.Map;

/**
 * Modèle de la couche application pour les permissions enrichies d'un utilisateur
 */
public record EnrichedPermissionsInfo(
    String username,
    String tenantId,
    Map<String, List<ResourcePermissionInfo>> resourcesByType
) {
}
