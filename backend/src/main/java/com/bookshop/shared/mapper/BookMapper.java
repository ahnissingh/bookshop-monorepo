package com.bookshop.shared.mapper;

import com.bookshop.shared.entity.Book;
import com.bookshop.vendor.dto.BookRequest;
import com.bookshop.vendor.dto.BookResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface BookMapper {

    Book toEntity(BookRequest request);

    @Mapping(target = "pictureUrl", source = "pictureUrl")
    BookResponse toResponse(Book book, String pictureUrl);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(BookRequest request, @MappingTarget Book book);
}