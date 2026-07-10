package org.example.bookshelf.controller;

import org.example.bookshelf.dto.book.BookResponse;
import org.example.bookshelf.entity.ReadStatus;
import org.example.bookshelf.exception.BookNotFoundException;
import org.example.bookshelf.exception.GlobalExceptionHandler;
import org.example.bookshelf.security.JwtAuthFilter;
import org.example.bookshelf.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void createBook_returnsCreatedBook() throws Exception {
        BookResponse response = sampleResponse();

        when(bookService.createBook(any())).thenReturn(response);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "War and Peace",
                                  "author": "Leo Tolstoy",
                                  "genre": "Novel",
                                  "status": "READ",
                                  "rating": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("War and Peace"))
                .andExpect(jsonPath("$.author").value("Leo Tolstoy"))
                .andExpect(jsonPath("$.genre").value("Novel"))
                .andExpect(jsonPath("$.status").value("READ"))
                .andExpect(jsonPath("$.rating").value(10));

        verify(bookService).createBook(any());
    }

    @Test
    void createBook_whenValidationFails_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "author": "Leo Tolstoy",
                                  "genre": "Novel",
                                  "status": "READ"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.fields.title").exists());
    }

    @Test
    void getBookById_returnsBook() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("War and Peace"));

        verify(bookService).getBookById(1L);
    }

    @Test
    void getBookById_whenBookNotFound_returnsNotFound() throws Exception {
        when(bookService.getBookById(99L)).thenThrow(new BookNotFoundException(99L));

        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Book not found: 99"));
    }

    @Test
    void patchBook_returnsUpdatedBook() throws Exception {
        BookResponse response = BookResponse.builder()
                .id(1L)
                .title("War and Peace")
                .author("Leo Tolstoy")
                .genre("Novel")
                .status(ReadStatus.READ)
                .rating(9)
                .createdAt(Instant.parse("2026-01-01T10:00:00Z"))
                .updatedAt(Instant.parse("2026-01-02T10:00:00Z"))
                .build();

        when(bookService.patchBook(eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "READ",
                                  "rating": 9
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READ"))
                .andExpect(jsonPath("$.rating").value(9));

        verify(bookService).patchBook(eq(1L), any());
    }

    @Test
    void patchBook_whenValidationFails_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 11
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.rating").exists());
    }

    @Test
    void patchBook_whenBookNotFound_returnsNotFound() throws Exception {
        doThrow(new BookNotFoundException(99L))
                .when(bookService)
                .patchBook(eq(99L), any());

        mockMvc.perform(patch("/api/books/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "READ"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Book not found: 99"));
    }

    @Test
    void deleteBookById_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBookById(1L);
    }

    @Test
    void deleteBookById_whenBookNotFound_returnsNotFound() throws Exception {
        doThrow(new BookNotFoundException(99L))
                .when(bookService)
                .deleteBookById(99L);

        mockMvc.perform(delete("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Book not found: 99"));
    }

    private static BookResponse sampleResponse() {
        return BookResponse.builder()
                .id(1L)
                .title("War and Peace")
                .author("Leo Tolstoy")
                .genre("Novel")
                .status(ReadStatus.READ)
                .rating(10)
                .createdAt(Instant.parse("2026-01-01T10:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T10:00:00Z"))
                .build();
    }
}
