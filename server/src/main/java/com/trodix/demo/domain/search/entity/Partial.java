package com.trodix.demo.domain.search.entity;

import java.util.HashMap;

public class Partial<T extends Entity> extends HashMap<String, Object> {

    public Partial(HashMap<String, Object> hashMap) {
        this.putAll(hashMap);
    }

}
