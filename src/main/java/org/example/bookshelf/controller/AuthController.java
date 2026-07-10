package org.example.bookshelf.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.bookshelf.dto.auth.AuthResponse;
import org.example.bookshelf.dto.auth.LoginRequest;
import org.example.bookshelf.dto.auth.LoginResponse;
import org.example.bookshelf.dto.auth.RegisterRequest;
import org.example.bookshelf.security.AuthenticatedUser;
import org.example.bookshelf.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Registration and authentication")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Log in and receive a JWT token")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Get the currently authenticated user")
    public AuthResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        return authService.getCurrentUser(user.userId());
    }
}
