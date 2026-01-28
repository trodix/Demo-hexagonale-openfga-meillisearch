package com.trodix.demo.adapter.in.dto;

/**
 * Generic DTO representing any resource with an ID and optional name.
 * Used as a common interface for tenants, entities, products, etc.
 */
public record ResourceInfo(String id, String name) {

    /**
     * Factory method to convert from TenantInfo
     */
    public static ResourceInfo fromTenant(TenantInfo tenant) {
        return new ResourceInfo(tenant.id(), tenant.name());
    }

    /**
     * Factory method to convert from EntityInfo
     */
    public static ResourceInfo fromEntity(EntityInfo entity) {
        return new ResourceInfo(entity.getId(), entity.getName());
    }

    /**
     * Factory method for products (which don't have names)
     */
    public static ResourceInfo fromProductId(String productId) {
        return new ResourceInfo(productId, null);
    }
}
