package com.trodix.demo.application.usecase;

import com.trodix.demo.adapter.in.dto.AddPermissionRequest;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddPermissionUseCase {

    private final OpenFgaClient fgaClient;

    public void addPermission(AddPermissionRequest request) {
        // Validation: interdire l'ajout de la relation "admin" sur tenant
        if ("tenant".equals(request.getObjectType()) && "admin".equals(request.getRelation())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Cannot grant tenant admin permission - only super admin can do this"
            );
        }

        try {
            String user = "user:" + request.getUsername();
            String object = request.getObjectType() + ":" + request.getObjectId();

            ClientTupleKey tuple = new ClientTupleKey()
                    .user(user)
                    .relation(request.getRelation())
                    ._object(object);

            fgaClient.writeTuples(List.of(tuple)).get();
        } catch (Exception e) {
            throw new RuntimeException("Error adding permission", e);
        }
    }
}
