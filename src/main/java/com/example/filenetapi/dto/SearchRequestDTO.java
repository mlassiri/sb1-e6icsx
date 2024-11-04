package com.example.filenetapi.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SearchRequestDTO {
    private String sqlQuery;
    private int maxResults = 100;
    private int offset = 0;
    private Map<String, Object> parameters;
    private String[] propertiesToInclude;
    private String orderBy;
    private boolean ascending = true;
}