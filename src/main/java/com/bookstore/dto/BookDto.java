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
    
    @PositiveOrZero(message = "Quantity in stock must be zero or positive")
    Integer quantityInStock,
    
    @PositiveOrZero(message = "Reserved quantity must be zero or positive") 
    Integer reservedQuantity,
    
    @PositiveOrZero(message = "Cost price must be zero or positive")
    @Digits(integer = 10, fraction = 2, message = "Cost price format invalid")
    BigDecimal costPrice,
    
    @Size(max = 500, message = "Supplier info must not exceed 500 characters")
    String supplierInfo,
    
    @PositiveOrZero(message = "Reorder level must be zero or positive")
    Integer reorderLevel,
    
    Long viewCount,
    Long version,
    
    Instant createdAt,
    Instant updatedAt
) {
    // Computed properties for business logic
    public Integer getAvailableQuantity() {
        if (quantityInStock == null || reservedQuantity == null) {
            return 0;
        }
        return Math.max(0, quantityInStock - reservedQuantity);
    }
    
    public boolean isAvailable() {
        return getAvailableQuantity() > 0;
    }
    
    public boolean needsRestock() {
        if (reorderLevel == null) {
            return false;
        }
        return getAvailableQuantity() <= reorderLevel;
    }
    
    public BigDecimal getMargin() {
        if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return price.subtract(costPrice);
    }
}