package org.example.bookshelf.integration;

import org.example.bookshelf.dto.book.BookResponse;
import org.example.bookshelf.dto.book.CreateBookRequest;
import org.example.bookshelf.dto.book.PatchBookRequest;
import org.example.bookshelf.entity.ReadStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class BookApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void overrideTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.liquibase.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void fullBookLifecycle() {
        CreateBookRequest createRequest = new CreateBookRequest();
        createRequest.setTitle("War and Peace");
        createRequest.setAuthor("Leo Tolstoy");
        createRequest.setGenre("Novel");
        createRequest.setStatus(ReadStatus.READING);
        createRequest.setRating(9);

        ResponseEntity<BookResponse> created = restTemplate.postForEntity(
                "/api/books",
                createRequest,
                BookResponse.class
        );

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().getId()).isPositive();
        assertThat(created.getBody().getTitle()).isEqualTo("War and Peace");
        assertThat(created.getBody().getCreatedAt()).isNotNull();

        Long bookId = created.getBody().getId();

        ResponseEntity<BookResponse> fetched = restTemplate.getForEntity(
                "/api/books/" + bookId,
                BookResponse.class
        );
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody()).isNotNull();
        assertThat(fetched.getBody().getAuthor()).isEqualTo("Leo Tolstoy");

        PatchBookRequest patchRequest = new PatchBookRequest();
        patchRequest.setStatus(ReadStatus.READ);
        patchRequest.setRating(10);

        ResponseEntity<BookResponse> patched = restTemplate.exchange(
                "/api/books/" + bookId,
                HttpMethod.PATCH,
                new HttpEntity<>(patchRequest),
                BookResponse.class
        );
        assertThat(patched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(patched.getBody()).isNotNull();
        assertThat(patched.getBody().getStatus()).isEqualTo(ReadStatus.READ);
        assertThat(patched.getBody().getRating()).isEqualTo(10);
        assertThat(patched.getBody().getTitle()).isEqualTo("War and Peace");

        ResponseEntity<Map<String, Object>> search = restTemplate.exchange(
                "/api/books?author=tolstoy&status=READ&genre=novel",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(search.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(search.getBody()).isNotNull();
        assertThat(search.getBody().get("totalElements")).isEqualTo(1);

        ResponseEntity<Void> deleted = restTemplate.exchange(
                "/api/books/" + bookId,
                HttpMethod.DELETE,
                null,
                Void.class
        );
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<ProblemDetail> notFound = restTemplate.getForEntity(
                "/api/books/" + bookId,
                ProblemDetail.class
        );
        assertThat(notFound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(notFound.getBody()).isNotNull();
        assertThat(notFound.getBody().getDetail()).contains("Book not found");
    }

    @Test
    void createBook_whenValidationFails_returnsProblemDetail() {
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("");
        request.setAuthor("Leo Tolstoy");
        request.setGenre("Novel");
        request.setStatus(ReadStatus.READ);

        ResponseEntity<ProblemDetail> response = restTemplate.postForEntity(
                "/api/books",
                request,
                ProblemDetail.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Validation failed");
        assertThat(response.getBody().getProperties()).containsKey("fields");
    }
}
