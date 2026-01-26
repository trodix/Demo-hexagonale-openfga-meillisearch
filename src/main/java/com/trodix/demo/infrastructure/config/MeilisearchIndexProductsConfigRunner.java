package com.trodix.demo.infrastructure.config;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.model.Settings;
import com.meilisearch.sdk.model.TaskInfo;
import com.trodix.demo.domain.model.Product;
import com.trodix.demo.domain.port.ProductsProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.trodix.demo.adapter.out.ProductsCrudMeilisearchAdapter.PRODUCTS_INDEX;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeilisearchIndexProductsConfigRunner implements CommandLineRunner {

    @Qualifier("dummyjsonProductsAdapter")
    private final ProductsProvider dummyjsonProductsAdapter;

    @Qualifier("meilisearchProductsAdapter")
    private final ProductsProvider meilisearchProductsAdapter;

    private final Client msClient;

    @Override
    public void run(String... args) throws Exception {

        log.info("Creating/updating index {} in Meilisearch", PRODUCTS_INDEX);
        msClient.createIndex(PRODUCTS_INDEX);

        log.info("Index configuration for {}", PRODUCTS_INDEX);
        Settings settings = new Settings();
        settings.setSearchableAttributes(new String[]{"*"});
        settings.setSortableAttributes(new String[]{"id", "title"});
        TaskInfo updateSettingsTask = msClient.getIndex(PRODUCTS_INDEX).updateSettings(settings);
        log.info("Updated settings for {} with taskInfo (id={}, status={})",
                PRODUCTS_INDEX,  updateSettingsTask.getTaskUid(),  updateSettingsTask.getStatus());

        List<Product> products = dummyjsonProductsAdapter.getProducts();
        meilisearchProductsAdapter.createProductsBatch(products, 50);

        log.info("Products indexed in Meilisearch: {}", products.size());
    }
}
