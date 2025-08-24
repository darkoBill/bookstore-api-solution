package com.bookstore.integration;

import com.bookstore.dto.BookDto;
import com.bookstore.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
            "CONCUR-ISBN",
            Set.of(),
            Set.of(),
            null,
            null
        );

        BookDto created = bookService.createBook(initial);
        UUID bookId = created.id();

        Callable<Void> task1 = () -> {
            BookDto update1 = new BookDto(
                bookId,
                "Title A",
                new BigDecimal("20.00"),
                2024,
                "CONCUR-ISBN",
                Set.of(),
                Set.of(),
                null,
                null
            );
            bookService.updateBook(bookId, update1);
            return null;
        };

        Callable<Void> task2 = () -> {
            BookDto update2 = new BookDto(
                bookId,
                "Title B",
                new BigDecimal("30.00"),
                2024,
                "CONCUR-ISBN",
                Set.of(),
                Set.of(),
                null,
                null
            );
            bookService.updateBook(bookId, update2);
            return null;
        };

        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<Void>> futures = executor.invokeAll(List.of(task1, task2));
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                fail("Unexpected exception during concurrent update", e.getCause());
            }
        }

        BookDto result = bookService.getBook(bookId);

        boolean firstUpdate = result.title().equals("Title A") && result.price().compareTo(new BigDecimal("20.00")) == 0;
        boolean secondUpdate = result.title().equals("Title B") && result.price().compareTo(new BigDecimal("30.00")) == 0;

        assertTrue(firstUpdate || secondUpdate, "Final state should match one of concurrent updates");
    }
}