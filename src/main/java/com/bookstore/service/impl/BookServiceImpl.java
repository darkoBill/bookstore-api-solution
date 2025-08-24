package com.bookstore.service.impl;

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
import com.bookstore.repository.specification.BookSpecification;
import com.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.annotation.Timed;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {
    
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final BookMapper bookMapper;
    
    @Override
    @Transactional
    @Timed(value = "book.creation", description = "Time spent creating books")
    public BookDto createBook(BookDto bookDto) {
        validateIsbn(bookDto.isbn(), null);
        
        Book book = bookMapper.toEntity(bookDto);
        
        if (bookDto.authors() != null && !bookDto.authors().isEmpty()) {
            Set<Author> authors = processAuthors(bookDto.authors());
            book.setAuthors(authors);
        }
        
        if (bookDto.genres() != null && !bookDto.genres().isEmpty()) {
            Set<Genre> genres = processGenres(bookDto.genres());
            book.setGenres(genres);
        }
        
        Book savedBook = bookRepository.save(book);
        log.debug("Created book with id: {}", savedBook.getId());
        
        return bookMapper.toDto(savedBook);
    }
    
    @Override
    @Transactional
    public BookDto getBook(UUID id) {
        Book book = bookRepository.findByIdWithRelations(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book", id));
        
        // Track view count for analytics
        bookRepository.incrementViewCount(id);
        
        return bookMapper.toDto(book);
    }
    
    @Override
    @Transactional
    public BookDto updateBook(UUID id, BookDto bookDto) {
        if (!id.equals(bookDto.id())) {
            throw new IdMismatchException(id, bookDto.id());
        }
        
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book", id));
        
        validateIsbn(bookDto.isbn(), book.getId());
        
        bookMapper.updateEntity(bookDto, book);
        
        if (bookDto.authors() != null) {
            Set<Author> authors = processAuthors(bookDto.authors());
            book.setAuthors(authors);
        }
        
        if (bookDto.genres() != null) {
            Set<Genre> genres = processGenres(bookDto.genres());
            book.setGenres(genres);
        }
        
        Book updatedBook = bookRepository.save(book);
        log.debug("Updated book with id: {}", updatedBook.getId());
        
        return bookMapper.toDto(updatedBook);
    }
    
    @Override
    @Transactional
    public void deleteBook(UUID id) {
        long deletedCount = bookRepository.deleteBookById(id);
        if (deletedCount > 0) {
            log.debug("Deleted book with id: {}", id);
        }
    }
    
    @Override
    @Timed(value = "book.search", description = "Time spent searching books")
    public Page<BookDto> searchBooks(String title, String author, String genre, Pageable pageable) {
        Specification<Book> spec = BookSpecification.withFilters(title, author, genre);
        
        Page<Book> books = bookRepository.findAll(spec, pageable);
        
        return books.map(bookMapper::toDto);
    }
    
    private Set<Author> processAuthors(Set<AuthorDto> authorDtos) {
        Set<Author> authors = new HashSet<>();

        // Batch fetch by IDs
        Set<UUID> ids = authorDtos.stream()
            .map(AuthorDto::id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        if (!ids.isEmpty()) {
            Set<Author> foundAuthors = authorRepository.findByIdIn(ids);
            if (foundAuthors.size() != ids.size()) {
                Set<UUID> foundIds = foundAuthors.stream()
                    .map(Author::getId)
                    .collect(Collectors.toSet());
                ids.removeAll(foundIds);
                throw new ResourceNotFoundException("Author", ids.iterator().next());
            }
            authors.addAll(foundAuthors);
        }

        // Process authors without IDs (by name)
        authorDtos.stream()
            .filter(dto -> dto.id() == null)
            .forEach(dto -> {
                Author author = authorRepository.findByNameIgnoreCase(dto.name())
                    .orElseGet(() -> {
                        Author newAuthor = new Author();
                        newAuthor.setName(dto.name());
                        return authorRepository.save(newAuthor);
                    });
                authors.add(author);
            });

        return authors;
    }
    
    private Set<Genre> processGenres(Set<GenreDto> genreDtos) {
        Set<Genre> genres = new HashSet<>();

        // Batch fetch by IDs
        Set<UUID> ids = genreDtos.stream()
            .map(GenreDto::id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        if (!ids.isEmpty()) {
            Set<Genre> foundGenres = genreRepository.findByIdIn(ids);
            if (foundGenres.size() != ids.size()) {
                Set<UUID> foundIds = foundGenres.stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
                ids.removeAll(foundIds);
                throw new ResourceNotFoundException("Genre", ids.iterator().next());
            }
            genres.addAll(foundGenres);
        }

        // Process genres without IDs (by name)
        genreDtos.stream()
            .filter(dto -> dto.id() == null)
            .forEach(dto -> {
                Genre genre = genreRepository.findByNameIgnoreCase(dto.name())
                    .orElseGet(() -> {
                        Genre newGenre = new Genre();
                        newGenre.setName(dto.name());
                        return genreRepository.save(newGenre);
                    });
                genres.add(genre);
            });

        return genres;
    }
    
    private void validateIsbn(String isbn, UUID excludeBookId) {
        if (isbn != null && !isbn.isBlank()) {
            bookRepository.findByIsbn(isbn).ifPresent(existingBook -> {
                if (!existingBook.getId().equals(excludeBookId)) {
                    throw new DuplicateResourceException("Book with ISBN " + isbn + " already exists");
                }
            });
        }
    }
}