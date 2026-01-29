package com.trodix.demo.application.model;

/**
 * Modèle de la couche application pour un tenant
 */
public record TenantInfo(
    String id,
    String name
) {
}
