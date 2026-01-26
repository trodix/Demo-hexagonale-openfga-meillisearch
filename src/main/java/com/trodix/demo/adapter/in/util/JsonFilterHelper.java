package com.trodix.demo.adapter.in.util;

import org.springframework.http.ResponseEntity;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JsonFilterHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> ResponseEntity<String> filterResponse(T data, List<String> includes) throws JacksonException {
        if (includes == null || includes.isEmpty()) {
            return ResponseEntity.ok(objectMapper.writeValueAsString(data));
        }

        JsonNode jsonNode = objectMapper.valueToTree(data);

        if (jsonNode.isObject() && jsonNode.has("entries")) {
            ArrayNode entriesArray = (ArrayNode) jsonNode.get("entries");
            ArrayNode filteredEntries = objectMapper.createArrayNode();

            Set<String> includeSet = new HashSet<>(includes);

            for (JsonNode entry : entriesArray) {
                if (entry.isObject()) {
                    ObjectNode entryObject = (ObjectNode) entry;
                    ObjectNode filteredEntry = objectMapper.createObjectNode();
                    entryObject.properties().forEach(field -> {
                        if (includeSet.contains(field.getKey())) {
                            filteredEntry.set(field.getKey(), field.getValue());
                        }
                    });
                    filteredEntries.add(filteredEntry);
                }
            }

            ((ObjectNode) jsonNode).set("entries", filteredEntries);
        }

        return ResponseEntity.ok(objectMapper.writeValueAsString(jsonNode));
    }

}
