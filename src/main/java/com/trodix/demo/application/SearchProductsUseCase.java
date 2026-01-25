package com.trodix.demo.application;

import com.trodix.demo.application.exceptions.ProductException;
import com.trodix.demo.domain.model.Product;
import com.trodix.demo.domain.model.ProductQuery;
import com.trodix.demo.domain.port.SearchProvider;
import com.trodix.demo.domain.search.pagination.Page;
import com.trodix.demo.domain.search.pagination.Pageable;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import dev.openfga.sdk.api.client.model.ClientListObjectsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchProductsUseCase {

    private final SearchProvider<Product, ProductQuery> searchProvider;

    private final AuthenticationService authService;

    private final OpenFgaClient fgaClient;

    public Page<Product> searchProducts(ProductQuery query) {
        Page<Product> unfilteredProducts = searchProvider.searchEntity(query);
        return getFilteredProducts(authService.getTenant(), unfilteredProducts);
    }

    private Page<Product> getFilteredProducts(String tenantId, Page<Product> unfilteredProducts) {
        String user = "user:" + authService.getUsername();
        String entityObject = "entity:" + tenantId + "/product";

        try {
            ClientCheckRequest checkRequest = new ClientCheckRequest()
                    .user(user)
                    .relation("read")
                    ._object(entityObject);

            boolean hasGlobalAccess = Boolean.TRUE.equals(fgaClient.check(checkRequest).get().getAllowed());

            if (hasGlobalAccess) {
                log.debug("User {} has global read access to all products in tenant {}", user, tenantId);
                return unfilteredProducts;
            }

            ClientListObjectsRequest listObjectsRequest = new ClientListObjectsRequest()
                    .user(user)
                    .relation("read")
                    .type("product");

            Set<String> allowedProductIds = fgaClient.listObjects(listObjectsRequest)
                    .get()
                    .getObjects()
                    .stream()
                    .map(obj -> obj.replace("product:", ""))
                    .collect(Collectors.toSet());

            log.debug("User {} has read access to {} products", user, allowedProductIds.size());

            List<Product> filteredEntries = unfilteredProducts.getEntries().stream()
                    .filter(product -> allowedProductIds.contains(product.getId().toString()))
                    .toList();

            // FIXME pageable.getCount() est faux, il faudrait connaitre le nombre total de products que l'utilisateur a la permission de read
            //  correspondant au critère de recherche
            Pageable pageable = unfilteredProducts.getPageable();
            return new Page<>(
                    new Pageable(pageable.getPage(), pageable.getPageSize(), filteredEntries.size(), pageable.getCount()),
                    filteredEntries
            );

        } catch (Exception e) {
            throw new ProductException("Error listing products for user %s in tenant %s", e, user, tenantId);
        }
    }

}
