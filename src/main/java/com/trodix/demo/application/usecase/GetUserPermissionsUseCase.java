package com.trodix.demo.application.usecase;

import com.trodix.demo.adapter.in.dto.PermissionTuple;
import com.trodix.demo.adapter.in.dto.UserPermissionsResponse;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientReadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GetUserPermissionsUseCase {

    private final OpenFgaClient fgaClient;

    // Relations configurables par type d'objet
    private static final Set<String> TENANT_RELATIONS = Set.of("member");
    private static final Set<String> ENTITY_RELATIONS = Set.of("read", "write", "delete");
    private static final Set<String> PRODUCT_RELATIONS = Set.of("owner", "read", "write", "delete");

    public UserPermissionsResponse getUserPermissions(String username, String tenantId) {
        List<PermissionTuple> permissions = new ArrayList<>();

        try {
            String user = "user:" + username;

            // L'API OpenFGA ne permet pas de filtrer par user dans read()
            // Nous devons lire tous les tuples et filtrer côté application
            ClientReadRequest readRequest = new ClientReadRequest();
            var tuples = fgaClient.read(readRequest).get().getTuples();

            for (var tuple : tuples) {
                // Filtrer uniquement les tuples de cet utilisateur
                if (!user.equals(tuple.getKey().getUser())) {
                    continue;
                }

                String object = tuple.getKey().getObject();
                String relation = tuple.getKey().getRelation();

                // Parse object type and id
                String[] parts = object.split(":", 2);
                if (parts.length != 2) continue;

                String objectType = parts[0];
                String objectId = parts[1];

                // Filter only configurable relations
                boolean isConfigurable = false;
                switch (objectType) {
                    case "tenant":
                        isConfigurable = TENANT_RELATIONS.contains(relation);
                        break;
                    case "entity":
                        isConfigurable = ENTITY_RELATIONS.contains(relation);
                        break;
                    case "product":
                        isConfigurable = PRODUCT_RELATIONS.contains(relation);
                        break;
                }

                if (isConfigurable) {
                    permissions.add(new PermissionTuple(objectType, objectId, relation));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading user permissions", e);
        }

        return new UserPermissionsResponse(username, tenantId, permissions);
    }
}
