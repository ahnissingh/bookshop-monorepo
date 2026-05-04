package com.bookshop.vendor.service.image;

import com.bookshop.shared.entity.Book;
import org.springframework.web.multipart.MultipartFile;

public interface PictureStorageService {

    // Handles saving the image (to DB or to S3)
    void uploadPicture(Book book, MultipartFile file);

    // Returns a URL that the React frontend can directly use
    String getPictureUrl(Book book);
}