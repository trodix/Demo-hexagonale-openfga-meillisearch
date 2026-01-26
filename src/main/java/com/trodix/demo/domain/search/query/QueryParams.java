package com.trodix.demo.domain.search.query;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public abstract class QueryParams {

    private List<String> includes;
    private Map<String, String> sort;
    private List<String> attributesToHighlight;
    private String highlightPreTag;
    private String highlightPostTag;

}
