package com.bookshop.vendor.service.image.impl;

import com.bookshop.shared.entity.Book;
import com.bookshop.vendor.service.image.PictureStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service("s3PictureService")
public class S3PictureStorageServiceImpl implements PictureStorageService {

    @Override
    public void uploadPicture(Book book, MultipartFile file) {
       //logic to upload picture
    }

    @Override
    public String getPictureUrl(Book book) {

        return "PresignedURL";
    }
}