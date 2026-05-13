package com.bookshop.vendor.service;

import com.bookshop.shared.entity.Book;
import com.bookshop.shared.entity.User;
import com.bookshop.shared.exception.ResourceNotFoundException;
import com.bookshop.vendor.dto.BookRequest;
import com.bookshop.vendor.dto.BookResponse;
import com.bookshop.shared.mapper.BookMapper;
import com.bookshop.shared.repository.BookRepository;
import com.bookshop.vendor.service.image.PictureStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<BookResponse> getMyBooks(User vendor, Pageable pageable) {
        return bookRepository.findByUser(vendor, pageable)
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
    public BookResponse updateBook(User vendor, Long bookId, BookRequest request) {
        Book book = getVerifiedBook(vendor, bookId);

        bookMapper.updateEntityFromRequest(request, book);
        Book updatedBook = bookRepository.save(book);

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

        // Let the strategy (DB or S3) handle the actual saving
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
}