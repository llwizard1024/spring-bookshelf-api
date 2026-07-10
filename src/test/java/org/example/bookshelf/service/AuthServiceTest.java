package org.example.bookshelf.service;

import org.example.bookshelf.dto.auth.AuthResponse;
import org.example.bookshelf.dto.auth.LoginRequest;
import org.example.bookshelf.dto.auth.LoginResponse;
import org.example.bookshelf.dto.auth.RegisterRequest;
import org.example.bookshelf.entity.User;
import org.example.bookshelf.exception.EmailAlreadyExistsException;
import org.example.bookshelf.exception.InvalidCredentialsException;
import org.example.bookshelf.exception.UsernameAlreadyExistsException;
import org.example.bookshelf.repository.UserRepository;
import org.example.bookshelf.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_savesEncodedUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("reader");
        request.setEmail("reader@example.com");
        request.setPassword("password123");
        request.setDisplayName("Reader");

        when(userRepository.existsByUsername("reader")).thenReturn(false);
        when(userRepository.existsByEmail("reader@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        AuthResponse response = authService.register(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("reader");
        assertThat(response.getEmail()).isEqualTo("reader@example.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("encoded-password");
    }

    @Test
    void register_whenUsernameExists_throwsConflict() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("reader");

        when(userRepository.existsByUsername("reader")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UsernameAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenEmailExists_throwsConflict() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("reader");
        request.setEmail("reader@example.com");

        when(userRepository.existsByUsername("reader")).thenReturn(false);
        when(userRepository.existsByEmail("reader@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsTokenAndUser() {
        LoginRequest request = new LoginRequest();
        request.setUsername("reader");
        request.setPassword("password123");

        User user = User.builder()
                .id(1L)
                .username("reader")
                .email("reader@example.com")
                .passwordHash("encoded-password")
                .displayName("Reader")
                .build();

        when(userRepository.findByUsername("reader")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(1L, "reader")).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getUsername()).isEqualTo("reader");
    }

    @Test
    void login_whenPasswordInvalid_throwsUnauthorized() {
        LoginRequest request = new LoginRequest();
        request.setUsername("reader");
        request.setPassword("wrong-password");

        User user = User.builder()
                .id(1L)
                .username("reader")
                .passwordHash("encoded-password")
                .build();

        when(userRepository.findByUsername("reader")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any(), anyString());
    }

    @Test
    void login_whenUserNotFound_throwsUnauthorized() {
        LoginRequest request = new LoginRequest();
        request.setUsername("missing");
        request.setPassword("password123");

        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
