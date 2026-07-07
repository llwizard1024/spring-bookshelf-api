package org.example.bookshelf.dto.book;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.example.bookshelf.entity.ReadStatus;

@Data
public class PatchBookRequest {
    @Size(min = 5, max = 255)
    private String title;

    @Size(min = 2, max = 255)
    private String author;

    @Size(min = 2, max = 255)
    private String genre;

    private ReadStatus status;

    @Min(1)
    @Max(10)
    private Integer rating;
}
