package com.trodix.demo.adapter.in;

import com.trodix.demo.adapter.in.dto.*;
import com.trodix.demo.adapter.in.mapper.PermissionMapper;
import com.trodix.demo.adapter.in.security.SpringAuthenticationAdapter;
import com.trodix.demo.application.usecase.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PermissionsRestAdapter {

    private final CheckPermissionsUseCase checkPermissionsUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final GetUserPermissionsUseCase getUserPermissionsUseCase;
    private final AddPermissionUseCase addPermissionUseCase;
    private final RemovePermissionUseCase removePermissionUseCase;
    private final ListEntitiesUseCase listEntitiesUseCase;
    private final ListAdminTenantsUseCase listAdminTenantsUseCase;
    private final GetEnrichedPermissionsUseCase getEnrichedPermissionsUseCase;
    private final CheckProductPermissionUseCase checkProductPermissionUseCase;
    private final SpringAuthenticationAdapter auth;
    private final PermissionMapper mapper;

    @PostMapping(value = "/api/permissions/check", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public PermissionCheckResponse checkPermissions(@RequestBody PermissionCheckRequest request) {
        return mapper.toResponse(checkPermissionsUseCase.checkPermissions(mapper.toCommand(request)));
    }

    @GetMapping(value = "/api/admin/users", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@fga.check('tenant', @springAuthenticationAdapter.getTenant(), 'admin', 'user')")
    public UserListResponse listUsers() {
        return mapper.toUserListResponse(listUsersUseCase.listUsers());
    }

    @GetMapping(value = "/api/admin/users/{username}/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@fga.check('tenant', @springAuthenticationAdapter.getTenant(), 'admin', 'user')")
    public UserPermissionsResponse getUserPermissions(
        @PathVariable String username,
        @RequestParam(required = false) String tenantId
    ) {
        // Si tenantId non fourni, utiliser celui de l'utilisateur connecté
        String targetTenant = tenantId != null ? tenantId : auth.getTenant();

        // Vérifier que l'utilisateur connecté est admin du tenant ciblé
        String connectedUser = auth.getUsername();
        PermissionCheckRequest checkRequest = new PermissionCheckRequest(List.of(
            new CheckItem("tenant:" + targetTenant, "admin")
        ));
        boolean isAdmin = mapper.toResponse(
            checkPermissionsUseCase.checkPermissions(mapper.toCommand(checkRequest))
        ).getResults().get("tenant:" + targetTenant + " admin");

        if (!isAdmin) {
            throw new com.trodix.demo.application.exceptions.AuthorizationException("Not admin of this tenant");
        }

        return mapper.toResponse(getUserPermissionsUseCase.getUserPermissions(username, targetTenant));
    }

    @PostMapping(value = "/api/admin/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@fga.check('tenant', @springAuthenticationAdapter.getTenant(), 'admin', 'user')")
    public void addPermission(@RequestBody AddPermissionRequest request) {
        addPermissionUseCase.addPermission(mapper.toCommand(request));
    }

    @DeleteMapping(value = "/api/admin/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@fga.check('tenant', @springAuthenticationAdapter.getTenant(), 'admin', 'user')")
    public void removePermission(@RequestBody AddPermissionRequest request) {
        removePermissionUseCase.removePermission(mapper.toCommand(request));
    }

    @GetMapping(value = "/api/admin/entities", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@fga.check('tenant', @springAuthenticationAdapter.getTenant(), 'admin', 'user')")
    public EntityListResponse listEntities() {
        return mapper.toEntityListResponse(listEntitiesUseCase.listEntities());
    }

    @GetMapping(value = "/api/admin/tenants", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public TenantListResponse listAdminTenants() {
        String username = auth.getUsername();
        return mapper.toTenantListResponse(listAdminTenantsUseCase.listAdminTenants(username));
    }

    @GetMapping(value = "/api/admin/users/{username}/permissions/enriched", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@fga.check('tenant', @springAuthenticationAdapter.getTenant(), 'admin', 'user')")
    public EnrichedPermissionsResponse getEnrichedPermissions(@PathVariable String username) {
        String tenantId = auth.getTenant();
        return mapper.toResponse(getEnrichedPermissionsUseCase.getEnrichedPermissions(username, tenantId));
    }

    /**
     * Generic endpoint for checking a permission on any resource type.
     * Replaces specific endpoints like /users/{username}/products/{productId}/check
     *
     * Example usage:
     * - GET /api/admin/users/john/resources/product/123/check?relation=read
     * - GET /api/admin/users/john/resources/entity/e1/check?relation=write
     * - GET /api/admin/users/john/resources/tenant/t1/check?relation=admin
     */
    @GetMapping(value = "/api/admin/users/{username}/resources/{resourceType}/{resourceId}/check", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@fga.check('tenant', @springAuthenticationAdapter.getTenant(), 'admin', 'user')")
    public GenericPermissionCheckResponse checkResourcePermission(
        @PathVariable String username,
        @PathVariable String resourceType,
        @PathVariable String resourceId,
        @RequestParam String relation
    ) {
        boolean allowed = checkProductPermissionUseCase.checkProductPermission(username, resourceId, relation);
        return new GenericPermissionCheckResponse(username, resourceType, resourceId, relation, allowed);
    }

}
