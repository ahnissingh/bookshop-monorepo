package com.bookshop.client.service;
import com.bookshop.client.dto.ClientBookSearchFilterRequest;
import com.bookshop.client.dto.ClientBookResponse;
import com.bookshop.client.specification.BookSpecification;
import com.bookshop.shared.entity.Book;
import com.bookshop.shared.exception.ResourceNotFoundException;
import com.bookshop.shared.mapper.BookMapper;
import com.bookshop.shared.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientBookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Transactional(readOnly = true)
    public Page<ClientBookResponse> getAllBooks(ClientBookSearchFilterRequest filterRequest, Pageable pageable) {
        log.info("Fetching books with filters: {}", filterRequest);

        Specification<Book> spec = BookSpecification.withFilters(filterRequest);

        // Execute the query and map the results
        return bookRepository.findAll(spec, pageable)
                .map(bookMapper::toClientResponse);
    }

    @Transactional(readOnly = true)
    public ClientBookResponse getBookById(Long id) {
        log.info("Fetching book details for ID: {}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        return bookMapper.toClientResponse(book);
    }



}