package com.trodix.demo.application;

import com.trodix.demo.application.exceptions.ProductException;
import com.trodix.demo.domain.model.Product;
import com.trodix.demo.domain.port.ProductsProvider;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientReadRequest;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientTupleKeyWithoutCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowProductsUseCase {

    private final ProductsProvider productsProvider;

    private final OpenFgaClient fgaClient;

    private final AuthenticationService authService;

    public List<Product> showProducts() {
        List<Product> products = productsProvider.getProducts();
        log.debug("Products count: {}", products.size());

        return products;
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
                                        .relation("read")
                                        ._object(objectId),
                                new ClientTupleKey()
                                        .user(user)
                                        .relation("write")
                                        ._object(objectId),
                                new ClientTupleKey()
                                        .user(user)
                                        .relation("delete")
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
