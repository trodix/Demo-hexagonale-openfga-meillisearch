package com.trodix.demo.adapter.out;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.model.SearchResultPaginated;
import com.trodix.demo.domain.model.Product;
import com.trodix.demo.domain.model.ProductQuery;
import com.trodix.demo.domain.port.SearchProvider;
import com.trodix.demo.domain.search.entity.Partial;
import com.trodix.demo.domain.search.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.trodix.demo.adapter.out.MeilisearchUtils.toEntityPage;
import static com.trodix.demo.adapter.out.ProductsCrudMeilisearchAdapter.PRODUCTS_INDEX;

@RequiredArgsConstructor
@Slf4j
public class ProductsMeilisearchSearchAdapter implements SearchProvider<Partial<Product>, ProductQuery> {

    private final Client msClient;

    private final ObjectMapper mapper;

    @Override
    public Page<Partial<Product>> searchEntity(ProductQuery query) {
        SearchRequest searchRequest = new SearchRequest(query.getParams().getSearchTerms())
                .setOffset(query.getPaging().getOffset())
                .setLimit(query.getPaging().getLimit());

        if (query.getParams().getIncludes() != null && !query.getParams().getIncludes().isEmpty()) {
            searchRequest.setAttributesToRetrieve(query.getParams().getIncludes().toArray(new String[0]));
        }

        if (query.getParams().getAttributesToHighlight() != null && !query.getParams().getAttributesToHighlight().isEmpty()) {
            searchRequest.setAttributesToHighlight(query.getParams().getAttributesToHighlight().toArray(new String[0]));
        }

        if (StringUtils.hasText(query.getParams().getHighlightPreTag())) {
            searchRequest.setHighlightPreTag(query.getParams().getHighlightPreTag());
        }

        if (StringUtils.hasText(query.getParams().getHighlightPostTag())) {
            searchRequest.setHighlightPostTag(query.getParams().getHighlightPostTag());
        }

        if (query.getParams().getSort() != null && !query.getParams().getSort().isEmpty()) {
            List<String> sortArgs = new ArrayList<>();
            for (Map.Entry<String, String> entry : query.getParams().getSort().entrySet()) {
                sortArgs.add(entry.getKey() + ":" + entry.getValue());
            }
            searchRequest.setSort(sortArgs.toArray(new String[0]));
        }

        String rawResponse = msClient.getIndex(PRODUCTS_INDEX).rawSearch(searchRequest);

        if (log.isTraceEnabled()) {
            log.debug("Raw response from Meilisearch: {}", rawResponse);
        }

        SearchResultPaginated result = mapper.readValue(
                rawResponse,
                SearchResultPaginated.class
        );

        return toEntityPage(result, query.getPaging());
    }

}
