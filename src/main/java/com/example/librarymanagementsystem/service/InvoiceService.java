package com.example.libmanagement.service;

import com.example.libmanagement.entity.Invoice;
import com.example.libmanagement.entity.ReturnRecord;
import com.example.libmanagement.enums.PaymentMethod;

import java.util.List;

public interface InvoiceService {

    List<Invoice> findAll();

    Invoice findById(Long id);

    Invoice createFromReturnRecord(ReturnRecord returnRecord);

    Invoice payInvoice(Long id, PaymentMethod paymentMethod, String note);

    Invoice cancelInvoice(Long id, String note);

    Invoice findByReturnRecord(ReturnRecord returnRecord);
}