package com.trodix.demo.adapter.in.security;

import com.trodix.demo.application.port.security.AuthenticationAdapter;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientListObjectsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SpringAuthenticationAdapter implements AuthenticationAdapter {

    public static final String SYSTEM_USER = "system";

    private final OpenFgaClient fgaClient;

    public Optional<Authentication> getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            return Optional.of(authentication);
        }
        return Optional.empty();
    }

    public String getUsername() {
        return getAuthentication()
                .map(Authentication::getPrincipal)
                .filter(UserDetails.class::isInstance)
                .map(UserDetails.class::cast)
                .map(UserDetails::getUsername)
                .orElse(SYSTEM_USER);
    }

    public boolean isSystemUser() {
        return SYSTEM_USER.equals(getUsername());
    }

    public List<String> getAssociatedTenants() {
        try {
            List<String> memberAssoc = fgaClient.listObjects(
                        new ClientListObjectsRequest()
                            .user("user:" + getUsername())
                            .relation("member")
                            .type("tenant")
                    )
                    .get()
                    .getObjects();

            List<String> adminAssoc = fgaClient.listObjects(
                            new ClientListObjectsRequest()
                                    .user("user:" + getUsername())
                                    .relation("admin")
                                    .type("tenant")
                    )
                    .get()
                    .getObjects();

            return Stream.of(memberAssoc, adminAssoc)
                    .flatMap(List::stream)
                    .map(obj -> obj.replace("tenant:", ""))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isTenantMember(String tenantId) {
        return getAssociatedTenants().contains(tenantId);
    }

    /**
     * Store the current tenant id in the Authentication details so it is available
     * through the SecurityContext for the duration of the request.
     */
    public void setCurrentTenant(String tenantId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return;

        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                auth.getPrincipal(), auth.getCredentials(), auth.getAuthorities()
        );
        newAuth.setDetails(tenantId);
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    /**
     * Read the current tenant id from Authentication details.
     */
    public String getTenant() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof String) {
            return (String) auth.getDetails();
        }
        return null;
    }

}
