package com.bookshop.client.service;
import com.bookshop.client.dto.ClientBookResponse;
import com.bookshop.shared.entity.Book;
import com.bookshop.shared.exception.ResourceNotFoundException;
import com.bookshop.shared.mapper.BookMapper;
import com.bookshop.shared.repository.BookRepository;
import com.bookshop.vendor.service.image.PictureStorageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientBookService {

    private final BookRepository bookRepository;
    private final BookMapper clientBookMapper;
    private final PictureStorageService pictureStorageService;

    public ClientBookService(BookRepository bookRepository,
                             BookMapper clientBookMapper,
                             @Qualifier("databasePictureService")
                             PictureStorageService pictureStorageService) {
        this.bookRepository = bookRepository;
        this.clientBookMapper = clientBookMapper;
        this.pictureStorageService = pictureStorageService;
    }

    @Transactional(readOnly = true)
    public Page<ClientBookResponse> getAllBooks(Pageable pageable) {
        // Fetch all books (JpaRepository already has findAll)
        return bookRepository.findAll(pageable)
                .map(book -> clientBookMapper.toClientResponse(book, pictureStorageService.getPictureUrl(book)));
    }

    @Transactional(readOnly = true)
    public ClientBookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        return clientBookMapper.toClientResponse(book, pictureStorageService.getPictureUrl(book));
    }

    // This logic is used by the Controller to serve the bytes
    @Transactional(readOnly = true)
    public byte[] getRawBookPicture(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found for book: " + id));
        return book.getPicture();
    }
}