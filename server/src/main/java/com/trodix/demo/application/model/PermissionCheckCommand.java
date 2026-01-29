package com.trodix.demo.application.model;

import java.util.List;

/**
 * Modèle Application pour une demande de vérification de permissions multiples
 */
public record PermissionCheckCommand(
    List<PermissionCheckItem> checks
) {}
