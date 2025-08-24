package com.bookstore.repository;

import com.bookstore.domain.NamedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface NamedEntityRepository<E extends NamedEntity> extends JpaRepository<E, UUID> {

    @Query("SELECT e FROM #{#entityName} e WHERE LOWER(e.name) = LOWER(:name)")
    Optional<E> findByNameIgnoreCase(@Param("name") String name);
}