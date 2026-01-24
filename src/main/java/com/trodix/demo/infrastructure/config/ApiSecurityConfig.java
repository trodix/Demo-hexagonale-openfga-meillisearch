package com.trodix.demo.infrastructure.config;

import dev.openfga.autoconfigure.OpenFgaProperties;
import dev.openfga.sdk.api.configuration.*;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class ApiSecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, OncePerRequestFilter tenantFilter) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .addFilterAfter(tenantFilter, BasicAuthenticationFilter.class)
                .build();
    }

    @Bean
    InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        List<UserDetails> users = List.of(
                new User("admin", "{noop}password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))),
                new User("user1", "{noop}password", List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        return new InMemoryUserDetailsManager(users);
    }

    /**
     * Fix par ce que l'autoconfiguration n'est pas compatible avec spring boot 4 pour le moment.
     * /!\ Ne supporte pas OpenTelemetry
     * @param openFgaProperties Les propriétés su starter Spring Boot OpenFga
     * @return La configuration
     */
    @Bean
    public ClientConfiguration fgaConfig(OpenFgaProperties openFgaProperties) {
        var clientConfiguration = new ClientConfiguration();
        var map = PropertyMapper.get();
        map.from(openFgaProperties::getCredentials)
                .whenHasText()
                .as(ApiSecurityConfig::toCredentials)
                .to(clientConfiguration::credentials);
        map.from(openFgaProperties::getApiUrl).whenHasText().to(clientConfiguration::apiUrl);
        map.from(openFgaProperties::getStoreId).whenHasText().to(clientConfiguration::storeId);
        map.from(openFgaProperties::getAuthorizationModelId)
                .whenHasText()
                .to(clientConfiguration::authorizationModelId);
        map.from(openFgaProperties::getUserAgent).whenHasText().to(clientConfiguration::userAgent);
        map.from(openFgaProperties::getReadTimeout).whenHasText().to(clientConfiguration::readTimeout);
        map.from(openFgaProperties::getConnectTimeout).whenHasText().to(clientConfiguration::connectTimeout);
        map.from(openFgaProperties::getMaxRetries).whenHasText().to(clientConfiguration::maxRetries);
        map.from(openFgaProperties::getMinimumRetryDelay).whenHasText().to(clientConfiguration::minimumRetryDelay);
        map.from(openFgaProperties::getDefaultHeaders).whenHasText().to(clientConfiguration::defaultHeaders);
//        map.from(openFgaProperties::getTelemetryConfiguration)
//                .whenHasText()
//                .as(ApiSecurityConfig::toTelemetryConfiguration)
//                .to(clientConfiguration::telemetryConfiguration);
        return clientConfiguration;
    }

    private static Credentials toCredentials(OpenFgaProperties.Credentials credentialsProperties) {
        var credentials = new Credentials();
        if (OpenFgaProperties.CredentialsMethod.API_TOKEN == credentialsProperties.getMethod()) {
            credentials.setCredentialsMethod(CredentialsMethod.API_TOKEN);
            credentials.setApiToken(
                    new ApiToken(credentialsProperties.getConfig().getApiToken()));
        } else if (OpenFgaProperties.CredentialsMethod.CLIENT_CREDENTIALS == credentialsProperties.getMethod()) {
            var clientCredentials = new ClientCredentials()
                    .clientId(credentialsProperties.getConfig().getClientId())
                    .clientSecret(credentialsProperties.getConfig().getClientSecret())
                    .apiTokenIssuer(credentialsProperties.getConfig().getApiTokenIssuer())
                    .apiAudience(credentialsProperties.getConfig().getApiAudience())
                    .scopes(credentialsProperties.getConfig().getScopes());
            credentials.setCredentialsMethod(CredentialsMethod.CLIENT_CREDENTIALS);
            credentials.setClientCredentials(clientCredentials);
        }
        return credentials;
    }

}
