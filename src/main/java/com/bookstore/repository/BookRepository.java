package com.bookstore.repository;

import com.bookstore.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @EntityGraph(attributePaths = {"authors", "genres"})
    Page<Book> findAll(Specification<Book> spec, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Book b WHERE b.id = :id")
    long deleteBookById(@Param("id") UUID id);

    @Query("SELECT b FROM Book b WHERE (b.quantityInStock - b.reservedQuantity) <= b.reorderLevel")
    List<Book> findBooksNeedingRestock();

    @Query("SELECT b FROM Book b WHERE (b.quantityInStock - b.reservedQuantity) <= :threshold")
    List<Book> findBooksWithLowStock(@Param("threshold") int threshold);

    @Query("SELECT b FROM Book b WHERE b.quantityInStock - b.reservedQuantity > 0 ORDER BY b.viewCount DESC")
    List<Book> findAvailableBooksByPopularity(Pageable pageable);

    @Modifying
    @Query("UPDATE Book b SET b.viewCount = b.viewCount + 1 WHERE b.id = :bookId")
    void incrementViewCount(@Param("bookId") UUID bookId);
}