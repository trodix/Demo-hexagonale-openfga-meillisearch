package com.trodix.demo.application.usecase;

import com.trodix.demo.adapter.in.dto.AddPermissionRequest;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKeyWithoutCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RemovePermissionUseCase {

    private final OpenFgaClient fgaClient;

    public void removePermission(AddPermissionRequest request) {
        // Validation: interdire la suppression de la relation "admin" sur tenant
        if ("tenant".equals(request.getObjectType()) && "admin".equals(request.getRelation())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Cannot remove tenant admin permission - only super admin can do this"
            );
        }

        try {
            String user = "user:" + request.getUsername();
            String object = request.getObjectType() + ":" + request.getObjectId();

            ClientTupleKeyWithoutCondition tuple = new ClientTupleKeyWithoutCondition()
                    .user(user)
                    .relation(request.getRelation())
                    ._object(object);

            fgaClient.deleteTuples(List.of(tuple)).get();
        } catch (Exception e) {
            throw new RuntimeException("Error removing permission", e);
        }
    }
}
