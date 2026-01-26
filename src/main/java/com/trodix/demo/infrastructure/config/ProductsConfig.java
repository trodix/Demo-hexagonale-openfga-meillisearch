package com.trodix.demo.infrastructure.config;

import com.meilisearch.sdk.Client;
import com.trodix.demo.adapter.out.DummyjsonProductsAdapter;
import com.trodix.demo.adapter.out.ProductsCrudMeilisearchAdapter;
import com.trodix.demo.adapter.out.ProductsMeilisearchSearchAdapter;
import com.trodix.demo.domain.model.Product;
import com.trodix.demo.domain.model.ProductQuery;
import com.trodix.demo.domain.port.ProductsProvider;
import com.trodix.demo.domain.port.SearchProvider;
import com.trodix.demo.domain.search.entity.Partial;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class ProductsConfig {

    @Bean
    RestClient productsRestClient() {
        return RestClient.create();
    }

    @Bean
    ProductsProvider dummyjsonProductsAdapter(@Qualifier("productsRestClient") RestClient restClient) {
        return new DummyjsonProductsAdapter(restClient);
    }

    @Bean
    ProductsProvider meilisearchProductsAdapter(Client msClient, ObjectMapper objectMapper) {
        return new ProductsCrudMeilisearchAdapter(msClient, objectMapper);
    }

    @Bean
    SearchProvider<Partial<Product>, ProductQuery> meilisearchSearchProductProvider(Client msClient, @Qualifier("meilisearchReadObjectMapper") ObjectMapper mapper) {
        return new ProductsMeilisearchSearchAdapter(msClient, mapper);
    }

}
