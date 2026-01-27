package com.trodix.demo.adapter.in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityInfo {
    private String id;
    private String name;
    private List<String> availableRelations;
}
