package com.trodix.demo.application.service;

import com.trodix.demo.application.model.PermissionStatusInfo;
import com.trodix.demo.application.model.ResourceInfo;
import com.trodix.demo.application.model.ResourcePermissionInfo;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import dev.openfga.sdk.api.client.model.ClientReadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ResourcePermissionChecker {
    private final OpenFgaClient fgaClient;

    /**
     * Checks multiple relations for a single resource.
     *
     * @param username The user to check permissions for
     * @param resourceType Type of resource ("tenant", "entity", "product")
     * @param resourceId ID of the resource
     * @param relations List of relations to check (e.g., ["read", "write", "delete"])
     * @param directPermissions Set of direct permissions (format: "resourceType:resourceId#relation")
     * @return Map with the status of each relation
     */
    public Map<String, PermissionStatusInfo> checkResourcePermissions(
        String username,
        String resourceType,
        String resourceId,
        List<String> relations,
        Set<String> directPermissions
    ) {
        String user = "user:" + username;
        String object = resourceType + ":" + resourceId;

        Map<String, PermissionStatusInfo> result = new HashMap<>();

        for (String relation : relations) {
            try {
                // Check if the permission exists (direct or indirect)
                ClientCheckRequest checkRequest = new ClientCheckRequest()
                    .user(user)
                    .relation(relation)
                    ._object(object);

                boolean hasPermission = fgaClient.check(checkRequest).get().getAllowed();

                if (!hasPermission) {
                    result.put(relation, new PermissionStatusInfo(false, false));
                } else {
                    // Check if it's a direct permission
                    String permissionKey = object + "#" + relation;
                    boolean isDirect = directPermissions.contains(permissionKey);
                    result.put(relation, new PermissionStatusInfo(true, isDirect));
                }
            } catch (Exception e) {
                throw new RuntimeException("Error checking permission " + relation + " on " + object, e);
            }
        }

        return result;
    }

    /**
     * Checks permissions for multiple resources of the same type.
     *
     * @param username The user to check permissions for
     * @param resourceType Type of resource
     * @param resources List of ResourceInfo (id + name)
     * @param relations Relations to check for each resource
     * @param directPermissions Set of direct permissions
     * @return List of ResourcePermissionStatus
     */
    public List<ResourcePermissionInfo> checkMultipleResources(
        String username,
        String resourceType,
        List<ResourceInfo> resources,
        List<String> relations,
        Set<String> directPermissions
    ) {
        return resources.stream()
            .map(resource -> {
                Map<String, PermissionStatusInfo> permissions = checkResourcePermissions(
                    username,
                    resourceType,
                    resource.id(),
                    relations,
                    directPermissions
                );

                return new ResourcePermissionInfo(
                    resourceType,
                    resource.id(),
                    resource.name(),
                    permissions
                );
            })
            .toList();
    }

    /**
     * Retrieves all direct permissions for a user.
     *
     * @param username The user to get permissions for
     * @return Set of permission keys (format: "resourceType:resourceId#relation")
     */
    public Set<String> getDirectPermissions(String username) {
        try {
            Set<String> directPermissions = new HashSet<>();
            ClientReadRequest readRequest = new ClientReadRequest();
            var tuples = fgaClient.read(readRequest).get().getTuples();

            String user = "user:" + username;
            for (var tuple : tuples) {
                if (user.equals(tuple.getKey().getUser())) {
                    String key = tuple.getKey().getObject() + "#" + tuple.getKey().getRelation();
                    directPermissions.add(key);
                }
            }

            return directPermissions;
        } catch (Exception e) {
            throw new RuntimeException("Error reading direct permissions for user: " + username, e);
        }
    }
}
