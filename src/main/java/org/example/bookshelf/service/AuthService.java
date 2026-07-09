package org.example.bookshelf.service;

import lombok.extern.slf4j.Slf4j;
import org.example.bookshelf.dto.auth.AuthResponse;
import org.example.bookshelf.dto.auth.RegisterRequest;
import org.example.bookshelf.entity.User;
import org.example.bookshelf.exception.EmailAlreadyExistsException;
import org.example.bookshelf.exception.UsernameAlreadyExistsException;
import org.example.bookshelf.mapper.AuthMapper;
import org.example.bookshelf.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

        return AuthMapper.toResponse(saved);
    }
}
