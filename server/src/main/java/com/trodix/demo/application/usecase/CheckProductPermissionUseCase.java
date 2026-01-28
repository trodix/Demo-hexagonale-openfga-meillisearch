package com.trodix.demo.application.usecase;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckProductPermissionUseCase {

    private final OpenFgaClient fgaClient;

    public boolean checkProductPermission(String username, String productId, String relation) {
        try {
            String user = "user:" + username;
            String object = "product:" + productId;

            ClientCheckRequest checkRequest = new ClientCheckRequest()
                .user(user)
                .relation(relation)
                ._object(object);

            return fgaClient.check(checkRequest).get().getAllowed();
        } catch (Exception e) {
            throw new RuntimeException("Error checking product permission", e);
        }
    }
}
