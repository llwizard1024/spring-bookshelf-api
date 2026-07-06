package org.example.bookshelf.repository;

import org.example.bookshelf.entity.Book;
import org.example.bookshelf.entity.ReadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.web.PageableDefault;

public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findByStatus(ReadStatus status, Pageable pageable);
    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);
}
