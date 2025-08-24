package com.bookstore.unit.service;

import com.bookstore.domain.Author;
import com.bookstore.domain.Book;
import com.bookstore.domain.Genre;
import com.bookstore.dto.AuthorDto;
import com.bookstore.dto.BookDto;
import com.bookstore.dto.GenreDto;
import com.bookstore.exception.DuplicateResourceException;
import com.bookstore.exception.IdMismatchException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.BookMapper;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.GenreRepository;
import com.bookstore.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {
    
    @Mock
    private BookRepository bookRepository;
    
    @Mock
    private AuthorRepository authorRepository;
    
    @Mock
    private GenreRepository genreRepository;
    
    @Mock
    private BookMapper bookMapper;
    
    @InjectMocks
    private BookServiceImpl bookService;
    
    private Book book;
    private BookDto bookDto;
    private Author author;
    private Genre genre;
    private UUID bookId;
    
    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();
        
        author = Author.builder()
            .name("Test Author")
            .build();
        author.setId(UUID.randomUUID());
        
        genre = Genre.builder()
            .name("Test Genre")
            .build();
        genre.setId(UUID.randomUUID());
        
        book = Book.builder()
            .title("Test Book")
            .price(new BigDecimal("19.99"))
            .publishedYear(2023)
            .isbn("1234567890")
            .authors(Set.of(author))
            .genres(Set.of(genre))
            .build();
        book.setId(bookId);
        
        AuthorDto authorDto = new AuthorDto(author.getId(), author.getName(), 
            Instant.now(), Instant.now());
        GenreDto genreDto = new GenreDto(genre.getId(), genre.getName(), 
            Instant.now(), Instant.now());
        
        bookDto = new BookDto(
            bookId,
            "Test Book",
            new BigDecimal("19.99"),
            2023,
            "1234567890",
            Set.of(authorDto),
            Set.of(genreDto),
            Instant.now(),
            Instant.now()
        );
    }
    
    @Test
    void createBook_ShouldCreateSuccessfully() {
        when(bookMapper.toEntity(bookDto)).thenReturn(book);
        when(authorRepository.findByIdIn(any())).thenReturn(Set.of(author));
        when(genreRepository.findByIdIn(any())).thenReturn(Set.of(genre));
        when(bookRepository.save(any())).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookDto);
        
        BookDto result = bookService.createBook(bookDto);
        
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Test Book");
        verify(bookRepository).save(any());
    }
    
    @Test
    void createBook_WithDuplicateIsbn_ShouldThrowException() {
        when(bookRepository.findByIsbn("1234567890")).thenReturn(Optional.of(book));
        
        assertThatThrownBy(() -> bookService.createBook(bookDto))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("ISBN");
    }
    
    @Test
    void getBook_WhenExists_ShouldReturnBook() {
        when(bookRepository.findByIdWithRelations(bookId)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookDto);
        
        BookDto result = bookService.getBook(bookId);
        
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(bookId);
    }
    
    @Test
    void getBook_WhenNotExists_ShouldThrowException() {
        when(bookRepository.findByIdWithRelations(bookId)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> bookService.getBook(bookId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Book");
    }
    
    @Test
    void updateBook_WithMismatchedIds_ShouldThrowException() {
        UUID pathId = UUID.randomUUID();
        
        assertThatThrownBy(() -> bookService.updateBook(pathId, bookDto))
            .isInstanceOf(IdMismatchException.class);
    }
    
    @Test
    void updateBook_ShouldUpdateSuccessfully() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(authorRepository.findByIdIn(any())).thenReturn(Set.of(author));
        when(genreRepository.findByIdIn(any())).thenReturn(Set.of(genre));
        when(bookRepository.save(any())).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookDto);
        
        BookDto result = bookService.updateBook(bookId, bookDto);
        
        assertThat(result).isNotNull();
        verify(bookMapper).updateEntity(bookDto, book);
        verify(bookRepository).save(book);
    }
    
    @Test
    void deleteBook_WhenExists_ShouldDelete() {
        when(bookRepository.existsById(bookId)).thenReturn(true);
        
        bookService.deleteBook(bookId);
        
        verify(bookRepository).deleteById(bookId);
    }
    
    @Test
    void deleteBook_WhenNotExists_ShouldNotThrow() {
        when(bookRepository.existsById(bookId)).thenReturn(false);
        
        bookService.deleteBook(bookId);
        
        verify(bookRepository, never()).deleteById(any());
    }
    
    @Test
    void searchBooks_ShouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Book> bookPage = new PageImpl<>(List.of(book), pageable, 1);
        
        when(bookRepository.findAll(any(Specification.class), eq(pageable)))
            .thenReturn(bookPage);
        when(bookMapper.toDto(book)).thenReturn(bookDto);
        
        Page<BookDto> result = bookService.searchBooks("Test", null, null, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Test Book");
    }
}