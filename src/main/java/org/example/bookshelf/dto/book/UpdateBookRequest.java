package org.example.bookshelf.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.bookshelf.entity.ReadStatus;

@Data
public class UpdateBookRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 255)
    private String title;

    @NotBlank(message = "Author is required")
    @Size(min = 2, max = 255)
    private String author;

    @NotBlank(message = "Genre is required")
    @Size(min = 2, max = 255)
    private String genre;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "WANT_TO_READ|READING|READ", message = "Status must be WANT_TO_READ or READING OR READ")
    private ReadStatus status;

    @Size(max = 10)
    private Integer rating;
}
