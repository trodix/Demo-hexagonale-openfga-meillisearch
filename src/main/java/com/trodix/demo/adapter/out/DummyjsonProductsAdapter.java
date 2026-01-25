package com.trodix.demo.adapter.out;

import com.trodix.demo.domain.model.Product;
import com.trodix.demo.domain.port.ProductsProvider;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DummyjsonProductsAdapter implements ProductsProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<Product> getProducts() {
        return Optional.ofNullable(
                restClient
                        .get()
                        .uri("https://dummyjson.com/products")
                        .retrieve()
                        .body(ProductResponse.class)
                )
                .map(ProductResponse::getProducts)
                .orElse(List.of());
    }

    @Override
    public Product getProduct(Long id) {
        return restClient
                .get()
                .uri("https://dummyjson.com/products/{id}", id)
                .retrieve()
                .body(Product.class);
    }

    @Override
    public Product createProduct(Product product) {
        // TODO
        return product;
    }

    @Override
    public Product updateProduct(Product product) {
        // TODO
        return product;
    }

    @Override
    public void deleteProduct(Long id) {
        // TODO
    }

    @Data
    static class ProductResponse {
        private List<Product> products = new ArrayList<>();
    }

}
