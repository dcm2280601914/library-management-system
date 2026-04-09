package com.example.libmanagement.service;

import com.example.libmanagement.entity.Book;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookService {
    List<Book> findAll();
    Book findById(Long id);
    Book save(Book book);
    Book update(Long id, Book book);
    void delete(Long id);

    Page<Book> searchBooks(String keyword,
                           Long categoryId,
                           String author,
                           Integer publicationYear,
                           int page,
                           int size,
                           String sortField,
                           String sortDir);

    List<String> getAllAuthors();
    List<Integer> getAllPublicationYears();
}