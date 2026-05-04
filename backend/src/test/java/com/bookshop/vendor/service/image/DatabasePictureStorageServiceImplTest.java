package com.bookshop.vendor.service.image;

import com.bookshop.shared.entity.Book;
import com.bookshop.shared.repository.BookRepository;
import com.bookshop.vendor.service.image.impl.DatabasePictureStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DatabasePictureStorageServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private DatabasePictureStorageServiceImpl databasePictureService;

    private Book book;
    private MultipartFile mockFile;

    @BeforeEach
    public void setup() {
        book = Book.builder()
                .id(1L)
                .title("Test Book")
                .build();

        mockFile = mock(MultipartFile.class);
    }

    @DisplayName("JUnit test for uploadPicture method - Success")
    @Test
    public void givenValidFile_whenUploadPicture_thenSavesBytesToDatabase() throws IOException {
        // given
        byte[] imageBytes = {1, 2, 3, 4, 5};
        given(mockFile.getBytes()).willReturn(imageBytes);

        // when
        databasePictureService.uploadPicture(book, mockFile);

        // then
        assertThat(book.getPicture()).isEqualTo(imageBytes);
        verify(bookRepository).save(book);
    }

    @DisplayName("JUnit test for uploadPicture method - Throws Exception on IO Error")
    @Test
    public void givenFileReadError_whenUploadPicture_thenThrowsRuntimeException() throws IOException {
        given(mockFile.getBytes()).willThrow(new IOException("Failed to read file stream"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> databasePictureService.uploadPicture(book, mockFile));

        assertThat(exception.getMessage()).isEqualTo("Failed to process image file");
        verify(bookRepository, never()).save(book);
    }

    @DisplayName("JUnit test for getPictureUrl method - With valid picture")
    @Test
    public void givenBookWithPicture_whenGetPictureUrl_thenReturnCorrectUrl() {
        book.setPicture(new byte[]{1, 2, 3});

        String url = databasePictureService.getPictureUrl(book);

        assertThat(url).isEqualTo("/api/v1/vendor/books/1/picture");
    }

    @DisplayName("JUnit test for getPictureUrl method - With null picture")
    @Test
    public void givenBookWithNullPicture_whenGetPictureUrl_thenReturnNull() {
        book.setPicture(null);

        String url = databasePictureService.getPictureUrl(book);

        assertThat(url).isNull();
    }

    @DisplayName("JUnit test for getPictureUrl method - With empty picture array")
    @Test
    public void givenBookWithEmptyPicture_whenGetPictureUrl_thenReturnNull() {
        book.setPicture(new byte[0]);

        String url = databasePictureService.getPictureUrl(book);

        assertThat(url).isNull();
    }
}