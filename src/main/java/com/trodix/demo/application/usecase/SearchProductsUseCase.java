package com.trodix.demo.application.usecase;

import com.trodix.demo.adapter.in.security.SpringAuthenticationAdapter;
import com.trodix.demo.application.exceptions.ProductException;
import com.trodix.demo.application.port.ProductAuthorizationPort;
import com.trodix.demo.domain.model.Product;
import com.trodix.demo.domain.model.ProductQuery;
import com.trodix.demo.domain.port.SearchProvider;
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

    private final SearchProvider<Product, ProductQuery> searchProvider;

    private final SpringAuthenticationAdapter authService;

    private final ProductAuthorizationPort authorizationPort;

    public Page<Product> searchProducts(ProductQuery query) {
        Page<Product> unfilteredProducts = searchProvider.searchEntity(query);
        return getFilteredProducts(authService.getTenant(), unfilteredProducts);
    }

    private Page<Product> getFilteredProducts(String tenantId, Page<Product> unfilteredProducts) {
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

            List<Product> filteredEntries = unfilteredProducts.getEntries().stream()
                    .filter(p -> allowedProductIds.contains(p.getId().toString()))
                    .toList();

            Pageable pageable = unfilteredProducts.getPageable();
            return new Page<>(
                    new Pageable(pageable.getPage(), pageable.getPageSize(), filteredEntries.size(), pageable.getCount()),
                    filteredEntries
            );

        } catch (Exception e) {
            throw new ProductException("Error listing products for user %s in tenant %s", e, username, tenantId);
        }
    }

}
