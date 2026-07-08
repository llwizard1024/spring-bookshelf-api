package org.example.bookshelf.service;

import org.example.bookshelf.dto.book.BookResponse;
import org.example.bookshelf.dto.book.CreateBookRequest;
import org.example.bookshelf.dto.book.PatchBookRequest;
import org.example.bookshelf.entity.Book;
import org.example.bookshelf.entity.ReadStatus;
import org.example.bookshelf.exception.BookNotFoundException;
import org.example.bookshelf.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void createBook_savesBookAndReturnsResponse() {
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("War and Peace");
        request.setAuthor("Leo Tolstoy");
        request.setGenre("Novel");
        request.setStatus(ReadStatus.READ);
        request.setRating(10);

        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            return Book.builder()
                    .id(1L)
                    .title(book.getTitle())
                    .author(book.getAuthor())
                    .genre(book.getGenre())
                    .status(book.getStatus())
                    .rating(book.getRating())
                    .createdAt(Instant.parse("2026-01-01T10:00:00Z"))
                    .updatedAt(Instant.parse("2026-01-01T10:00:00Z"))
                    .build();
        });

        BookResponse response = bookService.createBook(request);

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(bookCaptor.capture());

        assertThat(bookCaptor.getValue().getTitle()).isEqualTo("War and Peace");
        assertThat(bookCaptor.getValue().getStatus()).isEqualTo(ReadStatus.READ);
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("War and Peace");
        assertThat(response.getRating()).isEqualTo(10);
    }

    @Test
    void getBookById_returnsMappedResponse() {
        Book book = sampleBook();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookResponse response = bookService.getBookById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("War and Peace");
        assertThat(response.getStatus()).isEqualTo(ReadStatus.READING);
    }

    @Test
    void getBookById_whenBookNotFound_throwsException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("Book not found: 99");
    }

    @Test
    void patchBook_updatesOnlyProvidedFields() {
        Book book = sampleBook();
        PatchBookRequest request = new PatchBookRequest();
        request.setStatus(ReadStatus.READ);
        request.setRating(9);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse response = bookService.patchBook(1L, request);

        assertThat(response.getTitle()).isEqualTo("War and Peace");
        assertThat(response.getAuthor()).isEqualTo("Leo Tolstoy");
        assertThat(response.getStatus()).isEqualTo(ReadStatus.READ);
        assertThat(response.getRating()).isEqualTo(9);

        verify(bookRepository).findById(1L);
        verify(bookRepository).save(book);
    }

    @Test
    void patchBook_whenBookNotFound_throwsException() {
        PatchBookRequest request = new PatchBookRequest();
        request.setStatus(ReadStatus.READ);

        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.patchBook(99L, request))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("Book not found: 99");

        verify(bookRepository).findById(99L);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    void searchBooks_returnsMappedPage() {
        Page<Book> page = new PageImpl<>(List.of(sampleBook()), PageRequest.of(0, 20), 1);

        when(bookRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<BookResponse> response = bookService.searchBooks(ReadStatus.READ, "tolstoy", "novel", PageRequest.of(0, 20));

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().getFirst().getTitle()).isEqualTo("War and Peace");
        assertThat(response.getContent().getFirst().getStatus()).isEqualTo(ReadStatus.READING);
    }

    @Test
    void deleteBookById_deletesExistingBook() {
        Book book = sampleBook();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.deleteBookById(1L);

        verify(bookRepository).findById(1L);
        verify(bookRepository).delete(book);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    void deleteBookById_whenBookNotFound_throwsException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.deleteBookById(99L))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("Book not found: 99");

        verify(bookRepository).findById(99L);
        verifyNoMoreInteractions(bookRepository);
    }

    private static Book sampleBook() {
        return Book.builder()
                .id(1L)
                .title("War and Peace")
                .author("Leo Tolstoy")
                .genre("Novel")
                .status(ReadStatus.READING)
                .rating(10)
                .createdAt(Instant.parse("2026-01-01T10:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T10:00:00Z"))
                .build();
    }
}
