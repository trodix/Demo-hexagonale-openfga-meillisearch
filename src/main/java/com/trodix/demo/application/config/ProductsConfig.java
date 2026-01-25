package com.trodix.demo.application.config;

import com.meilisearch.sdk.Client;
import com.trodix.demo.domain.port.ProductsProvider;
import com.trodix.demo.domain.port.SearchProvider;
import com.trodix.demo.infrastructure.adapter.DummyjsonProductsAdapter;
import com.trodix.demo.infrastructure.adapter.ProductsCrudMeilisearchAdapter;
import com.trodix.demo.infrastructure.adapter.ProductsMeilisearchSearchAdapter;
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
    ProductsProvider dummyjsonProductsAdapter(@Qualifier("productsRestClient") RestClient restClient, ObjectMapper objectMapper) {
        return new DummyjsonProductsAdapter(restClient, objectMapper);
    }

    @Bean
    ProductsProvider meilisearchProductsAdapter(Client msClient, ObjectMapper objectMapper) {
        return new ProductsCrudMeilisearchAdapter(msClient, objectMapper);
    }

    @Bean
    SearchProvider meilisearchSearchProductProvider(Client msClient, @Qualifier("meilisearchReadObjectMapper") ObjectMapper mapper) {
        return new ProductsMeilisearchSearchAdapter(msClient, mapper);
    }

}
