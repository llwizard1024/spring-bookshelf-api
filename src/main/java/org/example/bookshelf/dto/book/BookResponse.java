package org.example.bookshelf.dto.book;

import lombok.Builder;
import lombok.Data;
import org.example.bookshelf.entity.ReadStatus;

import java.time.Instant;

@Data
@Builder
public class BookResponse {
    private long id;
    private String title;
    private String author;
    private String genre;
    private ReadStatus status;
    private Integer rating;
    private Instant createdAt;
    private Instant updatedAt;
}
