package org.example.bookshelf.integration;

import org.example.bookshelf.dto.auth.AuthResponse;
import org.example.bookshelf.dto.auth.LoginRequest;
import org.example.bookshelf.dto.auth.LoginResponse;
import org.example.bookshelf.dto.auth.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class AuthApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void overrideTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.liquibase.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("jwt.secret", () -> "integration-test-secret-key-32-chars!!");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void fullAuthLifecycle() {
        AuthResponse registered = registerUser("reader", "reader@example.com");

        assertThat(registered.getUsername()).isEqualTo("reader");
        assertThat(registered.getEmail()).isEqualTo("reader@example.com");
        assertThat(registered.getDisplayName()).isEqualTo("Reader");

        LoginResponse login = login("reader", "password123");

        assertThat(login.getToken()).isNotBlank();
        assertThat(login.getTokenType()).isEqualTo("Bearer");
        assertThat(login.getUser().getUsername()).isEqualTo("reader");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(login.getToken());

        ResponseEntity<AuthResponse> me = restTemplate.exchange(
                "/api/auth/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                AuthResponse.class
        );

        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(me.getBody()).isNotNull();
        assertThat(me.getBody().getUsername()).isEqualTo("reader");
        assertThat(me.getBody().getEmail()).isEqualTo("reader@example.com");
    }

    @Test
    void login_withWrongPassword_returnsUnauthorized() {
        registerUser("wrongpass", "wrongpass@example.com");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wrongpass");
        loginRequest.setPassword("wrong-password");

        ResponseEntity<ProblemDetail> response = restTemplate.postForEntity(
                "/api/auth/login",
                jsonEntity(loginRequest),
                ProblemDetail.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo("Invalid username or password");
    }

    @Test
    void register_duplicateUsername_returnsConflict() {
        registerUser("duplicate", "first@example.com");

        RegisterRequest duplicateRequest = new RegisterRequest();
        duplicateRequest.setUsername("duplicate");
        duplicateRequest.setEmail("second@example.com");
        duplicateRequest.setPassword("password123");
        duplicateRequest.setDisplayName("Second");

        ResponseEntity<ProblemDetail> response = restTemplate.postForEntity(
                "/api/auth/register",
                jsonEntity(duplicateRequest),
                ProblemDetail.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).contains("duplicate");
    }

    @Test
    void me_withoutToken_returnsUnauthorized() {
        ResponseEntity<Void> response = restTemplate.getForEntity("/api/auth/me", Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private AuthResponse registerUser(String username, String email) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword("password123");
        request.setDisplayName("Reader");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/register",
                jsonEntity(request),
                AuthResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private LoginResponse login(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/api/auth/login",
                jsonEntity(request),
                LoginResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private static <T> HttpEntity<T> jsonEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
