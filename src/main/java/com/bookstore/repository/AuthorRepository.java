package com.bookstore.repository;

import com.bookstore.domain.Author;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends NamedEntityRepository<Author> {
}