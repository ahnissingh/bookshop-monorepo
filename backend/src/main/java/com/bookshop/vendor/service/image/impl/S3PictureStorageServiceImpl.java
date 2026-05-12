package com.bookshop.vendor.service.image.impl;

import com.bookshop.shared.entity.Book;
import com.bookshop.shared.exception.FileProcessingException;
import com.bookshop.vendor.service.image.PictureStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Service("s3PictureService")
@RequiredArgsConstructor
public class S3PictureStorageServiceImpl implements PictureStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${app.aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public void uploadPicture(Book book, MultipartFile file) {
        String s3Key = "book-covers/" + book.getId();

        // Dynamically get the MIME type
        String contentType = file.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        log.info("Uploading picture for Book ID: {} with Content-Type: {}", book.getId(), contentType);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .cacheControl("max-age=31536000") //V-IMP
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        } catch (IOException e) {
            throw new FileProcessingException("Failed to upload image to S3", e);
        }
    }

    @Override
    public String getPictureUrl(Book book) {
        String s3Key = "book-covers/" + book.getId();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }
}