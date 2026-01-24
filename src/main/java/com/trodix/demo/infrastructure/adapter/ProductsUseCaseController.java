package com.trodix.demo.infrastructure.adapter;

import com.trodix.demo.application.ShowProductsUseCase;
import com.trodix.demo.application.ShowProductsUseCaseProvider;
import com.trodix.demo.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductsUseCaseController implements ShowProductsUseCaseProvider {

    private final ShowProductsUseCase showProductsUseCase;

    @GetMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@fga.check('entity', 'product', 'read', 'user')")
    @Override
    public List<Product> showProducts() {
        return showProductsUseCase.showProducts();
    }

    @GetMapping(value = "/products/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@fga.check('product', #id, 'read', 'user')")
    @Override
    public Product getProduct(@PathVariable Long id) {
        return showProductsUseCase.getProduct(id);
    }

    @PostMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("@fga.check('entity', 'product', 'write', 'user')")
    @Override
    public Product createProduct(@RequestBody Product product) {
        return showProductsUseCase.createProduct(product);
    }

    @PutMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("@fga.check('product', #product.id, 'write', 'user')")
    @Override
    public Product updateProduct(@RequestBody Product product) {
        return showProductsUseCase.updateProduct(product);
    }

    @DeleteMapping(value = "/products/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("@fga.check('product', #id, 'write', 'user')")
    @Override
    public void deleteProduct(@PathVariable Long id) {
        showProductsUseCase.deleteProduct(id);
    }

}
