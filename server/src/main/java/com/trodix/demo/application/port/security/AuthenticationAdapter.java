package com.trodix.demo.application.port.security;

/**
 * Port pour l'authentification (couche application)
 * Implémenté par SpringAuthenticationAdapter dans la couche adapter
 */
public interface AuthenticationAdapter {

    /**
     * Retourne le nom d'utilisateur courant
     */
    String getUsername();

    /**
     * Vérifie si l'utilisateur courant est l'utilisateur système
     */
    boolean isSystemUser();

    /**
     * Retourne l'ID du tenant courant
     */
    String getTenant();

}
