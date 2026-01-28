package com.trodix.demo.application.port;

import java.util.Set;

public interface ProductAuthorizationPort {

    boolean hasGlobalReadAccess(String username, String tenantId);

    Set<String> getReadableProductIds(String username, String tenantId);

    void onProductCreated(String username, String tenantId, String productId);

    void onProductDeleted(String productId);

}
