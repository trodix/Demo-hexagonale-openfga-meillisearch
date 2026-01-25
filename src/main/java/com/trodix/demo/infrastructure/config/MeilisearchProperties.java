package com.trodix.demo.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "meilisearch")
@Data
public class MeilisearchProperties {

    private String apiUrl;

    private String apiKey;

}
