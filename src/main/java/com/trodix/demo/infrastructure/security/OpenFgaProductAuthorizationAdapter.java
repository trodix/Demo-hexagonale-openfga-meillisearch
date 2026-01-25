package com.trodix.demo.infrastructure.security;

import com.trodix.demo.application.exceptions.AuthorizationException;
import com.trodix.demo.application.port.ProductAuthorizationPort;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OpenFgaProductAuthorizationAdapter implements ProductAuthorizationPort {

    private final OpenFgaClient fgaClient;

    @Override
    public boolean hasGlobalReadAccess(String username, String tenantId) {
        try {
            String user = "user:" + username;
            String entityObject = "entity:" + tenantId + "/product";
            ClientCheckRequest checkRequest = new ClientCheckRequest()
                    .user(user)
                    .relation("read")
                    ._object(entityObject);
            return Boolean.TRUE.equals(fgaClient.check(checkRequest).get().getAllowed());
        } catch (Exception e) {
            throw new AuthorizationException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> getReadableProductIds(String username, String tenantId) {
        try {
            String user = "user:" + username;
            ClientListObjectsRequest listObjectsRequest = new ClientListObjectsRequest()
                    .user(user)
                    .relation("read")
                    .type("product");

            return fgaClient.listObjects(listObjectsRequest)
                    .get()
                    .getObjects()
                    .stream()
                    .map(obj -> obj.replace("product:", ""))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            throw new AuthorizationException(e.getMessage(), e);
        }
    }

    @Override
    public void onProductCreated(String username, String tenantId, String productId) {
        try {
            String productObject = "product:" + productId;
            String entityObject = "entity:" + tenantId + "/product";
            String user = "user:" + username;
            fgaClient
                    .writeTuples(
                            List.of(
                                    new ClientTupleKey()
                                            .user(entityObject)
                                            .relation("parent")
                                            ._object(productObject),
                                    new ClientTupleKey()
                                            .user(user)
                                            .relation("owner")
                                            ._object(productObject)
                            )
                    )
                    .get();
        } catch (Exception e) {
            throw new AuthorizationException(e.getMessage(), e);
        }
    }

    @Override
    public void onProductDeleted(String productId) {
        try {
            String objectId = "product:" + productId;

            var readRequest = new ClientReadRequest()._object(objectId);
            var existingTuples = fgaClient.read(readRequest).get().getTuples();

            if (!existingTuples.isEmpty()) {
                List<ClientTupleKeyWithoutCondition> tuplesToDelete = existingTuples.stream()
                        .map(tuple -> new ClientTupleKeyWithoutCondition()
                                .user(tuple.getKey().getUser())
                                .relation(tuple.getKey().getRelation())
                                ._object(tuple.getKey().getObject()))
                        .toList();

                fgaClient.deleteTuples(tuplesToDelete).get();
            }
        } catch (Exception e) {
            throw new AuthorizationException(e.getMessage(), e);
        }
    }

}
