package com.trodix.demo.infrastructure.adapter;

import com.trodix.demo.application.ShowProductsUseCase;
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
public class ProductsUseCaseController {

    private final ShowProductsUseCase showProductsUseCase;

    @GetMapping(value = "tenant/{tenantId}/products", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public List<Product> showProducts(@PathVariable String tenantId) {
        return showProductsUseCase.showProducts(tenantId);
    }

    @GetMapping(value = "tenant/{tenantId}/products/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@fga.check('entity', #tenantId + '/product', 'read', 'user')")
    public Product getProduct(@PathVariable String tenantId, @PathVariable Long id) {
        return showProductsUseCase.getProduct(id);
    }

    @PostMapping(value = "tenant/{tenantId}/products", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("@fga.check('entity', #tenantId + '/product', 'write', 'user')")
    public Product createProduct(@PathVariable String tenantId, @RequestBody Product product) {
        return showProductsUseCase.createProduct(tenantId, product);
    }

    @PutMapping(value = "tenant/{tenantId}/products", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@fga.check('entity', #tenantId + '/product', 'write', 'user')")
    public Product updateProduct(@PathVariable String tenantId, @RequestBody Product product) {
        return showProductsUseCase.updateProduct(product);
    }

    @DeleteMapping(value = "tenant/{tenantId}/products/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("@fga.check('entity', #tenantId + '/product', 'write', 'user')")
    public void deleteProduct(@PathVariable String tenantId, @PathVariable Long id) {
        showProductsUseCase.deleteProduct(id);
    }

}
