package org.example.bookshelf.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Books", description = "Book management operations")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    @Operation(summary = "Search books", description = "Returns a paginated list of books with optional filters")
    public Page<BookResponse> searchBooks(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) ReadStatus status,
            @RequestParam(required = false) String genre,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return bookService.searchBooks(status, author, genre, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by id")
    @ApiResponse(responseCode = "404", description = "Book not found")
    public BookResponse getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a book")
    @ApiResponse(responseCode = "201", description = "Book created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public BookResponse createBook(@Valid @RequestBody CreateBookRequest createBookRequest) {
        return bookService.createBook(createBookRequest);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a book")
    @ApiResponse(responseCode = "404", description = "Book not found")
    public BookResponse patchBook(@PathVariable Long id, @Valid @RequestBody PatchBookRequest request) {
        return bookService.patchBook(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a book")
    @ApiResponse(responseCode = "204", description = "Book deleted")
    @ApiResponse(responseCode = "404", description = "Book not found")
    public void deleteBookById(@PathVariable Long id) {
        bookService.deleteBookById(id);
    }
}
