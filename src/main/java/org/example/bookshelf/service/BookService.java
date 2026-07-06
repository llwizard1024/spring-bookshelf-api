package org.example.bookshelf.service;

import org.example.bookshelf.dto.book.UpdateBookRequest;
import org.example.bookshelf.entity.Book;
import org.example.bookshelf.entity.ReadStatus;
import org.example.bookshelf.exception.BookNotFoundException;
import org.example.bookshelf.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public Page<Book> getBooksByStatus(ReadStatus status, Pageable pageable) {
        return bookRepository.findByStatus(status, pageable);
    }

    public Page<Book> searchByAuthor(String author, Pageable pageable) {
        return bookRepository.findByAuthorContainingIgnoreCase(author, pageable);
    }

    @Transactional
    public Book createBook(String title, String author, String genre, ReadStatus status, Integer rating) {
        Book book = Book.builder()
                .title(title)
                .author(author)
                .genre(genre)
                .status(status)
                .rating(rating)
                .build();

        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(Long id, UpdateBookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        book.set
    }
}
