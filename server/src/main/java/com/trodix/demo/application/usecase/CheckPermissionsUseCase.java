package com.trodix.demo.application.usecase;

import com.trodix.demo.application.model.PermissionCheckCommand;
import com.trodix.demo.application.model.PermissionCheckItem;
import com.trodix.demo.application.model.PermissionCheckResult;
import com.trodix.demo.application.port.security.AuthenticationAdapter;
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
    private final AuthenticationAdapter auth;

    public PermissionCheckResult checkPermissions(PermissionCheckCommand command) {
        Map<String, Boolean> results = new HashMap<>();
        String username = auth.getUsername();

        for (PermissionCheckItem check : command.checks()) {
            String key = check.object() + " " + check.relation();
            try {
                ClientCheckRequest checkRequest = new ClientCheckRequest()
                        .user("user:" + username)
                        .relation(check.relation())
                        ._object(check.object());

                Boolean allowed = fgaClient.check(checkRequest).get().getAllowed();
                results.put(key, Boolean.TRUE.equals(allowed));
            } catch (Exception e) {
                results.put(key, false);
            }
        }

        return new PermissionCheckResult(results);
    }
}
