package com.trodix.demo.application.usecase;

import com.trodix.demo.adapter.in.dto.CheckItem;
import com.trodix.demo.adapter.in.dto.PermissionCheckRequest;
import com.trodix.demo.adapter.in.dto.PermissionCheckResponse;
import com.trodix.demo.adapter.in.security.SpringAuthenticationAdapter;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckPermissionsUseCase {

    private final OpenFgaClient fgaClient;
    private final SpringAuthenticationAdapter auth;

    public PermissionCheckResponse checkPermissions(PermissionCheckRequest request) {
        Map<String, Boolean> results = new HashMap<>();
        String username = auth.getUsername();

        for (CheckItem check : request.getChecks()) {
            String key = check.getObject() + " " + check.getRelation();
            try {
                ClientCheckRequest checkRequest = new ClientCheckRequest()
                        .user("user:" + username)
                        .relation(check.getRelation())
                        ._object(check.getObject());

                Boolean allowed = fgaClient.check(checkRequest).get().getAllowed();
                results.put(key, Boolean.TRUE.equals(allowed));
            } catch (Exception e) {
                results.put(key, false);
            }
        }

        return new PermissionCheckResponse(results);
    }
}
