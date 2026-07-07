package org.example.bookshelf.repository;

import org.example.bookshelf.entity.Book;
import org.example.bookshelf.entity.ReadStatus;
import org.springframework.data.jpa.domain.Specification;

public final class BookSpecifications {
    private BookSpecifications() {
    }

    public static Specification<Book> hasStatus(ReadStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Book> authorContains(String author) {
        return (root, query, criteriaBuilder) -> {
            if (author == null || author.isBlank()) {
                return null;
            }
            String pattern = "%" + author.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), pattern);
        };
    }

    public static Specification<Book> genreContains(String genre) {
        return (root, query, criteriaBuilder) -> {
            if (genre == null || genre.isBlank()) {
                return null;
            }
            String pattern = "%" + genre.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("genre")), pattern);
        };
    }
}
