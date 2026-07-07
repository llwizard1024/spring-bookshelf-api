package org.example.bookshelf.service;

import org.example.bookshelf.dto.book.BookResponse;
import org.example.bookshelf.dto.book.CreateBookRequest;
import org.example.bookshelf.dto.book.PatchBookRequest;
import org.example.bookshelf.entity.Book;
import org.example.bookshelf.entity.ReadStatus;
import org.example.bookshelf.exception.BookNotFoundException;
import org.example.bookshelf.mapper.BookMapper;
import org.example.bookshelf.repository.BookRepository;
import org.example.bookshelf.repository.BookSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Page<BookResponse> searchBooks(ReadStatus status, String author, String genre, Pageable pageable) {
        Specification<Book> spec = Specification
                .where(BookSpecifications.hasStatus(status))
                .and(BookSpecifications.authorContains(author))
                .and(BookSpecifications.genreContains(genre));

        return bookRepository.findAll(spec, pageable)
                .map(BookMapper::toResponse);
    }

    public BookResponse getBookById(Long id) {
        return bookRepository.findById(id)
                .map(BookMapper::toResponse)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .genre(request.getGenre())
                .status(request.getStatus())
                .rating(request.getRating())
                .build();

        Book saved = bookRepository.save(book);
        return BookMapper.toResponse(saved);
    }

    @Transactional
    public BookResponse patchBook(Long id, PatchBookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        if (request.getTitle() != null) book.setTitle(request.getTitle());
        if (request.getAuthor() != null) book.setAuthor(request.getAuthor());
        if (request.getGenre() != null) book.setGenre(request.getGenre());
        if (request.getStatus() != null) book.setStatus(request.getStatus());
        if (request.getRating() != null) book.setRating(request.getRating());

        Book saved = bookRepository.save(book);
        return BookMapper.toResponse(saved);
    }

    @Transactional
    public void deleteBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        bookRepository.delete(book);
    }
}
