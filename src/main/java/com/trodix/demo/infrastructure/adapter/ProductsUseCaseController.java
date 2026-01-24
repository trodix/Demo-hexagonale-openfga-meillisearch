package com.trodix.demo.infrastructure.adapter;

import com.trodix.demo.application.AuthenticationService;
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
    private final AuthenticationService auth;

    @GetMapping(value = "products", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public List<Product> showProducts() {
        String tenantId = auth.getTenant();
        return showProductsUseCase.showProducts(tenantId);
    }

    @GetMapping(value = "products/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@fga.check('entity', @authenticationService.tenant() + '/product', 'read', 'user')")
    public Product getProduct(@PathVariable Long id) {
        return showProductsUseCase.getProduct(id);
    }

    @PostMapping(value = "products", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("@fga.check('entity', @authenticationService.tenant() + '/product', 'write', 'user')")
    public Product createProduct(@RequestBody Product product) {
        String tenantId = auth.getTenant();
        return showProductsUseCase.createProduct(tenantId, product);
    }

    @PutMapping(value = "products", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@fga.check('entity', @authenticationService.tenant() + '/product', 'write', 'user')")
    public Product updateProduct(@RequestBody Product product) {
        return showProductsUseCase.updateProduct(product);
    }

    @DeleteMapping(value = "products/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("@fga.check('entity', @authenticationService.tenant() + '/product', 'write', 'user')")
    public void deleteProduct(@PathVariable Long id) {
        showProductsUseCase.deleteProduct(id);
    }

}
