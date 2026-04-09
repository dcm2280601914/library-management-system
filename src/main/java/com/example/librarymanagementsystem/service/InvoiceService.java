package com.example.librarymanagementsystem.service;

import com.example.librarymanagementsystem.entity.Invoice;
import com.example.librarymanagementsystem.entity.ReturnRecord;
import com.example.librarymanagementsystem.enums.PaymentMethod;

import java.util.List;

public interface InvoiceService {

    List<Invoice> findAll();

    Invoice findById(Long id);

    Invoice createFromReturnRecord(ReturnRecord returnRecord);

    Invoice payInvoice(Long id, PaymentMethod paymentMethod, String note);

    Invoice cancelInvoice(Long id, String note);

    Invoice findByReturnRecord(ReturnRecord returnRecord);
}