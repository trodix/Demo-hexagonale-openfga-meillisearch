package com.trodix.demo.domain.search.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

@Data
@AllArgsConstructor
public class Page<T> {

    private Pageable pageable;
    private Collection<T> entries;

}
