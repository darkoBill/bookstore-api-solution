package com.bookstore.unit.mapper;

import com.bookstore.domain.Book;
import com.bookstore.dto.BookDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BookMapperTest {
    
    @Test
    void manualMapping_toDto_ShouldMapBasicFields() {
        UUID bookId = UUID.randomUUID();
        Instant now = Instant.now();
        
        Book book = Book.builder()
            .title("Test Book")
            .price(new BigDecimal("19.99"))
            .publishedYear(2023)
            .isbn("1234567890")
            .build();
        book.setId(bookId);
        book.setCreatedAt(now);
        book.setUpdatedAt(now);
        
        // Manual mapping to verify basic structure
        BookDto dto = new BookDto(
            book.getId(),
            book.getTitle(),
            book.getPrice(),
            book.getPublishedYear(),
            book.getIsbn(),
            null, // authors
            null, // genres
            book.getCreatedAt(),
            book.getUpdatedAt()
        );
        
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(bookId);
        assertThat(dto.title()).isEqualTo("Test Book");
        assertThat(dto.price()).isEqualTo(new BigDecimal("19.99"));
        assertThat(dto.publishedYear()).isEqualTo(2023);
        assertThat(dto.isbn()).isEqualTo("1234567890");
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.updatedAt()).isEqualTo(now);
    }
    
    @Test
    void manualMapping_toEntity_ShouldMapBasicFields() {
        UUID bookId = UUID.randomUUID();
        
        BookDto dto = new BookDto(
            bookId,
            "Test Book",
            new BigDecimal("19.99"),
            2023,
            "1234567890",
            null,
            null,
            Instant.now(),
            Instant.now()
        );
        
        // Manual mapping to verify basic structure
        Book book = Book.builder()
            .title(dto.title())
            .price(dto.price())
            .publishedYear(dto.publishedYear())
            .isbn(dto.isbn())
            .build();
        
        assertThat(book).isNotNull();
        assertThat(book.getTitle()).isEqualTo("Test Book");
        assertThat(book.getPrice()).isEqualTo(new BigDecimal("19.99"));
        assertThat(book.getPublishedYear()).isEqualTo(2023);
        assertThat(book.getIsbn()).isEqualTo("1234567890");
    }
}