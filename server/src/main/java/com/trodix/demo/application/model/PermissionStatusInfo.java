package com.trodix.demo.application.model;

/**
 * Modèle de la couche application pour le statut d'une permission
 */
public record PermissionStatusInfo(
    boolean hasPermission,
    boolean isDirect
) {
}
