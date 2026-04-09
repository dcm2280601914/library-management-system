package com.example.libmanagement.entity;

import com.example.libmanagement.enums.InvoiceStatus;
import com.example.libmanagement.enums.PaymentMethod;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_code", nullable = false, unique = true, length = 50)
    private String invoiceCode;

    @OneToOne
    @JoinColumn(name = "return_record_id", unique = true)
    private ReturnRecord returnRecord;

    @ManyToOne
    @JoinColumn(name = "borrower_id")
    private Borrower borrower;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "paid_by", length = 100)
    private String paidBy;

    public Invoice() {
    }

    public Long getId() {
        return id;
    }

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
    }

    public ReturnRecord getReturnRecord() {
        return returnRecord;
    }

    public void setReturnRecord(ReturnRecord returnRecord) {
        this.returnRecord = returnRecord;
    }

    public Borrower getBorrower() {
        return borrower;
    }

    public void setBorrower(Borrower borrower) {
        this.borrower = borrower;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(String paidBy) {
        this.paidBy = paidBy;
    }
}