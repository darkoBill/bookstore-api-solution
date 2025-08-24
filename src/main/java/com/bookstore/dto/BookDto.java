package com.bookstore.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record BookDto(
    UUID id,
    
    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    String title,
    
    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be zero or positive")
    @Digits(integer = 10, fraction = 2, message = "Price format invalid")
    BigDecimal price,
    
    @Min(value = 1450, message = "Published year must be after 1450")
    @Max(value = 2100, message = "Published year must be before 2100")
    Integer publishedYear,
    
    @Size(max = 20, message = "ISBN must not exceed 20 characters")
    String isbn,
    
    Set<AuthorDto> authors,
    Set<GenreDto> genres,
    
    Instant createdAt,
    Instant updatedAt
) {}