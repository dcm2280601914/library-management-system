package com.example.libmanagement.service.impl;

import com.example.libmanagement.entity.Book;
import com.example.libmanagement.entity.Category;
import com.example.libmanagement.repository.BookRepository;
import com.example.libmanagement.repository.CategoryRepository;
import com.example.libmanagement.service.AuditLogService;
import com.example.libmanagement.service.BookService;
import com.example.libmanagement.specification.BookSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final AuditLogService auditLogService;

    public BookServiceImpl(BookRepository bookRepository,
                           CategoryRepository categoryRepository,
                           AuditLogService auditLogService) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    public Book findById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    @Override
    public Book save(Book book) {
        if (book.getCategory() != null && book.getCategory().getId() != null) {
            Category category = categoryRepository.findById(book.getCategory().getId()).orElse(null);
            book.setCategory(category);
        } else {
            book.setCategory(null);
        }

        if (book.getBarcode() != null && !book.getBarcode().trim().isEmpty()) {
            String barcode = book.getBarcode().trim();
            boolean exists = bookRepository.existsByBarcode(barcode);
            if (exists) {
                throw new RuntimeException("Mã barcode đã tồn tại: " + barcode);
            }
            book.setBarcode(barcode);
        }

        Book savedBook = bookRepository.save(book);

        auditLogService.log(
                "CREATE",
                "BOOK",
                "Book",
                savedBook.getId(),
                "Thêm sách: " + savedBook.getTitle()
        );

        return savedBook;
    }

    @Override
    public Book update(Long id, Book book) {
        Book existingBook = findById(id);
        if (existingBook == null) {
            return null;
        }

        if (book.getBarcode() != null && !book.getBarcode().trim().isEmpty()) {
            String barcode = book.getBarcode().trim();
            boolean exists = bookRepository.existsByBarcodeAndIdNot(barcode, id);
            if (exists) {
                throw new RuntimeException("Mã barcode đã tồn tại: " + barcode);
            }
            existingBook.setBarcode(barcode);
        } else {
            existingBook.setBarcode(book.getBarcode());
        }

        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setPublisher(book.getPublisher());
        existingBook.setPublicationYear(book.getPublicationYear());
        existingBook.setIsbn(book.getIsbn());
        existingBook.setLocation(book.getLocation());
        existingBook.setCoverImage(book.getCoverImage());
        existingBook.setImportDate(book.getImportDate());
        existingBook.setTotalQuantity(book.getTotalQuantity());
        existingBook.setAvailableQuantity(book.getAvailableQuantity());
        existingBook.setDescription(book.getDescription());

        if (book.getCategory() != null && book.getCategory().getId() != null) {
            Category category = categoryRepository.findById(book.getCategory().getId()).orElse(null);
            existingBook.setCategory(category);
        } else {
            existingBook.setCategory(null);
        }

        Book updatedBook = bookRepository.save(existingBook);

        auditLogService.log(
                "UPDATE",
                "BOOK",
                "Book",
                updatedBook.getId(),
                "Cập nhật sách: " + updatedBook.getTitle()
        );

        return updatedBook;
    }

    @Override
    public void delete(Long id) {
        Book existingBook = findById(id);
        if (existingBook == null) {
            return;
        }

        String bookTitle = existingBook.getTitle();
        Long bookId = existingBook.getId();

        bookRepository.delete(existingBook);

        auditLogService.log(
                "DELETE",
                "BOOK",
                "Book",
                bookId,
                "Xóa sách: " + bookTitle
        );
    }

    @Override
    public Page<Book> searchBooks(String keyword,
                                  Long categoryId,
                                  String author,
                                  Integer publicationYear,
                                  int page,
                                  int size,
                                  String sortField,
                                  String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Book> spec = Specification
                .where(BookSpecification.keywordLike(keyword))
                .and(BookSpecification.hasCategory(categoryId))
                .and(BookSpecification.hasAuthor(author))
                .and(BookSpecification.hasPublicationYear(publicationYear));

        return bookRepository.findAll(spec, pageable);
    }

    @Override
    public List<String> getAllAuthors() {
        return bookRepository.findDistinctByAuthorIsNotNullOrderByAuthorAsc()
                .stream()
                .map(Book::getAuthor)
                .filter(author -> author != null && !author.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Integer> getAllPublicationYears() {
        return bookRepository.findDistinctByPublicationYearIsNotNullOrderByPublicationYearDesc()
                .stream()
                .map(Book::getPublicationYear)
                .distinct()
                .collect(Collectors.toList());
    }
}