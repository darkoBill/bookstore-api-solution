package com.bookstore.service;

import com.bookstore.dto.BookDto;
import com.bookstore.service.InventoryService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MetricsService {
    
    private final MeterRegistry meterRegistry;
    private final BookService bookService;
    private final InventoryService inventoryService;
    
    private final Counter bookCreatedCounter;
    private final Counter bookViewedCounter;
    private final Counter inventoryReservedCounter;
    
    public MetricsService(MeterRegistry meterRegistry, BookService bookService, InventoryService inventoryService) {
        this.meterRegistry = meterRegistry;
        this.bookService = bookService;
        this.inventoryService = inventoryService;
        
        // Initialize counters
        this.bookCreatedCounter = Counter.builder("books.created")
            .description("Number of books created")
            .register(meterRegistry);
        
        this.bookViewedCounter = Counter.builder("books.viewed")
            .description("Number of book views")
            .register(meterRegistry);
            
        this.inventoryReservedCounter = Counter.builder("inventory.reserved")
            .description("Number of inventory reservations")
            .register(meterRegistry);
        
        // Register custom gauges
        registerInventoryGauges();
    }
    
    public void recordBookCreated(String genre) {
        Counter.builder("books.created")
            .tag("genre", genre)
            .register(meterRegistry)
            .increment();
        log.debug("Recorded book creation metric for genre: {}", genre);
    }
    
    public void recordBookViewed(String genre) {
        // Use low-cardinality tags to avoid metric explosion
        Counter.builder("books.viewed")
            .tag("genre", genre != null ? genre : "unknown")
            .register(meterRegistry)
            .increment();
        log.debug("Recorded book view metric for genre: {}", genre);
    }
    
    public void recordInventoryReserved(String quantityRange) {
        // Use quantity ranges instead of exact values to reduce cardinality
        Counter.builder("inventory.reserved")
            .tag("quantity_range", quantityRange != null ? quantityRange : "unknown")
            .register(meterRegistry)
            .increment();
        log.debug("Recorded inventory reservation metric for quantity range: {}", quantityRange);
    }
    
    public String getQuantityRange(int quantity) {
        if (quantity <= 5) return "1-5";
        if (quantity <= 10) return "6-10";
        if (quantity <= 25) return "11-25";
        if (quantity <= 50) return "26-50";
        return "50+";
    }
    
    private void registerInventoryGauges() {
        // Total books gauge - simplified for compilation
        Gauge.builder("inventory.total.books", this, s -> s.getTotalBooksCount())
            .description("Total number of books in inventory")  
            .register(meterRegistry);
            
        // Books needing restock gauge
        Gauge.builder("inventory.restock.needed", this, s -> s.getRestockNeededCount())
            .description("Number of books needing restock")
            .register(meterRegistry);
    }
    
    private double getTotalBooksCount() {
        try {
            return bookService.searchBooks(null, null, null, 
                org.springframework.data.domain.PageRequest.of(0, 1))
                .getTotalElements();
        } catch (Exception e) {
            log.warn("Failed to get total books count for metrics", e);
            return 0;
        }
    }
    
    private double getRestockNeededCount() {
        try {
            return inventoryService.getBooksNeedingRestock().size();
        } catch (Exception e) {
            log.warn("Failed to get restock count for metrics", e);
            return 0;
        }
    }
}