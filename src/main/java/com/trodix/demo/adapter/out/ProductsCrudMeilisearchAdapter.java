package com.trodix.demo.adapter.out;

import com.meilisearch.sdk.Client;
import com.trodix.demo.domain.model.Product;
import com.trodix.demo.domain.port.ProductsProvider;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class ProductsCrudMeilisearchAdapter implements ProductsProvider {

    public static final String PRODUCTS_INDEX = "products";

    private final Client msClient;

    private final ObjectMapper objectMapper;

    @Override
    public List<Product> getProducts() {
        return Arrays.asList(msClient.getIndex(PRODUCTS_INDEX).getDocuments(Product.class).getResults());
    }

    @Override
    public Product getProduct(Long id) {
        return msClient.getIndex(PRODUCTS_INDEX).getDocument(id.toString(), Product.class);
    }

    @Override
    public Product createProduct(Product product) {
        String json = objectMapper.writeValueAsString(product);
        msClient.getIndex(PRODUCTS_INDEX).addDocuments(json, "id");
        return product;
    }

    @Override
    public Product updateProduct(Product product) {
        String json = objectMapper.writeValueAsString(product);
        msClient.getIndex(PRODUCTS_INDEX).updateDocuments(json, "id");
        return product;
    }

    @Override
    public void deleteProduct(Long id) {
        msClient.getIndex(PRODUCTS_INDEX).deleteDocument(id.toString());
    }

}
