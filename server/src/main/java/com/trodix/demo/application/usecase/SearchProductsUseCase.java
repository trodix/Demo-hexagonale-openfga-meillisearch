package com.trodix.demo.application.usecase;

import com.trodix.demo.application.port.security.AuthenticationAdapter;
import com.trodix.demo.application.exceptions.ProductException;
import com.trodix.demo.application.port.ProductAuthorizationPort;
import com.trodix.demo.domain.model.Product;
import com.trodix.demo.domain.model.ProductQuery;
import com.trodix.demo.domain.port.SearchProvider;
import com.trodix.demo.domain.search.entity.Partial;
import com.trodix.demo.domain.search.pagination.Page;
import com.trodix.demo.domain.search.pagination.Pageable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchProductsUseCase {

    private final SearchProvider<Partial<Product>, ProductQuery> searchProvider;

    private final AuthenticationAdapter authService;

    private final ProductAuthorizationPort authorizationPort;

    public Page<Partial<Product>> searchProducts(ProductQuery query) {
        Page<Partial<Product>> unfilteredProducts = searchProvider.searchEntity(query);
        return getFilteredProducts(authService.getTenant(), unfilteredProducts);
    }

    private Page<Partial<Product>> getFilteredProducts(String tenantId, Page<Partial<Product>> unfilteredProducts) {
        String username = authService.getUsername();
        try {

            if (authService.isSystemUser()) {
                log.debug("System user has full access to all products");
                return unfilteredProducts;
            }

            if (authorizationPort.hasGlobalReadAccess(username, tenantId)) {
                log.debug("User {} has global read access to all products in tenant {}", username, tenantId);
                return unfilteredProducts;
            }

            Set<String> allowedProductIds = authorizationPort.getReadableProductIds(username, tenantId);
            log.debug("User {} has read access to {} products", username, allowedProductIds.size());

            List<Partial<Product>> filteredEntries = unfilteredProducts.getEntries().stream()
                    .filter(p -> allowedProductIds.contains(p.get("id").toString()))
                    .toList();

            Pageable pageable = unfilteredProducts.getPageable();
            return new Page<>(
                    new Pageable(pageable.getPage(), filteredEntries.size(), filteredEntries.size() < pageable.getPageSize()),
                    filteredEntries
            );

        } catch (Exception e) {
            throw new ProductException("Error listing products for user %s in tenant %s", e, username, tenantId);
        }
    }

}
