package com.example.libmanagement.service.impl;

import com.example.libmanagement.entity.Invoice;
import com.example.libmanagement.entity.ReturnRecord;
import com.example.libmanagement.enums.InvoiceStatus;
import com.example.libmanagement.enums.PaymentMethod;
import com.example.libmanagement.enums.PaymentStatus;
import com.example.libmanagement.repository.InvoiceRepository;
import com.example.libmanagement.repository.ReturnRecordRepository;
import com.example.libmanagement.service.AuditLogService;
import com.example.libmanagement.service.InvoiceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ReturnRecordRepository returnRecordRepository;
    private final AuditLogService auditLogService;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
                              ReturnRecordRepository returnRecordRepository,
                              AuditLogService auditLogService) {
        this.invoiceRepository = invoiceRepository;
        this.returnRecordRepository = returnRecordRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
    }

    @Override
    public Invoice findById(Long id) {
        return invoiceRepository.findById(id).orElse(null);
    }

    @Override
    public Invoice findByReturnRecord(ReturnRecord returnRecord) {
        return invoiceRepository.findByReturnRecord(returnRecord).orElse(null);
    }

    @Override
    @Transactional
    public Invoice createFromReturnRecord(ReturnRecord returnRecord) {
        if (returnRecord == null) {
            return null;
        }

        BigDecimal fineAmount = returnRecord.getFineAmount() == null
                ? BigDecimal.ZERO
                : returnRecord.getFineAmount();

        if (fineAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        if (invoiceRepository.existsByReturnRecord(returnRecord)) {
            return invoiceRepository.findByReturnRecord(returnRecord).orElse(null);
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceCode(generateInvoiceCode());
        invoice.setReturnRecord(returnRecord);

        if (returnRecord.getBorrowRecord() != null) {
            invoice.setBorrower(returnRecord.getBorrowRecord().getBorrower());
        }

        invoice.setAmount(fineAmount);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setNote("Hóa đơn tạo từ phiếu trả: " + returnRecord.getReturnCode());

        Invoice saved = invoiceRepository.save(invoice);

        auditLogService.log(
                "CREATE",
                "INVOICE",
                "Invoice",
                saved.getId(),
                "Tạo hóa đơn: " + saved.getInvoiceCode()
        );

        return saved;
    }

    @Override
    @Transactional
    public Invoice payInvoice(Long id, PaymentMethod paymentMethod, String note) {
        Invoice invoice = findById(id);
        if (invoice == null) {
            return null;
        }

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            return invoice;
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new RuntimeException("Hóa đơn đã bị hủy, không thể thanh toán");
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setPaidAmount(invoice.getAmount());
        invoice.setPaidAt(LocalDateTime.now());

        if (note != null && !note.trim().isEmpty()) {
            invoice.setNote(note.trim());
        }

        ReturnRecord returnRecord = invoice.getReturnRecord();
        if (returnRecord != null) {
            returnRecord.setPaymentStatus(PaymentStatus.PAID);
            returnRecordRepository.save(returnRecord);
        }

        Invoice saved = invoiceRepository.save(invoice);

        auditLogService.log(
                "UPDATE",
                "INVOICE",
                "Invoice",
                saved.getId(),
                "Thanh toán hóa đơn: " + saved.getInvoiceCode()
        );

        return saved;
    }

    @Override
    @Transactional
    public Invoice cancelInvoice(Long id, String note) {
        Invoice invoice = findById(id);
        if (invoice == null) {
            return null;
        }

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new RuntimeException("Hóa đơn đã thanh toán, không thể hủy");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);

        if (note != null && !note.trim().isEmpty()) {
            invoice.setNote(note.trim());
        }

        Invoice saved = invoiceRepository.save(invoice);

        auditLogService.log(
                "UPDATE",
                "INVOICE",
                "Invoice",
                saved.getId(),
                "Hủy hóa đơn: " + saved.getInvoiceCode()
        );

        return saved;
    }

    private String generateInvoiceCode() {
        String code;
        do {
            code = "INV-" + System.currentTimeMillis();
        } while (invoiceRepository.existsByInvoiceCode(code));
        return code;
    }
}