package com.trodix.demo.application.model;

/**
 * Item de vérification de permission (couche application)
 */
public record CheckItemCommand(
    String object,
    String relation
) {
}
