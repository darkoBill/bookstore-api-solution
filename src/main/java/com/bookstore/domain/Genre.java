package com.bookstore.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "genres", indexes = {
    @Index(name = "idx_genre_name_lower", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true, exclude = "books")
public class Genre extends BaseEntity implements NamedEntity {
    
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;
    
    @ManyToMany(mappedBy = "genres", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Book> books = new HashSet<>();
}