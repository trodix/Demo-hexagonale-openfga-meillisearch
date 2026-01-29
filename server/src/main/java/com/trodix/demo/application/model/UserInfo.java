package com.trodix.demo.application.model;

/**
 * Modèle de la couche application pour les informations d'un utilisateur
 */
public record UserInfo(
    String username,
    String displayName
) {
}
