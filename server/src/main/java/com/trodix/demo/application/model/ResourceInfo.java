package com.trodix.demo.application.model;

/**
 * Modèle de la couche application pour une ressource (tenant, entity, product)
 */
public record ResourceInfo(
    String id,
    String name
) {
    /**
     * Factory method pour créer depuis un EntityInfo
     */
    public static ResourceInfo fromEntity(EntityInfo entity) {
        return new ResourceInfo(entity.id(), entity.name());
    }

    /**
     * Factory method pour créer depuis un ID de produit
     */
    public static ResourceInfo fromProductId(String productId) {
        return new ResourceInfo(productId, null);
    }

    /**
     * Factory method pour créer depuis un TenantInfo
     */
    public static ResourceInfo fromTenant(TenantInfo tenant) {
        return new ResourceInfo(tenant.id(), tenant.name());
    }
}
