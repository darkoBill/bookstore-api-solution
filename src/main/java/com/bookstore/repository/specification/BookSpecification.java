package com.bookstore.repository.specification;

import com.bookstore.domain.Author;
import com.bookstore.domain.Book;
import com.bookstore.domain.Genre;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {
    
    public static Specification<Book> titleContains(String title) {
        return (root, query, cb) -> {
            if (title == null || title.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("title")), 
                          "%" + title.toLowerCase() + "%");
        };
    }
    
    public static Specification<Book> hasAuthor(String authorName) {
        return (root, query, cb) -> {
            if (authorName == null || authorName.isBlank()) {
                return cb.conjunction();
            }
            
            Join<Book, Author> authorJoin = root.join("authors", JoinType.LEFT);
            query.distinct(true);
            
            return cb.like(cb.lower(authorJoin.get("name")), 
                          "%" + authorName.toLowerCase() + "%");
        };
    }
    
    public static Specification<Book> hasGenre(String genreName) {
        return (root, query, cb) -> {
            if (genreName == null || genreName.isBlank()) {
                return cb.conjunction();
            }
            
            Join<Book, Genre> genreJoin = root.join("genres", JoinType.LEFT);
            query.distinct(true);
            
            return cb.like(cb.lower(genreJoin.get("name")), 
                          "%" + genreName.toLowerCase() + "%");
        };
    }
    
    public static Specification<Book> withFilters(String title, String author, String genre) {
        return Specification.where(titleContains(title))
                           .and(hasAuthor(author))
                           .and(hasGenre(genre));
    }
}