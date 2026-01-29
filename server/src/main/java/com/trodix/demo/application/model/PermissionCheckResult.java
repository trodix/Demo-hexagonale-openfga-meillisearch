package com.trodix.demo.application.model;

import java.util.Map;

/**
 * Modèle Application pour le résultat d'une vérification de permissions
 */
public record PermissionCheckResult(
    Map<String, Boolean> results
) {}
