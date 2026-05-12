package com.bookshop.shared.mapper;

import com.bookshop.client.dto.ClientBookResponse;
import com.bookshop.client.dto.VendorSummary;
import com.bookshop.shared.entity.Book;
import com.bookshop.shared.entity.User;
import com.bookshop.vendor.dto.BookRequest;
import com.bookshop.vendor.dto.BookResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

//todo use isp to split this inf for client and vendor
@Mapper(componentModel = "spring")
public interface BookMapper {

    Book toEntity(BookRequest request);

    @Mapping(target = "pictureUrl", source = "pictureUrl")
    BookResponse toResponse(Book book, String pictureUrl);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(BookRequest request, @MappingTarget Book book);


    @Mapping(target = "vendor", source = "book.user")
    @Mapping(target = "pictureUrl", source = "pictureUrl")
    ClientBookResponse toClientResponse(Book book, String pictureUrl);

    VendorSummary toVendorSummary(User user);
}