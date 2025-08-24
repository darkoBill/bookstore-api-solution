package com.bookstore.unit.mapper;

import com.bookstore.domain.Author;
import com.bookstore.domain.Book;
import com.bookstore.domain.Genre;
import com.bookstore.dto.AuthorDto;
import com.bookstore.dto.BookDto;
import com.bookstore.dto.GenreDto;
import com.bookstore.mapper.AuthorMapper;
import com.bookstore.mapper.BookMapper;
import com.bookstore.mapper.GenreMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BookMapperTest {

    private BookMapper bookMapper;

    @BeforeEach
    void setUp() throws Exception {
        // Create mappers using MapStruct factory
        bookMapper = Mappers.getMapper(BookMapper.class);
        AuthorMapper authorMapper = Mappers.getMapper(AuthorMapper.class);
        GenreMapper genreMapper = Mappers.getMapper(GenreMapper.class);

        // Use reflection to inject dependencies since we're not using Spring context
        try {
            Field authorField = bookMapper.getClass().getDeclaredField("authorMapper");
            authorField.setAccessible(true);
            authorField.set(bookMapper, authorMapper);

            Field genreField = bookMapper.getClass().getDeclaredField("genreMapper");
            genreField.setAccessible(true);
            genreField.set(bookMapper, genreMapper);
        } catch (NoSuchFieldException e) {
            // If fields don't exist, the mapper might be using different approach
            // This is expected as MapStruct might generate different implementations
        }
    }

    @Test
    void toDto_ShouldMapNestedAuthorAndGenre() {
        UUID bookId = UUID.randomUUID();
        Instant now = Instant.now();

        Author author = Author.builder().name("John Doe").build();
        author.setId(UUID.randomUUID());
        Genre genre = Genre.builder().name("Fiction").build();
        genre.setId(UUID.randomUUID());

        Book book = Book.builder()
            .title("Test Book")
            .price(new BigDecimal("19.99"))
            .publishedYear(2023)
            .isbn("1234567890")
            .authors(Set.of(author))
            .genres(Set.of(genre))
            .build();
        book.setId(bookId);
        book.setCreatedAt(now);
        book.setUpdatedAt(now);

        BookDto dto = bookMapper.toDto(book);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(bookId);
        assertThat(dto.title()).isEqualTo("Test Book");
        assertThat(dto.price()).isEqualTo(new BigDecimal("19.99"));
        assertThat(dto.publishedYear()).isEqualTo(2023);
        assertThat(dto.isbn()).isEqualTo("1234567890");
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.updatedAt()).isEqualTo(now);

        assertThat(dto.authors()).hasSize(1);
        AuthorDto authorDto = dto.authors().iterator().next();
        assertThat(authorDto.id()).isEqualTo(author.getId());
        assertThat(authorDto.name()).isEqualTo("John Doe");

        assertThat(dto.genres()).hasSize(1);
        GenreDto genreDto = dto.genres().iterator().next();
        assertThat(genreDto.id()).isEqualTo(genre.getId());
        assertThat(genreDto.name()).isEqualTo("Fiction");
    }
    
    @Test
    void toEntity_ShouldIgnoreNestedAuthorAndGenre() {
        UUID bookId = UUID.randomUUID();
        Instant now = Instant.now();

        AuthorDto authorDto = new AuthorDto(UUID.randomUUID(), "John Doe", now, now);
        GenreDto genreDto = new GenreDto(UUID.randomUUID(), "Fiction", now, now);

        BookDto dto = new BookDto(
            bookId,
            "Test Book",
            new BigDecimal("19.99"),
            2023,
            "1234567890",
            Set.of(authorDto),
            Set.of(genreDto),
            now,
            now
        );

        Book book = bookMapper.toEntity(dto);

        assertThat(book).isNotNull();
        assertThat(book.getTitle()).isEqualTo("Test Book");
        assertThat(book.getPrice()).isEqualTo(new BigDecimal("19.99"));
        assertThat(book.getPublishedYear()).isEqualTo(2023);
        assertThat(book.getIsbn()).isEqualTo("1234567890");
        assertThat(book.getAuthors()).isEmpty();
        assertThat(book.getGenres()).isEmpty();
    }
}