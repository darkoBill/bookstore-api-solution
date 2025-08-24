package com.bookstore.unit.mapper;

import com.bookstore.domain.Author;
import com.bookstore.domain.Book;
import com.bookstore.domain.Genre;
import com.bookstore.dto.AuthorDto;
import com.bookstore.dto.BookDto;
import com.bookstore.dto.GenreDto;
import com.bookstore.mapper.BookMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BookMapperTest {

    private final BookMapper bookMapper = Mappers.getMapper(BookMapper.class);

    @Test
    void toDto_shouldMapAllFields() {
        UUID bookId = UUID.randomUUID();
        Instant now = Instant.now();

        Author author1 = Author.builder().name("Author One").build();
        author1.setId(UUID.randomUUID());
        author1.setCreatedAt(now);
        author1.setUpdatedAt(now);

        Author author2 = Author.builder().name("Author Two").build();
        author2.setId(UUID.randomUUID());
        author2.setCreatedAt(now);
        author2.setUpdatedAt(now);

        Genre genre1 = Genre.builder().name("Fiction").build();
        genre1.setId(UUID.randomUUID());
        genre1.setCreatedAt(now);
        genre1.setUpdatedAt(now);

        Genre genre2 = Genre.builder().name("Science").build();
        genre2.setId(UUID.randomUUID());
        genre2.setCreatedAt(now);
        genre2.setUpdatedAt(now);

        Book book = Book.builder()
            .title("Test Book")
            .price(new BigDecimal("19.99"))
            .publishedYear(2023)
            .isbn("1234567890")
            .authors(Set.of(author1, author2))
            .genres(Set.of(genre1, genre2))
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

        assertThat(dto.authors()).hasSize(2);
        assertThat(dto.authors()).extracting(AuthorDto::name)
            .containsExactlyInAnyOrder("Author One", "Author Two");

        assertThat(dto.genres()).hasSize(2);
        assertThat(dto.genres()).extracting(GenreDto::name)
            .containsExactlyInAnyOrder("Fiction", "Science");
    }
    
    @Test
    void toEntity_shouldMapBasicFields() {
        Instant now = Instant.now();

        AuthorDto authorDto = new AuthorDto(UUID.randomUUID(), "Author One", now, now);
        GenreDto genreDto = new GenreDto(UUID.randomUUID(), "Fiction", now, now);

        BookDto dto = new BookDto(
            UUID.randomUUID(),
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
        assertThat(book.getId()).isNull();
        assertThat(book.getCreatedAt()).isNull();
        assertThat(book.getUpdatedAt()).isNull();
        assertThat(book.getTitle()).isEqualTo("Test Book");
        assertThat(book.getPrice()).isEqualTo(new BigDecimal("19.99"));
        assertThat(book.getPublishedYear()).isEqualTo(2023);
        assertThat(book.getIsbn()).isEqualTo("1234567890");
        assertThat(book.getAuthors()).isEmpty();
        assertThat(book.getGenres()).isEmpty();
    }
}