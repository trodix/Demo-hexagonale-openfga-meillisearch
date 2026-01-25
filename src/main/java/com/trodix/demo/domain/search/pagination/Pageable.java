package com.trodix.demo.domain.search.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Limitation: On ne peut pas avoir le nombre total d'élements avec le filtre local sur les permissions
 */
@Data
@AllArgsConstructor
public class Pageable {

    private long page;
    private long pageSize;
    private boolean hasMoreElements;

}
