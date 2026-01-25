package com.trodix.demo.infrastructure.adapter;

import com.trodix.demo.application.AuthenticationService;
import com.trodix.demo.application.CrudProductsUseCase;
import com.trodix.demo.domain.model.Product;
import com.trodix.demo.domain.port.ProductsProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductsControllerAdapter implements ProductsProvider {

    private final CrudProductsUseCase crudProductsUseCase;
    private final AuthenticationService auth;

    @GetMapping(value = "products", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public List<Product> getProducts() {
        String tenantId = auth.getTenant();
        return crudProductsUseCase.showProducts(tenantId);
    }

    @GetMapping(value = "products/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@fga.check('entity', @authenticationService.getTenant() + '/product', 'read', 'user')")
    public Product getProduct(@PathVariable Long id) {
        return crudProductsUseCase.getProduct(id);
    }

    @PostMapping(value = "products", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("@fga.check('entity', @authenticationService.getTenant() + '/product', 'write', 'user')")
    public Product createProduct(@RequestBody Product product) {
        String tenantId = auth.getTenant();
        return crudProductsUseCase.createProduct(tenantId, product);
    }

    @PutMapping(value = "products", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@fga.check('entity', @authenticationService.getTenant() + '/product', 'write', 'user')")
    public Product updateProduct(@RequestBody Product product) {
        return crudProductsUseCase.updateProduct(product);
    }

    @DeleteMapping(value = "products/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("@fga.check('entity', @authenticationService.getTenant() + '/product', 'write', 'user')")
    public void deleteProduct(@PathVariable Long id) {
        crudProductsUseCase.deleteProduct(id);
    }

}
