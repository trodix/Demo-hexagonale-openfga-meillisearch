package com.trodix.demo.domain.port;

import com.trodix.demo.domain.model.Product;

import java.util.List;

public interface ProductsProvider {

    List<Product> getProducts();

    Product getProduct(Long id);

    Product createProduct(Product product);

    void createProductsBatch(List<Product> products, int batchSize);

    Product updateProduct(Product product);

    void deleteProduct(Long id);

}
