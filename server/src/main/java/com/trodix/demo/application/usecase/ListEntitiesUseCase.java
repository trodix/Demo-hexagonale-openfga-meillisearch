package com.trodix.demo.application.usecase;

import com.trodix.demo.application.port.security.AuthenticationAdapter;
import com.trodix.demo.application.model.EntityInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListEntitiesUseCase {

    private final AuthenticationAdapter auth;

    public List<EntityInfo> listEntities() {
        String tenantId = auth.getTenant();

        // Pour MVP: retourne liste hardcodée avec "product" uniquement
        return List.of(
                new EntityInfo(
                        tenantId + "/product",
                        "Products",
                        List.of("read", "write", "delete")
                )
        );
    }
}
