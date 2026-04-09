package com.example.libmanagement.specification;

import com.example.libmanagement.entity.Book;
import com.example.libmanagement.entity.Category;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {

    public static Specification<Book> keywordLike(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return null;
            }

            String value = "%" + keyword.trim().toLowerCase() + "%";
            Join<Book, Category> categoryJoin = root.join("category", jakarta.persistence.criteria.JoinType.LEFT);

            return cb.or(
                    cb.like(cb.lower(root.get("title")), value),
                    cb.like(cb.lower(root.get("author")), value),
                    cb.like(cb.lower(root.get("publisher")), value),
                    cb.like(cb.lower(root.get("isbn")), value),
                    cb.like(cb.lower(root.get("barcode")), value),
                    cb.like(cb.lower(root.get("location")), value),
                    cb.like(cb.lower(root.get("description")), value),
                    cb.like(cb.lower(categoryJoin.get("name")), value)
            );
        };
    }

    public static Specification<Book> hasCategory(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) {
                return null;
            }
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    public static Specification<Book> hasAuthor(String author) {
        return (root, query, cb) -> {
            if (author == null || author.trim().isEmpty()) {
                return null;
            }
            return cb.equal(cb.lower(root.get("author")), author.trim().toLowerCase());
        };
    }

    public static Specification<Book> hasPublicationYear(Integer year) {
        return (root, query, cb) -> {
            if (year == null) {
                return null;
            }
            return cb.equal(root.get("publicationYear"), year);
        };
    }
}
