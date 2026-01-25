package com.trodix.demo.application.usecase;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.trodix.demo.domain.model.Product;
import com.trodix.demo.domain.port.ProductsProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static com.trodix.demo.adapter.out.ProductsCrudMeilisearchAdapter.PRODUCTS_INDEX;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexProductsUseCase implements CommandLineRunner {

    @Qualifier("dummyjsonProductsAdapter")
    private final ProductsProvider dummyjsonProductsAdapter;

    @Qualifier("meilisearchProductsAdapter")
    private final ProductsProvider meilisearchProductsAdapter;

    private final Client msClient;

    @Override
    public void run(String... args) throws Exception {

        if (Arrays.stream(msClient.getIndexes().getResults()).map(Index::getUid).anyMatch(PRODUCTS_INDEX::equals)) {
            log.info("Index {} already exists in Meilisearch", PRODUCTS_INDEX);
            return;
        }

        log.info("Creating index {} in Meilisearch", PRODUCTS_INDEX);
        msClient.createIndex(PRODUCTS_INDEX);

        List<Product> products = dummyjsonProductsAdapter.getProducts();

        for (Product product : products) {
            meilisearchProductsAdapter.createProduct(product);
        }
        log.info("Products indexed in Meilisearch: {}", products.size());
    }
}
