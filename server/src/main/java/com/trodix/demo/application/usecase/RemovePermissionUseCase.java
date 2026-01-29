package com.trodix.demo.application.usecase;

import com.trodix.demo.application.model.PermissionCommand;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKeyWithoutCondition;
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
public class RemovePermissionUseCase {

    private final OpenFgaClient fgaClient;

    public void removePermission(PermissionCommand command) {
        if ("tenant".equals(command.objectType()) && "admin".equals(command.relation())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Cannot remove tenant admin permission - only super admin can do this"
            );
        }

        try {
            String user = "user:" + command.username();
            String object = command.objectType() + ":" + command.objectId();

            ClientTupleKeyWithoutCondition tuple = new ClientTupleKeyWithoutCondition()
                    .user(user)
                    .relation(command.relation())
                    ._object(object);

            fgaClient.deleteTuples(List.of(tuple)).get();
        } catch (Exception e) {
            if (e.getCause() instanceof FgaApiValidationError) {
                FgaApiValidationError fgaError = (FgaApiValidationError) e.getCause();
                if (fgaError.getMessage() != null && fgaError.getMessage().contains("did not exist")) {
                    log.debug("Tuple does not exist, ignoring: user={}, object={}, relation={}",
                        command.username(), command.objectId(), command.relation());
                    return;
                }
            }
            throw new RuntimeException("Error removing permission", e);
        }
    }
}
