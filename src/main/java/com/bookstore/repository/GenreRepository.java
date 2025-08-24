package com.bookstore.repository;

import com.bookstore.domain.Genre;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends NamedEntityRepository<Genre> {
}