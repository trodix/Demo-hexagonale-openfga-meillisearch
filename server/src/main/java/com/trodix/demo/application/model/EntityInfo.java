package com.trodix.demo.application.model;

import java.util.List;

/**
 * Modèle de la couche application pour une entité
 */
public record EntityInfo(
    String id,
    String name,
    List<String> availableRelations
) {
}
