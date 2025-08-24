package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.BookDto;
import com.bookstore.dto.PageMeta;
import com.bookstore.service.BookService;
import com.bookstore.util.SortValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Validated
@Tag(name = "Books", description = "Book management endpoints")
@SecurityRequirement(name = "basicAuth")
public class BookController {
    
    // Service layer handles business logic and data access
    private final BookService bookService;
    // Custom validator for sort parameters to prevent injection attacks
    private final SortValidator sortValidator;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new book")
    public ResponseEntity<ApiResponse<BookDto>> createBook(@Valid @RequestBody BookDto bookDto) {
        BookDto created = bookService.createBook(bookDto);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();
        
        return ResponseEntity.created(location).body(ApiResponse.of(created));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get book by ID")
    public ResponseEntity<ApiResponse<BookDto>> getBook(@PathVariable UUID id) {
        BookDto book = bookService.getBook(id);
        return ResponseEntity.ok(ApiResponse.of(book));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing book")
    public ResponseEntity<ApiResponse<BookDto>> updateBook(
            @PathVariable UUID id,
            @Valid @RequestBody BookDto bookDto) {
        BookDto updated = bookService.updateBook(id, bookDto);
        return ResponseEntity.ok(ApiResponse.of(updated));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a book")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Search and list books")
    public ResponseEntity<ApiResponse<List<BookDto>>> searchBooks(
            @RequestParam(required = false) @Size(max = 255) String title,
            @RequestParam(required = false) @Size(max = 255) String author,
            @RequestParam(required = false) @Size(max = 255) String genre,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "title,asc") String sort) {
        
        Sort sortObj = sortValidator.validateAndParse(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        Page<BookDto> result = bookService.searchBooks(title, author, genre, pageable);
        
        PageMeta meta = PageMeta.of(page, size, result.getTotalElements());
        
        return ResponseEntity.ok(ApiResponse.of(result.getContent(), meta));
    }
}