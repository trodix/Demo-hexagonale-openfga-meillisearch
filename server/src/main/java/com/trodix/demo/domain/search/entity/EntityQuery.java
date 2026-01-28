package com.trodix.demo.domain.search.entity;

import com.trodix.demo.domain.search.query.Paging;
import com.trodix.demo.domain.search.query.QueryParams;
import lombok.Data;

@Data
public abstract class EntityQuery<T extends QueryParams> {

    private T params;

    private Paging paging;

}
