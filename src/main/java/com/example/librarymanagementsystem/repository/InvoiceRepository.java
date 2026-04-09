package com.example.librarymanagementsystem.repository;

import com.example.librarymanagementsystem.entity.Invoice;
import com.example.librarymanagementsystem.entity.ReturnRecord;
import com.example.librarymanagementsystem.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    boolean existsByInvoiceCode(String invoiceCode);

    Optional<Invoice> findByInvoiceCode(String invoiceCode);

    boolean existsByReturnRecord(ReturnRecord returnRecord);

    Optional<Invoice> findByReturnRecord(ReturnRecord returnRecord);

    List<Invoice> findByStatus(InvoiceStatus status);
}