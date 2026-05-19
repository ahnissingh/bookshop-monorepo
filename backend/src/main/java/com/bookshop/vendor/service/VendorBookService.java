package com.bookshop.vendor.service;

import com.bookshop.shared.entity.Book;
import com.bookshop.shared.entity.User;
import com.bookshop.shared.exception.ResourceNotFoundException;
import com.bookshop.vendor.dto.BookRequest;
import com.bookshop.vendor.dto.BookResponse;
import com.bookshop.shared.mapper.BookMapper;
import com.bookshop.shared.repository.BookRepository;
import com.bookshop.vendor.dto.VendorBookFilterRequest;
import com.bookshop.vendor.service.image.PictureStorageService;
import com.bookshop.vendor.specification.VendorBookSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class VendorBookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final PictureStorageService pictureStorageService;

    public VendorBookService(BookRepository bookRepository,
                             BookMapper bookMapper,
                             @Qualifier("s3PictureService") PictureStorageService pictureStorageService) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.pictureStorageService = pictureStorageService;
    }



    @Transactional(readOnly = true)
    public Page<BookResponse> getMyBooks(User vendor, VendorBookFilterRequest filterRequest, Pageable pageable) {
        Specification<Book> spec = VendorBookSpecification.withFilters(filterRequest, vendor);
        return bookRepository.findAll(spec, pageable)
                .map(bookMapper::toResponse);
    }
    @Transactional(readOnly = true)
    public BookResponse getBookById(User vendor, Long bookId) {
        Book book = getVerifiedBook(vendor, bookId);
        return bookMapper.toResponse(book);
    }

    @Transactional
    public BookResponse createBook(User vendor, BookRequest request) {
        Book book = bookMapper.toEntity(request);
        book.setUser(vendor); // Enforce ownership at creation

        Book savedBook = bookRepository.save(book);
        return bookMapper.toResponse(savedBook);
    }

    @Transactional
    public BookResponse updateBook(User vendor, Long bookId, BookRequest request, MultipartFile file) {
        Book book = getVerifiedBook(vendor, bookId);

        bookMapper.updateEntityFromRequest(request, book);

        if (file != null && !file.isEmpty()) {
            pictureStorageService.uploadPicture(book, file);
            String publicUrl = pictureStorageService.getPictureUrl(book);

            book.setPictureUrl(publicUrl + "?v=" + System.currentTimeMillis());
        }

        Book updatedBook = bookRepository.save(book);

        // 5. Ab return karo modified entity
        return bookMapper.toResponse(updatedBook);
    }

    @Transactional
    public void deleteBook(User vendor, Long bookId) {
        Book book = getVerifiedBook(vendor, bookId);
        bookRepository.delete(book);
    }


    @Transactional
    public void uploadBookPicture(User vendor, Long bookId, MultipartFile file) {
        Book book = getVerifiedBook(vendor, bookId);

        pictureStorageService.uploadPicture(book, file);
        String publicUrl = pictureStorageService.getPictureUrl(book);
        //Now I attach the pictureUrl as a string in the book table
        book.setPictureUrl(publicUrl);
        bookRepository.save(book);
    }


    private Book getVerifiedBook(User vendor, Long bookId) {
        return bookRepository.findByIdAndUser(bookId, vendor)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found or you are not authorized to access it"));
    }

    @Transactional
    public BookResponse createBookWithPicture(User vendor, BookRequest request, MultipartFile file) {
        Book book = bookMapper.toEntity(request);
        book.setUser(vendor);

        Book savedBook = bookRepository.save(book);

        if (file != null && !file.isEmpty()) {
            pictureStorageService.uploadPicture(savedBook, file);
            String publicUrl = pictureStorageService.getPictureUrl(savedBook);

            savedBook.setPictureUrl(publicUrl + "?v=" + System.currentTimeMillis());

            savedBook = bookRepository.save(savedBook);
        }

        return bookMapper.toResponse(savedBook);
    }

}