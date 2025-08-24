package com.bookstore.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true, exclude = {"authors", "genres"})
public class Book extends BaseEntity {
    
    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String title;
    
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
    
    @Column(name = "published_year")
    private Integer publishedYear;
    
    @Size(max = 20)
    @Column(length = 20)
    private String isbn;
    
    @PositiveOrZero
    @Column(name = "quantity_in_stock", nullable = false)
    @Builder.Default
    private Integer quantityInStock = 0;
    
    @PositiveOrZero
    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;
    
    @PositiveOrZero
    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;
    
    @Column(name = "supplier_info", length = 500)
    private String supplierInfo;
    
    @Column(name = "reorder_level")
    @Builder.Default
    private Integer reorderLevel = 5;
    
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_authors",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    @Builder.Default
    private Set<Author> authors = new HashSet<>();
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_genres",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();
    
    public boolean isAvailable() {
        return getAvailableQuantity() > 0;
    }
    
    public Integer getAvailableQuantity() {
        return Math.max(0, quantityInStock - reservedQuantity);
    }
    
    public boolean needsRestock() {
        return getAvailableQuantity() <= reorderLevel;
    }
    
    public BigDecimal getMargin() {
        if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return price.subtract(costPrice);
    }
    
    public BigDecimal getMarginPercent() {
        if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return getMargin().divide(costPrice, 4, java.math.RoundingMode.HALF_UP)
               .multiply(new BigDecimal("100"));
    }
    
    public void incrementViewCount() {
        this.viewCount++;
    }
}