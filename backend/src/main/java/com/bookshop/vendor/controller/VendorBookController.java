package com.bookshop.vendor.controller;

import com.bookshop.shared.dto.ApiResponse;
import com.bookshop.shared.entity.User;
import com.bookshop.vendor.dto.BookRequest;
import com.bookshop.vendor.dto.BookResponse;
import com.bookshop.vendor.dto.VendorBookFilterRequest;
import com.bookshop.vendor.service.VendorBookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/vendor/books")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_VENDOR')")
public class VendorBookController {
    private final VendorBookService vendorBookService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getMyBooks(
            @AuthenticationPrincipal User vendor,
            VendorBookFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return ResponseEntity.ok(ApiResponse.success(
                vendorBookService.getMyBooks(vendor, filterRequest, pageable), "Books fetched successfully"));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(
            @AuthenticationPrincipal User vendor, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                vendorBookService.getBookById(vendor, id), "Book fetched successfully"));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
            @AuthenticationPrincipal User vendor,
            @Valid @RequestPart("book") BookRequest request, // JSON part
            @RequestPart(value = "file", required = false) MultipartFile file) { // File part (optional rakha hai incase bina photo ke book daalni ho)

        log.info("Vendor {} creating book {} with picture attached: {}", vendor.getUsername(), request.title(), file != null);

        return ResponseEntity.ok(ApiResponse.success(
                vendorBookService.createBookWithPicture(vendor, request, file), "Book created successfully"));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @AuthenticationPrincipal User vendor,
            @PathVariable Long id,
            @Valid @RequestPart("book") BookRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        log.info("Vendor {} updating book ID {} with new picture attached: {}", vendor.getUsername(), id, file != null);

        return ResponseEntity.ok(ApiResponse.success(
                vendorBookService.updateBook(vendor, id, request, file), "Book updated successfully"));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(
            @AuthenticationPrincipal User vendor, @PathVariable Long id) {
        vendorBookService.deleteBook(vendor, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Book deleted successfully"));
    }




}