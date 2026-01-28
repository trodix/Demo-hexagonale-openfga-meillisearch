package com.trodix.demo.application.usecase;

import com.trodix.demo.adapter.in.dto.EntityInfo;
import com.trodix.demo.adapter.in.dto.EntityListResponse;
import com.trodix.demo.adapter.in.security.SpringAuthenticationAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListEntitiesUseCase {

    private final SpringAuthenticationAdapter auth;

    public EntityListResponse listEntities() {
        String tenantId = auth.getTenant();

        // Pour MVP: retourne liste hardcodée avec "product" uniquement
        List<EntityInfo> entities = List.of(
                new EntityInfo(
                        tenantId + "/product",
                        "Products",
                        List.of("read", "write", "delete")
                )
        );

        return new EntityListResponse(entities);
    }
}
