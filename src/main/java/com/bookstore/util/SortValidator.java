package com.bookstore.util;

import com.bookstore.exception.InvalidSortParameterException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
public class SortValidator {
    
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "title", "price", "publishedYear"
    );
    
    private static final Set<String> ALLOWED_DIRECTIONS = Set.of(
        "asc", "desc"
    );
    
    public Sort validateAndParse(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "title");
        }
        
        List<String> parts = Arrays.asList(sortParam.split(","));
        
        if (parts.size() != 2) {
            throw new InvalidSortParameterException("Sort parameter must be in format: field,direction");
        }
        
        String field = parts.get(0).trim().toLowerCase();
        String direction = parts.get(1).trim().toLowerCase();
        
        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            throw new InvalidSortParameterException("Invalid sort field: " + field + 
                ". Allowed fields: " + ALLOWED_SORT_FIELDS);
        }
        
        if (!ALLOWED_DIRECTIONS.contains(direction)) {
            throw new InvalidSortParameterException("Invalid sort direction: " + direction + 
                ". Allowed directions: " + ALLOWED_DIRECTIONS);
        }
        
        Sort.Direction sortDirection = "desc".equals(direction) ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        
        return Sort.by(sortDirection, field);
    }
}