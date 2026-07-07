package org.example.bookshelf.controller;

import jakarta.validation.Valid;
import org.example.bookshelf.dto.book.BookResponse;
import org.example.bookshelf.dto.book.CreateBookRequest;
import org.example.bookshelf.dto.book.PatchBookRequest;
import org.example.bookshelf.entity.ReadStatus;
import org.example.bookshelf.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Page<BookResponse> searchBooks(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) ReadStatus status,
            @RequestParam(required = false) String genre,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return bookService.searchBooks(status, author, genre, pageable);
    }

    @GetMapping("/{id}")
    public BookResponse getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse createBook(@Valid @RequestBody CreateBookRequest createBookRequest) {
        return bookService.createBook(createBookRequest);
    }

    @PatchMapping("/{id}")
    public BookResponse patchBook(@PathVariable Long id, @Valid @RequestBody PatchBookRequest request) {
        return bookService.patchBook(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBookById(@PathVariable Long id) {
        bookService.deleteBookById(id);
    }
}
