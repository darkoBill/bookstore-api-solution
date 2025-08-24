package com.bookstore.util;

import com.bookstore.dto.AuthorDto;
import com.bookstore.dto.BookDto;
import com.bookstore.dto.GenreDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class TestDataUtil {
    
    public static BookDto createBookDto(
            UUID id,
            String title, 
            BigDecimal price,
            Integer publishedYear,
            String isbn,
            Set<AuthorDto> authors,
            Set<GenreDto> genres) {
        
        return new BookDto(
            id,
            title,
            price,
            publishedYear,
            isbn,
            authors,
            genres,
            10,      // quantityInStock
            0,       // reservedQuantity
            price.multiply(new BigDecimal("0.8")), // costPrice (80% of selling price)
            "Test Supplier",
            5,       // reorderLevel
            0L,      // viewCount
            0L,      // version
            Instant.now(),
            Instant.now()
        );
    }
    
    public static BookDto createMinimalBookDto(String title, BigDecimal price) {
        return createBookDto(
            null,
            title,
            price,
            2023,
            null,
            Set.of(),
            Set.of()
        );
    }
}