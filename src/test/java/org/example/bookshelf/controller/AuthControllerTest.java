package org.example.bookshelf.controller;

import org.example.bookshelf.dto.auth.AuthResponse;
import org.example.bookshelf.dto.auth.LoginResponse;
import org.example.bookshelf.exception.GlobalExceptionHandler;
import org.example.bookshelf.security.JwtAuthFilter;
import org.example.bookshelf.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void register_returnsCreatedUser() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .id(1L)
                .username("reader")
                .email("reader@example.com")
                .displayName("Reader")
                .build();

        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "reader",
                                  "email": "reader@example.com",
                                  "password": "password123",
                                  "displayName": "Reader"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("reader"))
                .andExpect(jsonPath("$.email").value("reader@example.com"));

        verify(authService).register(any());
    }

    @Test
    void login_returnsTokenAndUser() throws Exception {
        AuthResponse user = AuthResponse.builder()
                .id(1L)
                .username("reader")
                .email("reader@example.com")
                .displayName("Reader")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .token("jwt-token")
                .user(user)
                .build();

        when(authService.login(any())).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "reader",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("reader"));

        verify(authService).login(any());
    }
}
