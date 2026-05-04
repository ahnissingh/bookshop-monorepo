package com.bookshop.shared.repository;

import com.bookshop.auth.repository.UserRepository;
import com.bookshop.shared.AbstractBaseRepositoryTest;
import com.bookshop.shared.entity.Book;
import com.bookshop.shared.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class BookRepositoryTest extends AbstractBaseRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    private User vendor1;
    private User vendor2;

    @BeforeEach
    void setup() {

        vendor1 = User.builder()
                .firstName("Vendor")
                .lastName("One")
                .email("vendor1@test.com")
                .username("vendor1")
                .password("password123")
                .build();

        vendor2 = User.builder()
                .firstName("Vendor")
                .lastName("Two")
                .email("vendor2@test.com")
                .username("vendor2")
                .password("password123")
                .build();

        userRepository.save(vendor1);
        userRepository.save(vendor2);
    }


    @Test
    @DisplayName("Should find only books belonging to a specific user")
    void givenVendorWithBooks_whenFindByUser_thenReturnOnlyVendorsBooks() {
        // given - setup books for two different vendors
        Book book1 = Book.builder().title("Book A").author("Author A").price(BigDecimal.TEN).user(vendor1).build();
        Book book2 = Book.builder().title("Book B").author("Author B").price(BigDecimal.valueOf(20)).user(vendor1).build();
        Book book3 = Book.builder().title("Book C").author("Author C").price(BigDecimal.valueOf(15)).user(vendor2).build();

        bookRepository.save(book1);
        bookRepository.save(book2);
        bookRepository.save(book3);

        // when
        Page<Book> result = bookRepository.findByUser(vendor1, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Book A", "Book B");
        assertThat(result.getContent())
                .extracting(Book::getTitle)
                .doesNotContain("Book C");
    }

    @Test
    @DisplayName("Should correctly paginate books for a user")
    void givenMultipleBooks_whenFindByUserWithSmallPage_thenPaginateCorrectly() {
        // given - Save 3 books for vendor1
        bookRepository.save(Book.builder().title("B1").author("Author").price(BigDecimal.TEN).user(vendor1).build());
        bookRepository.save(Book.builder().title("B2").author("Author").price(BigDecimal.TEN).user(vendor1).build());
        bookRepository.save(Book.builder().title("B3").author("Author").price(BigDecimal.TEN).user(vendor1).build());

        // when - Request Page 0, but only 2 items per page
        Page<Book> result = bookRepository.findByUser(vendor1, PageRequest.of(0, 2));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return empty Page when user has no books")
    void givenVendorWithNoBooks_whenFindByUser_thenReturnEmptyPage() {
        // given - We use vendor2 who has no books saved
        // when
        Page<Book> result = bookRepository.findByUser(vendor2, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }


    @Test
    @DisplayName("Should find Book by ID and User when ownership matches")
    void givenBookIdAndOwner_whenFindByIdAndUser_thenReturnBook() {
        // given
        Book targetBook = Book.builder().title("Target Book").author("Author").price(BigDecimal.TEN).user(vendor1).build();
        Book savedBook = bookRepository.save(targetBook);

        // when
        Optional<Book> foundBook = bookRepository.findByIdAndUser(savedBook.getId(), vendor1);

        // then
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getTitle()).isEqualTo("Target Book");
    }

    @Test
    @DisplayName("Should return empty when a User tries to find a Book they do not own")
    void givenBookIdAndWrongOwner_whenFindByIdAndUser_thenReturnEmpty() {
        // given - Book belongs to vendor1
        Book targetBook = Book.builder().title("Secret Book").author("Author").price(BigDecimal.TEN).user(vendor1).build();
        Book savedBook = bookRepository.save(targetBook);

        // when - vendor2 tries to access it
        Optional<Book> foundBook = bookRepository.findByIdAndUser(savedBook.getId(), vendor2);

        // then
        assertThat(foundBook).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when looking up a Book ID that does not exist")
    void givenNonExistentBookId_whenFindByIdAndUser_thenReturnEmpty() {
        // given -Random or  mock a fake ID
        Long fakeId = 999L;

        // when
        Optional<Book> foundBook = bookRepository.findByIdAndUser(fakeId, vendor1);

        // then
        assertThat(foundBook).isEmpty();
    }
}