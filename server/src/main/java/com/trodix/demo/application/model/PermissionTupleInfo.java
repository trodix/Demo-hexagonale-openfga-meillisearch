package com.trodix.demo.application.model;

/**
 * Modèle de la couche application pour un tuple de permission
 */
public record PermissionTupleInfo(
    String objectType,
    String objectId,
    String relation
) {
}
