package com.trodix.demo.application.usecase;

import com.trodix.demo.application.model.TenantInfo;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientReadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListAdminTenantsUseCase {
    private final OpenFgaClient fgaClient;

    public List<TenantInfo> listAdminTenants(String username) {
        List<TenantInfo> tenants = new ArrayList<>();

        try {
            ClientReadRequest readRequest = new ClientReadRequest();
            var tuples = fgaClient.read(readRequest).get().getTuples();

            String user = "user:" + username;

            for (var tuple : tuples) {
                if (!user.equals(tuple.getKey().getUser())) {
                    continue;
                }

                String object = tuple.getKey().getObject();
                String relation = tuple.getKey().getRelation();

                if (object.startsWith("tenant:") && relation.equals("admin")) {
                    String tenantId = object.substring(7); // Retirer "tenant:"
                    tenants.add(new TenantInfo(tenantId, tenantId)); // name = id pour l'instant
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading admin tenants", e);
        }

        return tenants;
    }
}
