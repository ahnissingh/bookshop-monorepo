package com.bookshop.vendor.service;

import com.bookshop.shared.entity.Book;
import com.bookshop.shared.entity.User;
import com.bookshop.shared.mapper.BookMapper;
import com.bookshop.shared.repository.BookRepository;
import com.bookshop.vendor.dto.BookRequest;
import com.bookshop.vendor.dto.BookResponse;
import com.bookshop.vendor.dto.VendorBookFilterRequest;
import com.bookshop.vendor.service.image.PictureStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class VendorBookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private PictureStorageService pictureStorageService;

    @InjectMocks
    private VendorBookService vendorBookService;

    private User vendor;
    private Book book;
    private BookRequest bookRequest;
    private BookResponse bookResponse;

    @BeforeEach
    public void setup() {
        vendor = User.builder()
                .id(1L)
                .username("vendor123")
                .email("vendor@test.com")
                .build();

        book = Book.builder()
                .id(100L)
                .title("Spring Boot Mastery")
                .author("Ahnis Singh")
                .price(BigDecimal.valueOf(29.99))
                .quantity(10)
                .user(vendor)
                .build();

        bookRequest = new BookRequest(
                "Spring Boot Mastery", "Ahnis Singh", "A deep dive", BigDecimal.valueOf(29.99), "New", "Great book", 10
        );

        bookResponse = new BookResponse(
                100L, "Spring Boot Mastery", "Ahnis Singh", "A deep dive", BigDecimal.valueOf(29.99), "New", "Great book", 10, "dummyUrl", Instant.now(),Instant.now()
        );
    }

    @DisplayName("JUnit test for getMyBooks method with filters")
    @Test
    public void givenVendorAndPageable_whenGetMyBooks_thenReturnPaginatedBooks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(List.of(book));
        // Creating an empty filter request
        VendorBookFilterRequest filterRequest = new VendorBookFilterRequest(null, null, null, null, null);

        // We mock Specification match since we moved to Specification
        given(bookRepository.findAll(any(Specification.class), eq(pageable))).willReturn(bookPage);
        given(bookMapper.toResponse(book)).willReturn(bookResponse);

        Page<BookResponse> responsePage = vendorBookService.getMyBooks(vendor, filterRequest, pageable);

        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getContent()).hasSize(1);
        assertThat(responsePage.getContent().get(0).title()).isEqualTo("Spring Boot Mastery");
    }

    @DisplayName("JUnit test for getBookById method - Success")
    @Test
    public void givenValidBookIdAndOwner_whenGetBookById_thenReturnBookResponse() {
        given(bookRepository.findByIdAndUser(100L, vendor)).willReturn(Optional.of(book));
        given(bookMapper.toResponse(book)).willReturn(bookResponse);

        BookResponse response = vendorBookService.getBookById(vendor, 100L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(100L);
    }

    @DisplayName("JUnit test for getBookById method - Unauthorized/Not Found")
    @Test
    public void givenInvalidOwner_whenGetBookById_thenThrowsException() {
        given(bookRepository.findByIdAndUser(100L, vendor)).willReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> vendorBookService.getBookById(vendor, 100L));

        verify(bookMapper, never()).toResponse(any(Book.class), any());
    }

    @DisplayName("JUnit test for createBookWithPicture method")
    @Test
    public void givenBookRequest_whenCreateBookWithPicture_thenReturnBookResponse() {
        given(bookMapper.toEntity(bookRequest)).willReturn(book);
        given(bookRepository.save(any(Book.class))).willReturn(book);
        given(bookMapper.toResponse(book)).willReturn(bookResponse);

        BookResponse response = vendorBookService.createBookWithPicture(vendor, bookRequest, null);

        assertThat(response).isNotNull();
        assertThat(book.getUser()).isEqualTo(vendor);
    }

    @DisplayName("JUnit test for updateBook method with MultiPart integration")
    @Test
    public void givenValidBookIdAndRequest_whenUpdateBook_thenReturnUpdatedBookResponse() {
        given(bookRepository.findByIdAndUser(100L, vendor)).willReturn(Optional.of(book));
        given(bookRepository.save(book)).willReturn(book);
        given(bookMapper.toResponse(book)).willReturn(bookResponse);

        BookResponse response = vendorBookService.updateBook(vendor, 100L, bookRequest, null);

        assertThat(response).isNotNull();
        verify(bookMapper).updateEntityFromRequest(bookRequest, book);
        verify(bookRepository).save(book);
    }

    @DisplayName("JUnit test for deleteBook method")
    @Test
    public void givenValidBookId_whenDeleteBook_thenDeletesSuccessfully() {
        given(bookRepository.findByIdAndUser(100L, vendor)).willReturn(Optional.of(book));

        vendorBookService.deleteBook(vendor, 100L);

        verify(bookRepository).delete(book);
    }
}