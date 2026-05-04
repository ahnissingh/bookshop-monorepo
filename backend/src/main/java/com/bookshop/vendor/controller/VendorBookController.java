package com.bookshop.vendor.controller;

import com.bookshop.shared.dto.ApiResponse;
import com.bookshop.shared.entity.User;
import com.bookshop.vendor.dto.BookRequest;
import com.bookshop.vendor.dto.BookResponse;
import com.bookshop.vendor.service.VendorBookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
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
            @AuthenticationPrincipal User vendor, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                vendorBookService.getMyBooks(vendor, pageable), "Books fetched successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(
            @AuthenticationPrincipal User vendor, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                vendorBookService.getBookById(vendor, id), "Book fetched successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
            @AuthenticationPrincipal User vendor, @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                vendorBookService.createBook( vendor, request), "Book created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @AuthenticationPrincipal User vendor, @PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                vendorBookService.updateBook(vendor, id, request), "Book updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(
            @AuthenticationPrincipal User vendor, @PathVariable Long id) {
        vendorBookService.deleteBook(vendor, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Book deleted successfully"));
    }


    @PostMapping(value = "/{id}/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> uploadPicture(
            @AuthenticationPrincipal User vendor,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        vendorBookService.uploadBookPicture(vendor, id, file);
        return ResponseEntity.ok(ApiResponse.success(null, "Picture uploaded successfully"));
    }

    // we do NOT return an ApiResponse wrapping the data here.
    // We return raw bytes with an image content-type so the browser can render it directly
    @GetMapping("/{id}/picture")
    public ResponseEntity<byte[]> getBookPicture(
            @AuthenticationPrincipal User vendor, @PathVariable Long id) {
        byte[] imageBytes = vendorBookService.getRawBookPicture(vendor, id);

        if (imageBytes == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                .body(imageBytes);
    }
}