package com.trodix.demo.application.port.security;

public interface AuthenticationAdapter {

    String getUsername();
    boolean isSystemUser();

}
