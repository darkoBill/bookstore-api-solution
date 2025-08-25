package com.bookstore.mapper;

import com.bookstore.domain.Book;
import com.bookstore.dto.BookDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", 
        uses = {AuthorMapper.class, GenreMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookMapper {
    
    BookDto toDto(Book book);
    
    List<BookDto> toDtoList(List<Book> books);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "authors", ignore = true)
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "viewCount", defaultValue = "0L")
    @Mapping(target = "reservedQuantity", defaultValue = "0")
    @Mapping(target = "reorderLevel", defaultValue = "5")
    Book toEntity(BookDto dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "authors", ignore = true)
    @Mapping(target = "genres", ignore = true)
    void updateEntity(BookDto dto, @MappingTarget Book book);
}