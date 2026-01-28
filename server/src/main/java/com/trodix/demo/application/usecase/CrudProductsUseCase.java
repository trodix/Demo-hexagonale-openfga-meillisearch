package com.trodix.demo.application.usecase;

import com.trodix.demo.application.exceptions.ProductException;
import com.trodix.demo.application.port.ProductAuthorizationPort;
import com.trodix.demo.application.port.security.AuthenticationAdapter;
import com.trodix.demo.domain.model.Product;
import com.trodix.demo.domain.port.ProductsProvider;
import dev.openfga.sdk.api.client.model.ClientReadRequest;
import dev.openfga.sdk.api.client.model.ClientTupleKeyWithoutCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrudProductsUseCase {

    @Qualifier("meilisearchProductsAdapter")
    private final ProductsProvider productsProvider;

    private final ProductAuthorizationPort productAuthorizationPort;

    private final AuthenticationAdapter authService;

    public List<Product> showProducts(String tenantId) {
        if (authService.isSystemUser()) {
            log.debug("System user has full access to all products");
            return productsProvider.getProducts();
        }

        return getFilteredProducts(tenantId);
    }

    private List<Product> getFilteredProducts(String tenantId) {

        String username = authService.getUsername();
        try {
            boolean hasGlobalAccess = productAuthorizationPort.hasGlobalReadAccess(username, tenantId);

            if (hasGlobalAccess) {
                log.debug("User {} has global read access to all products in tenant {}", username, tenantId);
                return productsProvider.getProducts();
            }

            Set<String> allowedProductIds = productAuthorizationPort.getReadableProductIds(username, tenantId);

            log.debug("User {} has read access to {} products", username, allowedProductIds.size());

            return productsProvider.getProducts().stream()
                    .filter(product -> allowedProductIds.contains(product.getId().toString()))
                    .toList();

        } catch (Exception e) {
            throw new ProductException("Error listing products for user %s in tenant %s", e, username, tenantId);
        }
    }

    public Product getProduct(Long id) {
        log.debug("Getting product with id: {}", id);
        return productsProvider.getProduct(id);
    }

    //@Transactional
    public Product createProduct(String tenantId, Product product) {
        log.debug("Creating product: {} in tenant: {}", product, tenantId);

        try {
            Product createdProduct = productsProvider.createProduct(product);
            productAuthorizationPort.onProductCreated(authService.getUsername(), tenantId, createdProduct.getId().toString());
            return createdProduct;
        } catch (Exception e) {
            throw new ProductException("Error creating product %s in tenant %s", e, product.getId(), tenantId);
        }
    }

    //@Transactional
    public Product updateProduct(Product product) {
        log.debug("Updating product: {}", product);
        return productsProvider.updateProduct(product);
    }

    //@Transactional
    public void deleteProduct(Long id) {
        log.debug("Deleting product with id: {}", id);
        try {
            productsProvider.deleteProduct(id);
            productAuthorizationPort.onProductDeleted(id.toString());
        } catch (Exception e) {
            throw new ProductException("Error deleting product %s", e, id);
        }
    }

}
