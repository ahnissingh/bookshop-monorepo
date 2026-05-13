package com.bookshop.client.controller;

import com.bookshop.client.dto.BookSearchFilterRequest;
import com.bookshop.client.dto.ClientBookResponse;
import com.bookshop.client.service.ClientBookService;
import com.bookshop.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/client/books")
@PreAuthorize("hasRole('ROLE_CLIENT')")
@RequiredArgsConstructor
public class ClientController {
    private final ClientBookService clientBookService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ClientBookResponse>>> browseBooks(
            BookSearchFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Client browsing books with filters: {} - Page: {}, Size: {}, SortBy: {}",
                filterRequest, page, size, sortBy);

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ClientBookResponse> books = clientBookService.getAllBooks(filterRequest, pageable);

        return ResponseEntity.ok(ApiResponse.success(books, "Books fetched successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientBookResponse>> getBookDetails(@PathVariable Long id) {
        log.info("Client requesting details for book ID: {}", id);

        ClientBookResponse book = clientBookService.getBookById(id);

        return ResponseEntity.ok(ApiResponse.success(book, "Book details fetched successfully"));
    }
}
