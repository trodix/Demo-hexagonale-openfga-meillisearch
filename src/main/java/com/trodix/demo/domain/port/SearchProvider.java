package com.trodix.demo.domain.port;

import com.trodix.demo.domain.search.entity.EntityQuery;
import com.trodix.demo.domain.search.pagination.Page;
import com.trodix.demo.domain.search.query.QueryParams;

public interface SearchProvider<T, R extends EntityQuery<? extends QueryParams>> {

    Page<T> searchEntity(R query);

}
