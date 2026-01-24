package com.trodix.demo.application.config;

import com.trodix.demo.domain.port.ProductsProvider;
import com.trodix.demo.infrastructure.adapter.DummyjsonProductsAdapter;
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
    ProductsProvider productsProvider(@Qualifier("productsRestClient") RestClient restClient, ObjectMapper objectMapper) {
        return new DummyjsonProductsAdapter(restClient, objectMapper);
    }

}
