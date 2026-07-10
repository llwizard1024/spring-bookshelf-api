package org.example.bookshelf.security;

public record AuthenticatedUser(Long userId, String username) {
}
