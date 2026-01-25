package com.trodix.demo.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import com.meilisearch.sdk.exceptions.JsonDecodingException;
import com.meilisearch.sdk.exceptions.JsonEncodingException;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.json.JacksonJsonHandler;
import com.meilisearch.sdk.json.JsonHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.util.StdDateFormat;

@Configuration
@RequiredArgsConstructor
public class MeilisearchConfig {

    private final MeilisearchProperties props;

    @Bean
    Client meilisearchClient() {
        Config config = new Config(
                props.getApiUrl(),
                props.getApiKey(),
                new Jackson3JsonHandler()
        );

        return new Client(config);
    }

    /**
     * Rend compatible meilisearch Client avec Jackson 3.
     * <p>
     * Remplace {@link JacksonJsonHandler}
     * </p>
     */
    public static class Jackson3JsonHandler implements JsonHandler {

        private final ObjectMapper mapper;
        private final ObjectMapper writerMapper;

        public Jackson3JsonHandler() {
            this.mapper = JsonMapper.builder()
                    .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .defaultDateFormat(new StdDateFormat().withColonInTimeZone(true))
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .build();
            this.writerMapper = this.mapper.rebuild()
                    .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                    .build();
        }

        public Jackson3JsonHandler(ObjectMapper mapper) {
            this.mapper = mapper;
            this.writerMapper = mapper.rebuild()
                    .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                    .build();
        }

        @Override
        public String encode(Object o) throws MeilisearchException {
            if (o != null && o.getClass() == String.class) {
                return (String) o;
            }
            try {
                return writerMapper.writeValueAsString(o);
            } catch (JacksonException e) {
                throw new JsonEncodingException(e);
            }
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public <T> T decode(Object o, Class<T> targetClass, Class<?>... parameters)
                throws MeilisearchException {
            if (o == null) {
                throw new JsonDecodingException("Response to deserialize is null");
            }
            if (targetClass == String.class) {
                return (T) o;
            }
            try {
                if (parameters == null || parameters.length == 0) {
                    return mapper.readValue((String) o, targetClass);
                } else {
                    return mapper.readValue(
                            (String) o,
                            mapper.getTypeFactory().constructParametricType(targetClass, parameters));
                }
            } catch (JacksonException e) {
                throw new JsonDecodingException(e);
            }
        }
    }
}
