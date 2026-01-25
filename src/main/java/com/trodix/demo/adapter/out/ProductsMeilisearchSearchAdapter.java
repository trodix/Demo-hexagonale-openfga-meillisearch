package com.trodix.demo.adapter.out;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.SearchRequest;
import com.trodix.demo.domain.model.Product;
import com.trodix.demo.domain.model.ProductQuery;
import com.trodix.demo.domain.port.SearchProvider;
import com.trodix.demo.domain.search.pagination.Page;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

import static com.trodix.demo.adapter.out.ProductsCrudMeilisearchAdapter.PRODUCTS_INDEX;
import static com.trodix.demo.adapter.out.MeilisearchUtils.SearchResultPaginated;
import static com.trodix.demo.adapter.out.MeilisearchUtils.toEntityPage;

@RequiredArgsConstructor
public class ProductsMeilisearchSearchAdapter implements SearchProvider<Product, ProductQuery> {

    private final Client msClient;

    private final ObjectMapper mapper;

    @Override
    public Page<Product> searchEntity(ProductQuery query) {
        String rawResponse = msClient.getIndex(PRODUCTS_INDEX).rawSearch(new SearchRequest(query.getParams().getSearchTerms())
                .setPage(query.getPaging().getOffset() != 0 ? (query.getPaging().getOffset() * query.getPaging().getPageSize()) : 1)
                .setHitsPerPage(query.getPaging().getPageSize())
        );

        SearchResultPaginated<Product> result = mapper.readValue(
                rawResponse,
                mapper.getTypeFactory().constructParametricType(SearchResultPaginated.class, Product.class)
        );

        return toEntityPage(result);
    }

}
