package com.example.libmanagement.repository;

import com.example.libmanagement.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, Long id);

    List<Book> findDistinctByAuthorIsNotNullOrderByAuthorAsc();

    List<Book> findDistinctByPublicationYearIsNotNullOrderByPublicationYearDesc();
}