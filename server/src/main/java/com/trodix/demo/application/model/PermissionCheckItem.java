package com.trodix.demo.application.model;

/**
 * Modèle Application pour un item de vérification de permission
 */
public record PermissionCheckItem(
    String object,
    String relation
) {}
