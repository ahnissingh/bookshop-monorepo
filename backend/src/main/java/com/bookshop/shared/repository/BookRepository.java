package com.bookshop.shared.repository;


import com.bookshop.shared.entity.Book;
import com.bookshop.shared.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findByUser(User user, Pageable pageable);

    Optional<Book> findByIdAndUser(Long id, User user);

    //Client side
    @EntityGraph(attributePaths = {"user"})
    Page<Book> findAll(Pageable pageable);

    // Filter by title or author (ignore case)
    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
            String title, String author, Pageable pageable );
}