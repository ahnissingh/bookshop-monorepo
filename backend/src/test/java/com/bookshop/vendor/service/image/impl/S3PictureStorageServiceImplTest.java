package com.bookshop.vendor.service.image.impl;

import com.bookshop.shared.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3PictureStorageServiceImplTest {

    @Mock private S3Client s3Client;
    @Mock private MultipartFile file;

    @InjectMocks private S3PictureStorageServiceImpl service;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(service, "bucketName", "my-bucket");
        ReflectionTestUtils.setField(service, "region", "ap-south-1");
    }

    @Test
    void testUpload() throws Exception {
        Book book = Book.builder().id(55L).build();

        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(10L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[10]));

        service.uploadPicture(book, file);

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUrlGeneration() {
        Book book = Book.builder().id(55L).build();

        String url = service.getPictureUrl(book);


        assertEquals("https://my-bucket.s3.ap-south-1.amazonaws.com/book-covers/55", url);
    }
}