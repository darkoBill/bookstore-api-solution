package com.bookstore.repository;

import com.bookstore.domain.Genre;
import org.springframework.stereotype.Repository;
import java.util.Set;
import java.util.UUID;

@Repository
public interface GenreRepository extends NamedEntityRepository<Genre> {
    
    Set<Genre> findByIdIn(Set<UUID> ids);
}