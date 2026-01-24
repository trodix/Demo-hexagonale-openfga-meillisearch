package com.trodix.demo.infrastructure.filter;

import com.trodix.demo.application.AuthenticationService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.Authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "X-Tenant-Id";

    private final AuthenticationService authenticationService;

    public TenantFilter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        // Apply only to API endpoints
        if (path.startsWith(request.getContextPath() + "/api")) {
            String tenantId = request.getHeader(TENANT_HEADER);
            if (tenantId == null || tenantId.isBlank()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                String msg = "{\"error\": \"Le header 'X-Tenant-Id' est requis pour l'authentification\"}";
                response.getWriter().write(msg);
                return;
            }

            // Ensure user is authenticated
            var optAuth = authenticationService.getAuthentication();
            if (optAuth.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                String msg = "{\"error\": \"Utilisateur non authentifié\"}";
                response.getWriter().write(msg);
                return;
            }

            // Verify membership to tenant and store tenant in security context
            if (!authenticationService.isTenantMember(tenantId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                String msg = "{\"error\": \"L'utilisateur n'est pas membre du tenant fourni\"}";
                response.getWriter().write(msg);
                return;
            }

            authenticationService.setCurrentTenant(tenantId);
        }

        filterChain.doFilter(request, response);
    }
}
