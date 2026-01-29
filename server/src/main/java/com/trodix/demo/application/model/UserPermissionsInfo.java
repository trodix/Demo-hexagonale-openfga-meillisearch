package com.trodix.demo.application.model;

import java.util.List;

/**
 * Modèle de la couche application pour les permissions d'un utilisateur
 */
public record UserPermissionsInfo(
    String username,
    String tenantId,
    List<PermissionTupleInfo> permissions
) {
}
