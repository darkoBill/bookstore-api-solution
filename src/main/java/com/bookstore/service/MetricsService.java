package com.bookstore.service;

import com.bookstore.dto.BookDto;
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
    
    private final Counter bookCreatedCounter;
    private final Counter bookViewedCounter;
    private final Counter inventoryReservedCounter;
    
    public MetricsService(MeterRegistry meterRegistry, BookService bookService) {
        this.meterRegistry = meterRegistry;
        this.bookService = bookService;
        
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
    
    public void recordBookViewed(String bookId, String title) {
        Counter.builder("books.viewed")
            .tag("book_id", bookId)
            .tag("title", title)
            .register(meterRegistry)
            .increment();
        log.debug("Recorded book view metric for: {}", title);
    }
    
    public void recordInventoryReserved(String bookId, int quantity) {
        Counter.builder("inventory.reserved")
            .tag("book_id", bookId)
            .tag("quantity", String.valueOf(quantity))
            .register(meterRegistry)
            .increment();
        log.debug("Recorded inventory reservation metric: {} units for book {}", quantity, bookId);
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
            return 0; // Placeholder - would call inventory service
        } catch (Exception e) {
            log.warn("Failed to get restock count for metrics", e);
            return 0;
        }
    }
}