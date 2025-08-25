package com.bookstore.integration;

import com.bookstore.dto.BookDto;
import com.bookstore.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("basic")
class BookConcurrentUpdateTest extends BaseIntegrationTest {

    @Autowired
    private BookService bookService;

    @Test
    void concurrentUpdatesShouldNotCauseDuplicatesOrLostUpdates() throws Exception {
        BookDto initial = new BookDto(
            null,
            "Concurrent Book",
            new BigDecimal("10.00"),
            2024,
            "978-0123456789",
            Set.of(),
            Set.of(),
            100,
            0,
            new BigDecimal("8.00"),
            "Test Supplier",
            10,
            0L,
            null,
            null,
            null
        );

        BookDto created = bookService.createBook(initial);
        UUID bookId = created.id();

        Callable<String> task1 = () -> {
            try {
                BookDto current = bookService.getBook(bookId);
                BookDto update1 = new BookDto(
                    bookId,
                    "Title A",
                    new BigDecimal("20.00"),
                    2024,
                    "978-0123456789",
                    Set.of(),
                    Set.of(),
                    120,
                    5,
                    new BigDecimal("15.00"),
                    "Supplier A",
                    12,
                    current.viewCount(),
                    current.version(),
                    current.createdAt(),
                    current.updatedAt()
                );
                bookService.updateBook(bookId, update1);
                return "A";
            } catch (Exception e) {
                return "A_FAILED: " + e.getMessage();
            }
        };

        Callable<String> task2 = () -> {
            try {
                BookDto current = bookService.getBook(bookId);
                BookDto update2 = new BookDto(
                    bookId,
                    "Title B",
                    new BigDecimal("30.00"),
                    2024,
                    "978-0123456789",
                    Set.of(),
                    Set.of(),
                    80,
                    10,
                    new BigDecimal("22.00"),
                    "Supplier B",
                    8,
                    current.viewCount(),
                    current.version(),
                    current.createdAt(),
                    current.updatedAt()
                );
                bookService.updateBook(bookId, update2);
                return "B";
            } catch (Exception e) {
                return "B_FAILED: " + e.getMessage();
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<String>> futures = executor.invokeAll(List.of(task1, task2));
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        String result1 = null;
        String result2 = null;
        
        try {
            result1 = futures.get(0).get();
            result2 = futures.get(1).get();
        } catch (ExecutionException e) {
            fail("Unexpected exception during concurrent update", e.getCause());
        }

        // At least one should succeed, one might fail due to optimistic locking
        boolean hasSuccess = !result1.contains("_FAILED") || !result2.contains("_FAILED");
        assertTrue(hasSuccess, "At least one concurrent update should succeed. Results: " + result1 + ", " + result2);

        BookDto result = bookService.getBook(bookId);

        boolean firstUpdate = result.title().equals("Title A") && result.price().compareTo(new BigDecimal("20.00")) == 0;
        boolean secondUpdate = result.title().equals("Title B") && result.price().compareTo(new BigDecimal("30.00")) == 0;

        assertTrue(firstUpdate || secondUpdate, "Final state should match one of concurrent updates");
    }
}