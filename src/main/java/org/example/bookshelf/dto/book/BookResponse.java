package org.example.bookshelf.dto.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bookshelf.entity.ReadStatus;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
