package com.bookstore.repository;

import com.bookstore.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID>, JpaSpecificationExecutor<Book> {
    
    @Query("SELECT DISTINCT b FROM Book b " +
           "LEFT JOIN FETCH b.authors " +
           "LEFT JOIN FETCH b.genres " +
           "WHERE b.id = :id")
    Optional<Book> findByIdWithRelations(UUID id);
    
    Optional<Book> findByIsbn(String isbn);
}