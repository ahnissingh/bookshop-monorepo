package com.bookshop.vendor.service.image.impl;

import com.bookshop.shared.entity.Book;
import com.bookshop.shared.exception.FileProcessingException;
import com.bookshop.shared.repository.BookRepository;
import com.bookshop.vendor.service.image.PictureStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service("databasePictureService")
@RequiredArgsConstructor
public class DatabasePictureStorageServiceImpl implements PictureStorageService {

    private final BookRepository bookRepository;

    @Override
    public void uploadPicture(Book book, MultipartFile file) {
        try {
            book.setPicture(file.getBytes());
            bookRepository.save(book);
        } catch (IOException e) {
            throw new FileProcessingException("Failed to process image file", e);
        }
    }
    //todo migrate return url to client agnostic endpoint
    @Override
    public String getPictureUrl(Book book) {
        // If the book has no picture, return null so UI shows a placeholder
        if (book.getPicture() == null || book.getPicture().length == 0) {
            return null;
        }
        // Return a local URL that the React app will call to fetch the bytes
        return "/api/v1/vendor/books/" + book.getId() + "/picture";
    }
}