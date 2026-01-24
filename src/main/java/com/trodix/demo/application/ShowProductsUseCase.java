package com.trodix.demo.application;

import com.trodix.demo.application.exceptions.ProductException;
import com.trodix.demo.domain.model.Product;
import com.trodix.demo.domain.port.ProductsProvider;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowProductsUseCase {

    private final ProductsProvider productsProvider;

    private final OpenFgaClient fgaClient;

    private final AuthenticationService authService;

    public List<Product> showProducts() {
        String user = "user:" + authService.getUsername();

        try {
            ClientCheckRequest checkRequest = new ClientCheckRequest()
                    .user(user)
                    .relation("read")
                    ._object("entity:product");

            boolean hasGlobalAccess = Boolean.TRUE.equals(fgaClient.check(checkRequest).get().getAllowed());

            if (hasGlobalAccess) {
                log.debug("User {} has global read access to all products", user);
                return productsProvider.getProducts();
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

            return productsProvider.getProducts().stream()
                    .filter(product -> allowedProductIds.contains(product.getId().toString()))
                    .toList();

        } catch (Exception e) {
            throw new ProductException("Error listing products for user %s", e, user);
        }
    }

    public Product getProduct(Long id) {
        log.debug("Getting product with id: {}", id);
        return productsProvider.getProduct(id);
    }

    //@Transactional
    public Product createProduct(Product product) {
        log.debug("Creating product: {}", product);

        try {
            Product createdProduct = productsProvider.createProduct(product);
            String objectId = "product:" + createdProduct.getId();
            String user = "user:" + authService.getUsername();
            fgaClient
                    .writeTuples(
                        List.of(
                                new ClientTupleKey()
                                        .user(user)
                                        .relation("owner")
                                        ._object(objectId)
                        )
                    )
                    .get();
            return createdProduct;
        } catch (Exception e) {
            throw new ProductException("Error creating product %s", e, product.getId());
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
            String objectId = "product:" + id;

            var readRequest = new ClientReadRequest()._object(objectId);
            var existingTuples = fgaClient.read(readRequest).get().getTuples();

            if (!existingTuples.isEmpty()) {
                List<ClientTupleKeyWithoutCondition> tuplesToDelete = existingTuples.stream()
                        .map(tuple -> new ClientTupleKeyWithoutCondition()
                                .user(tuple.getKey().getUser())
                                .relation(tuple.getKey().getRelation())
                                ._object(tuple.getKey().getObject()))
                        .toList();

                fgaClient.deleteTuples(tuplesToDelete).get();
            }
        } catch (Exception e) {
            throw new ProductException("Error deleting product %s", e, id);
        }
    }

}
