package com.bookshop.vendor.service.image.impl;

import com.bookshop.shared.entity.Book;
import com.bookshop.shared.exception.FileProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class S3PictureStorageServiceImplTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3PictureStorageServiceImpl s3PictureService;

    private Book book;
    private MultipartFile mockFile;

    @BeforeEach
    public void setup() {
        book = Book.builder()
                .id(5L)
                .title("S3 Test Book")
                .build();

        ReflectionTestUtils.setField(s3PictureService, "bucketName", "test-bucket");
    }

    @DisplayName("JUnit test for uploadPicture method - Success")
    @Test
    public void givenValidFile_whenUploadPicture_thenPutsObjectToS3() {
        // given
        mockFile = new MockMultipartFile("file", "cover.jpg", "image/jpeg", "dummy-image-data".getBytes());
        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willReturn(PutObjectResponse.builder().build());

        // when
        s3PictureService.uploadPicture(book, mockFile);

        // then
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @DisplayName("JUnit test for uploadPicture method - Throws Exception on IO Error")
    @Test
    public void givenFileReadError_whenUploadPicture_thenThrowsException() throws IOException {
        // given
        MultipartFile failingFile = mock(MultipartFile.class);
        given(failingFile.getContentType()).willReturn("image/jpeg");
        given(failingFile.getInputStream()).willThrow(new IOException("Stream error"));

        // when & then
        FileProcessingException exception = assertThrows(FileProcessingException.class,
                () -> s3PictureService.uploadPicture(book, failingFile));

        assertThat(exception.getMessage()).contains("Failed to upload image to S3");
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @DisplayName("JUnit test for getPictureUrl method - Generates Presigned URL")
    @Test
    public void givenBook_whenGetPictureUrl_thenReturnPresignedUrl() throws MalformedURLException {
        // given
        URL mockAwsUrl = new URL("https://test-bucket.s3.amazonaws.com/book-covers/5?signature=xyz");

        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        given(presignedRequest.url()).willReturn(mockAwsUrl);

        given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .willReturn(presignedRequest);

        // when
        String resultUrl = s3PictureService.getPictureUrl(book);

        // then
        assertThat(resultUrl).isEqualTo(mockAwsUrl.toString());
        verify(s3Presigner, times(1)).presignGetObject(any(GetObjectPresignRequest.class));
    }
}