package com.bookstore.integration;

import com.bookstore.dto.AuthorDto;
import com.bookstore.dto.BookDto;
import com.bookstore.dto.GenreDto;
import com.bookstore.repository.BookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class BookControllerIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private BookRepository bookRepository;
    
    private BookDto validBookDto;
    
    @BeforeEach
    void setUp() {
        AuthorDto author = new AuthorDto(null, "Integration Author", null, null);
        GenreDto genre = new GenreDto(null, "Integration Genre", null, null);
        
        validBookDto = new BookDto(
            null,
            "Integration Test Book",
            new BigDecimal("29.99"),
            2023,
            "978-1234567890",
            Set.of(author),
            Set.of(genre),
            null,
            null
        );
    }
    
    @Test
    void createBook_AsAdmin_ShouldReturn201() throws Exception {
        mockMvc.perform(post("/api/books")
                .with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validBookDto)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.data.title").value("Integration Test Book"))
            .andExpect(jsonPath("$.data.price").value(29.99))
            .andExpect(jsonPath("$.data.id").isNotEmpty());
    }
    
    @Test
    void createBook_AsUser_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/books")
                .with(httpBasic("user", "user123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validBookDto)))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }
    
    @Test
    void createBook_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validBookDto)))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    void createBook_InvalidData_ShouldReturn400() throws Exception {
        BookDto invalidBook = new BookDto(
            null,
            "", // Invalid: blank title
            new BigDecimal("-10"), // Invalid: negative price
            1000, // Invalid: year too early
            null,
            null,
            null,
            null,
            null
        );
        
        mockMvc.perform(post("/api/books")
                .with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBook)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(containsString("validation-error")))
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors[*].field", hasItems("title", "price", "publishedYear")));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void getBook_WhenExists_ShouldReturn200() throws Exception {
        BookDto created = createTestBook();
        
        mockMvc.perform(get("/api/books/{id}", created.id())
                .with(httpBasic("admin", "admin123")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(created.id().toString()))
            .andExpect(jsonPath("$.data.title").value("Integration Test Book"));
    }
    
    @Test
    void getBook_WhenNotExists_ShouldReturn404() throws Exception {
        UUID randomId = UUID.randomUUID();
        
        mockMvc.perform(get("/api/books/{id}", randomId)
                .with(httpBasic("user", "user123")))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }
    
    @Test
    void updateBook_AsAdmin_ShouldReturn200() throws Exception {
        BookDto created = createTestBook();
        
        BookDto updateDto = new BookDto(
            created.id(),
            "Updated Title",
            new BigDecimal("39.99"),
            2024,
            "978-0987654321",
            created.authors(),
            created.genres(),
            null,
            null
        );
        
        mockMvc.perform(put("/api/books/{id}", created.id())
                .with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("Updated Title"))
            .andExpect(jsonPath("$.data.price").value(39.99));
    }
    
    @Test
    void updateBook_IdMismatch_ShouldReturn400() throws Exception {
        BookDto created = createTestBook();
        UUID differentId = UUID.randomUUID();
        
        BookDto updateDto = new BookDto(
            differentId, // Different ID
            "Updated Title",
            new BigDecimal("39.99"),
            2024,
            null,
            null,
            null,
            null,
            null
        );
        
        mockMvc.perform(put("/api/books/{id}", created.id())
                .with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(containsString("id-mismatch")));
    }
    
    @Test
    void deleteBook_AsAdmin_ShouldReturn204() throws Exception {
        BookDto created = createTestBook();
        
        mockMvc.perform(delete("/api/books/{id}", created.id())
                .with(httpBasic("admin", "admin123")))
            .andExpect(status().isNoContent());
        
        // Verify idempotency
        mockMvc.perform(delete("/api/books/{id}", created.id())
                .with(httpBasic("admin", "admin123")))
            .andExpect(status().isNoContent());
    }
    
    @Test
    void deleteBook_AsUser_ShouldReturn403() throws Exception {
        BookDto created = createTestBook();
        
        mockMvc.perform(delete("/api/books/{id}", created.id())
                .with(httpBasic("user", "user123")))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void searchBooks_WithFilters_ShouldReturnFilteredResults() throws Exception {
        createTestBook();
        
        mockMvc.perform(get("/api/books")
                .with(httpBasic("user", "user123"))
                .param("title", "Integration")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].title").value(containsString("Integration")))
            .andExpect(jsonPath("$.meta.page").value(0))
            .andExpect(jsonPath("$.meta.size").value(10))
            .andExpect(jsonPath("$.meta.total").value(greaterThanOrEqualTo(1)));
    }
    
    @Test
    void searchBooks_WithInvalidSort_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/books")
                .with(httpBasic("user", "user123"))
                .param("sort", "invalidField,asc"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(containsString("invalid-sort")));
    }
    
    @Test
    void searchBooks_ExceedingMaxSize_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/books")
                .with(httpBasic("user", "user123"))
                .param("size", "101"))
            .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("searchParamNames")
    void searchBooks_WithMaxLengthFilters_ShouldReturn200(String paramName) throws Exception {
        String value = "a".repeat(255);
        mockMvc.perform(get("/api/books")
                .with(httpBasic("user", "user123"))
                .param(paramName, value))
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("searchParamNames")
    void searchBooks_WithTooLongFilters_ShouldReturn400(String paramName) throws Exception {
        String value = "a".repeat(256);
        mockMvc.perform(get("/api/books")
                .with(httpBasic("user", "user123"))
                .param(paramName, value))
            .andExpect(status().isBadRequest());
    }

    private static Stream<String> searchParamNames() {
        return Stream.of("title", "author", "genre");
    }
    
    private BookDto createTestBook() throws Exception {
        String response = mockMvc.perform(post("/api/books")
                .with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validBookDto)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        JsonNode dataNode = objectMapper.readTree(response).at("/data");
        return objectMapper.treeToValue(dataNode, BookDto.class);
    }
}