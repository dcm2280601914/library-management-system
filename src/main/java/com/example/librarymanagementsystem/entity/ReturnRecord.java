package com.example.libmanagement.entity;

import com.example.libmanagement.enums.BookCondition;
import com.example.libmanagement.enums.FineReason;
import com.example.libmanagement.enums.PaymentStatus;
import com.example.libmanagement.enums.ReturnStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "return_records")
public class ReturnRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_code", nullable = false, unique = true, length = 30)
    private String returnCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_record_id", nullable = false)
    private BorrowRecord borrowRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReturnStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_condition", nullable = false, length = 30)
    private BookCondition bookCondition = BookCondition.INTACT;

    @Column(name = "overdue_days", nullable = false)
    private Integer overdueDays = 0;

    @Column(name = "fine_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "fine_reason", nullable = false, length = 30)
    private FineReason fineReason = FineReason.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PAID;

    @Column(length = 255)
    private String note;

    public ReturnRecord() {
    }

    @PrePersist
    public void prePersist() {
        if (returnDate == null) {
            returnDate = LocalDate.now();
        }
        if (bookCondition == null) {
            bookCondition = BookCondition.INTACT;
        }
        if (overdueDays == null) {
            overdueDays = 0;
        }
        if (fineAmount == null) {
            fineAmount = BigDecimal.ZERO;
        }
        if (fineReason == null) {
            fineReason = FineReason.NONE;
        }
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PAID;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public BorrowRecord getBorrowRecord() {
        return borrowRecord;
    }

    public void setBorrowRecord(BorrowRecord borrowRecord) {
        this.borrowRecord = borrowRecord;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public ReturnStatus getStatus() {
        return status;
    }

    public void setStatus(ReturnStatus status) {
        this.status = status;
    }

    public BookCondition getBookCondition() {
        return bookCondition;
    }

    public void setBookCondition(BookCondition bookCondition) {
        this.bookCondition = bookCondition;
    }

    public Integer getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(Integer overdueDays) {
        this.overdueDays = overdueDays;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }

    public FineReason getFineReason() {
        return fineReason;
    }

    public void setFineReason(FineReason fineReason) {
        this.fineReason = fineReason;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}