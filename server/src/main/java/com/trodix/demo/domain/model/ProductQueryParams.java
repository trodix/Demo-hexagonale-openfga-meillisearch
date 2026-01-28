package com.trodix.demo.domain.model;

import com.trodix.demo.domain.search.query.QueryParams;
import lombok.Data;

@Data
public class ProductQueryParams extends QueryParams {

    private String searchTerms;

}
