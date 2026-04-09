package com.example.libmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "author", length = 100)
    private String author;

    @Column(name = "publisher", length = 100)
    private String publisher;

    @Column(name = "publication_year", nullable = false)
    private Integer publicationYear;

    @Column(name = "isbn", unique = true, length = 30)
    private String isbn;

    @Column(name = "barcode", unique = true, length = 50)
    private String barcode;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "cover_image", length = 1000)
    private String coverImage;

    @Column(name = "import_date")
    private LocalDate importDate;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity = 0;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 0;

    @Column(length = 500)
    private String description;

    public Book() {
    }

    public Book(String title, Category category, String author, String publisher,
                Integer publicationYear, String isbn, String barcode, String location,
                String coverImage, LocalDate importDate, Integer totalQuantity,
                Integer availableQuantity, String description) {
        this.title = title;
        this.category = category;
        this.author = author;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.isbn = isbn;
        this.barcode = barcode;
        this.location = location;
        this.coverImage = coverImage;
        this.importDate = importDate;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    // THÊM METHOD NÀY
    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public LocalDate getImportDate() {
        return importDate;
    }

    public void setImportDate(LocalDate importDate) {
        this.importDate = importDate;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}