package com.trodix.demo.application.usecase;

import com.trodix.demo.adapter.in.dto.AddPermissionRequest;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.errors.FgaApiValidationError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddPermissionUseCase {

    private final OpenFgaClient fgaClient;

    public void addPermission(AddPermissionRequest request) {
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
            if (e.getCause() instanceof FgaApiValidationError) {
                FgaApiValidationError fgaError = (FgaApiValidationError) e.getCause();
                if (fgaError.getMessage() != null && fgaError.getMessage().contains("already exists")) {
                    log.debug("Tuple already exists, ignoring: user={}, object={}, relation={}",
                        request.getUsername(), request.getObjectId(), request.getRelation());
                    return;
                }
            }
            throw new RuntimeException("Error adding permission", e);
        }
    }
}
