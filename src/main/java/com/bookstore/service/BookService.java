package com.bookstore.service;

import com.bookstore.dto.BookDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BookService {
    
    BookDto createBook(BookDto bookDto);
    
    BookDto getBook(UUID id);
    
    BookDto updateBook(UUID id, BookDto bookDto);
    
    void deleteBook(UUID id);
    
    Page<BookDto> searchBooks(String title, String author, String genre, Pageable pageable);
}