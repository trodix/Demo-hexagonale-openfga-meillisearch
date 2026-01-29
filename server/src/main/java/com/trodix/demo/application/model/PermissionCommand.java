package com.trodix.demo.application.model;

import lombok.Builder;

/**
 * Commande pour ajouter/supprimer une permission (couche application)
 * Indépendante des DTOs de la couche adapter
 */
@Builder
public record PermissionCommand(
    String username,
    String objectType,
    String objectId,
    String relation
) {
}
