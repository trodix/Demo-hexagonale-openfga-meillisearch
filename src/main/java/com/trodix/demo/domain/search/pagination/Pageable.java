package com.trodix.demo.domain.search.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pageable {

    private long page;
    private long pageSize;
    private long pageCount;
    private long count;

}
