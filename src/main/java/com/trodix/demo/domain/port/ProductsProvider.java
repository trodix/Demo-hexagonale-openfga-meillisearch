package com.trodix.demo.domain.port;

import com.trodix.demo.domain.model.Product;

import java.util.List;

public interface ProductsProvider {

    List<Product> getProducts();

    Product getProduct(Long id);

    Product createProduct(Product product);

    Product updateProduct(Product product);

    void deleteProduct(Long id);

}
