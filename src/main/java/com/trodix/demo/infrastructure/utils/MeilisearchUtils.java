package com.trodix.demo.infrastructure.utils;

import com.trodix.demo.domain.model.Product;
import com.trodix.demo.domain.search.pagination.Page;
import com.trodix.demo.domain.search.pagination.Pageable;
import lombok.Data;

import java.util.List;

public class MeilisearchUtils {

    @Data
    public static class SearchResultPaginated<T> {
        private List<T> hits;
        private String query;
        private int processingTimeMs;
        private int hitsPerPage;
        private int page;
        private int totalPages;
        private int totalHits;
        private String requestUid;
    }

    public static Page<Product> toEntityPage(SearchResultPaginated<Product> searchResultPaginated) {
        return new Page<>(
                new Pageable(
                        searchResultPaginated.getPage(),
                        searchResultPaginated.getHitsPerPage(),
                        searchResultPaginated.getTotalPages(),
                        searchResultPaginated.getTotalHits()
                ),
                searchResultPaginated.getHits()
        );
    }

}
