package com.bookstore.integration;

import com.bookstore.domain.Book;
import com.bookstore.dto.InventoryAdjustmentDto;
import com.bookstore.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class InventoryControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void adjustInventory_NegativeAdjustment_ShouldReturn400() throws Exception {
        Book book = Book.builder()
                .title("Test Book")
                .price(new BigDecimal("9.99"))
                .quantityInStock(5)
                .build();
        book = bookRepository.save(book);

        InventoryAdjustmentDto adjustment = new InventoryAdjustmentDto(
                book.getId(),
                -10,
                InventoryAdjustmentDto.AdjustmentType.MANUAL_ADJUSTMENT,
                "Damaged",
                Instant.now()
        );

        mockMvc.perform(post("/api/inventory/{bookId}/adjust", book.getId())
                .with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adjustment)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(containsString("invalid-inventory-adjustment")))
            .andExpect(jsonPath("$.title").value("Invalid Inventory Adjustment"));
    }

    @Test
    void bulkInventoryUpdate_NegativeAdjustment_ShouldReturn400() throws Exception {
        Book book = Book.builder()
                .title("Bulk Book")
                .price(new BigDecimal("9.99"))
                .quantityInStock(5)
                .build();
        book = bookRepository.save(book);

        InventoryAdjustmentDto adjustment = new InventoryAdjustmentDto(
                book.getId(),
                -10,
                InventoryAdjustmentDto.AdjustmentType.MANUAL_ADJUSTMENT,
                "Damaged",
                Instant.now()
        );

        mockMvc.perform(post("/api/inventory/bulk-adjust")
                .with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(adjustment))))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(containsString("invalid-inventory-adjustment")))
            .andExpect(jsonPath("$.title").value("Invalid Inventory Adjustment"));
    }
}
