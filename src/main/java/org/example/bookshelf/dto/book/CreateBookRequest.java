package org.example.bookshelf.dto.book;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.example.bookshelf.entity.ReadStatus;

@Data
public class CreateBookRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 255)
    private String title;

    @NotBlank(message = "Author is required")
    @Size(min = 2, max = 255)
    private String author;

    @NotBlank(message = "Genre is required")
    @Size(min = 2, max = 255)
    private String genre;

    @NotNull
    private ReadStatus status;

    @Min(1)
    @Max(10)
    private Integer rating;
}
