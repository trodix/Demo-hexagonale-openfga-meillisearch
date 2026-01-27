package com.trodix.demo.adapter.in;

import com.trodix.demo.adapter.in.dto.*;
import com.trodix.demo.adapter.in.security.SpringAuthenticationAdapter;
import com.trodix.demo.application.usecase.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PermissionsRestAdapter {

    private final CheckPermissionsUseCase checkPermissionsUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final GetUserPermissionsUseCase getUserPermissionsUseCase;
    private final AddPermissionUseCase addPermissionUseCase;
    private final RemovePermissionUseCase removePermissionUseCase;
    private final ListEntitiesUseCase listEntitiesUseCase;
    private final SpringAuthenticationAdapter auth;

    @PostMapping(value = "/api/permissions/check", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public PermissionCheckResponse checkPermissions(@RequestBody PermissionCheckRequest request) {
        return checkPermissionsUseCase.checkPermissions(request);
    }

    @GetMapping(value = "/api/admin/users", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@fga.check('tenant', @springAuthenticationAdapter.getTenant(), 'admin', 'user')")
    public UserListResponse listUsers() {
        return listUsersUseCase.listUsers();
    }

    @GetMapping(value = "/api/admin/users/{username}/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@fga.check('tenant', @springAuthenticationAdapter.getTenant(), 'admin', 'user')")
    public UserPermissionsResponse getUserPermissions(@PathVariable String username) {
        String tenantId = auth.getTenant();
        return getUserPermissionsUseCase.getUserPermissions(username, tenantId);
    }

    @PostMapping(value = "/api/admin/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@fga.check('tenant', @springAuthenticationAdapter.getTenant(), 'admin', 'user')")
    public void addPermission(@RequestBody AddPermissionRequest request) {
        addPermissionUseCase.addPermission(request);
    }

    @DeleteMapping(value = "/api/admin/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@fga.check('tenant', @springAuthenticationAdapter.getTenant(), 'admin', 'user')")
    public void removePermission(@RequestBody AddPermissionRequest request) {
        removePermissionUseCase.removePermission(request);
    }

    @GetMapping(value = "/api/admin/entities", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@fga.check('tenant', @springAuthenticationAdapter.getTenant(), 'admin', 'user')")
    public EntityListResponse listEntities() {
        return listEntitiesUseCase.listEntities();
    }
}
