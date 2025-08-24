package com.bookstore.repository;

import com.bookstore.domain.Author;
import org.springframework.stereotype.Repository;
import java.util.Set;
import java.util.UUID;

@Repository
public interface AuthorRepository extends NamedEntityRepository<Author> {
    
    Set<Author> findByIdIn(Set<UUID> ids);
}