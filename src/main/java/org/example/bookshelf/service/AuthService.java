package org.example.bookshelf.service;

import lombok.extern.slf4j.Slf4j;
import org.example.bookshelf.dto.auth.AuthResponse;
import org.example.bookshelf.dto.auth.LoginRequest;
import org.example.bookshelf.dto.auth.LoginResponse;
import org.example.bookshelf.dto.auth.RegisterRequest;
import org.example.bookshelf.entity.User;
import org.example.bookshelf.exception.EmailAlreadyExistsException;
import org.example.bookshelf.exception.InvalidCredentialsException;
import org.example.bookshelf.exception.UserNotFoundException;
import org.example.bookshelf.exception.UsernameAlreadyExistsException;
import org.example.bookshelf.mapper.AuthMapper;
import org.example.bookshelf.repository.UserRepository;
import org.example.bookshelf.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .displayName(request.getDisplayName())
                .build();

        User saved = userRepository.save(user);
        log.info("User registered: {}", saved.getUsername());

        return AuthMapper.toResponse(saved);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Failed login attempt for username: {}", request.getUsername());
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername());
        log.info("User logged in: {}", user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .user(AuthMapper.toResponse(user))
                .build();
    }

    public AuthResponse getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .map(AuthMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
