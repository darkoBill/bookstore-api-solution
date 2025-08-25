package com.bookstore.mapper;

import com.bookstore.domain.Author;
import com.bookstore.dto.AuthorDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface AuthorMapper {
    
    AuthorDto toDto(Author author);
    
    Set<AuthorDto> toDtoSet(Set<Author> authors);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Author toEntity(AuthorDto dto);
}