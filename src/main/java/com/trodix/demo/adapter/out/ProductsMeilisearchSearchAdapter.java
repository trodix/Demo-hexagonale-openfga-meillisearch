package com.trodix.demo.adapter.out;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.SearchRequest;
import com.trodix.demo.domain.model.Product;
import com.trodix.demo.domain.model.ProductQuery;
import com.trodix.demo.domain.port.SearchProvider;
import com.trodix.demo.domain.search.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import static com.trodix.demo.adapter.out.MeilisearchUtils.SearchResultPaginated;
import static com.trodix.demo.adapter.out.MeilisearchUtils.toEntityPage;
import static com.trodix.demo.adapter.out.ProductsCrudMeilisearchAdapter.PRODUCTS_INDEX;

@RequiredArgsConstructor
@Slf4j
public class ProductsMeilisearchSearchAdapter implements SearchProvider<Product, ProductQuery> {

    private final Client msClient;

    private final ObjectMapper mapper;

    @Override
    public Page<Product> searchEntity(ProductQuery query) {
        String rawResponse = msClient.getIndex(PRODUCTS_INDEX).rawSearch(new SearchRequest(query.getParams().getSearchTerms())
                .setOffset(query.getPaging().getOffset())
                .setLimit(query.getPaging().getLimit())
        );

        if (log.isTraceEnabled()) {
            log.debug("Raw response from Meilisearch: {}", rawResponse);
        }

        SearchResultPaginated<Product> result = mapper.readValue(
                rawResponse,
                mapper.getTypeFactory().constructParametricType(SearchResultPaginated.class, Product.class)
        );

        return toEntityPage(result, query.getPaging());
    }

}
