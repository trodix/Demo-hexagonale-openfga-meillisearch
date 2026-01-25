package com.trodix.demo.infrastructure.adapter;

import com.trodix.demo.application.AuthenticationService;
import com.trodix.demo.application.CrudProductsUseCase;
import com.trodix.demo.application.SearchProductsUseCase;
import com.trodix.demo.domain.model.Product;
import com.trodix.demo.domain.model.ProductQuery;
import com.trodix.demo.domain.search.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductsControllerAdapter {

    private final CrudProductsUseCase crudProductsUseCase;
    private final SearchProductsUseCase searchProductProvider;
    private final AuthenticationService auth;

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public List<Product> getProducts() {
        String tenantId = auth.getTenant();
        return crudProductsUseCase.showProducts(tenantId);
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@fga.check('entity', @authenticationService.getTenant() + '/product', 'read', 'user')")
    public Product getProduct(@PathVariable Long id) {
        return crudProductsUseCase.getProduct(id);
    }

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("@fga.check('entity', @authenticationService.getTenant() + '/product', 'write', 'user')")
    public Product createProduct(@RequestBody Product product) {
        String tenantId = auth.getTenant();
        return crudProductsUseCase.createProduct(tenantId, product);
    }

    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@fga.check('entity', @authenticationService.getTenant() + '/product', 'write', 'user')")
    public Product updateProduct(@RequestBody Product product) {
        return crudProductsUseCase.updateProduct(product);
    }

    @DeleteMapping(value = "{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("@fga.check('entity', @authenticationService.getTenant() + '/product', 'write', 'user')")
    public void deleteProduct(@PathVariable Long id) {
        crudProductsUseCase.deleteProduct(id);
    }

    @GetMapping(value = "search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Page<Product> searchProducts(@RequestBody ProductQuery queryRequest) {
        return searchProductProvider.searchProducts(queryRequest);
    }

}
