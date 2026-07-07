package org.example.bookshelf.mapper;

import org.example.bookshelf.dto.book.BookResponse;
import org.example.bookshelf.entity.Book;

public class BookMapper {
    public static BookResponse toResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .genre(book.getGenre())
                .status(book.getStatus())
                .rating(book.getRating())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}
