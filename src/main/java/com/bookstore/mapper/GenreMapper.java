package com.bookstore.mapper;

import com.bookstore.domain.Genre;
import com.bookstore.dto.GenreDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface GenreMapper {
    
    GenreDto toDto(Genre genre);
    
    Set<GenreDto> toDtoSet(Set<Genre> genres);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Genre toEntity(GenreDto dto);
}