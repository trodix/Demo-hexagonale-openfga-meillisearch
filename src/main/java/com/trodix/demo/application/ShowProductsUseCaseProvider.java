package com.trodix.demo.application;

import com.trodix.demo.domain.model.Product;

import java.util.List;

public interface ShowProductsUseCaseProvider {

    List<Product> showProducts();

    Product getProduct(Long id);

    Product createProduct(Product product);

    Product updateProduct(Product product);

    void deleteProduct(Long id);

}
