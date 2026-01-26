package com.trodix.demo.domain.search.query;

import lombok.Data;

import java.util.List;

@Data
public abstract class QueryParams {

    private List<String> includes;
    private List<String> attributesToHighlight;
    private String highlightPreTag;
    private String highlightPostTag;

}
