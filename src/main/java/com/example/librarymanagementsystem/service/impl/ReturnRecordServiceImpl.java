package com.example.libmanagement.service.impl;

import com.example.libmanagement.entity.Book;
import com.example.libmanagement.entity.BorrowRecord;
import com.example.libmanagement.entity.Borrower;
import com.example.libmanagement.entity.Employee;
import com.example.libmanagement.entity.Invoice;
import com.example.libmanagement.entity.ReturnRecord;
import com.example.libmanagement.enums.BookCondition;
import com.example.libmanagement.enums.BorrowStatus;
import com.example.libmanagement.enums.FineReason;
import com.example.libmanagement.enums.InvoiceStatus;
import com.example.libmanagement.enums.PaymentStatus;
import com.example.libmanagement.enums.ReturnStatus;
import com.example.libmanagement.repository.BookRepository;
import com.example.libmanagement.repository.BorrowRecordRepository;
import com.example.libmanagement.repository.BorrowerRepository;
import com.example.libmanagement.repository.ReturnRecordRepository;
import com.example.libmanagement.service.AuditLogService;
import com.example.libmanagement.service.InvoiceService;
import com.example.libmanagement.service.ReturnRecordService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class ReturnRecordServiceImpl implements ReturnRecordService {

    private static final BigDecimal DAILY_FINE = BigDecimal.valueOf(5000);
    private static final BigDecimal MINOR_DAMAGE_FINE = BigDecimal.valueOf(20000);
    private static final BigDecimal SEVERE_DAMAGE_FINE = BigDecimal.valueOf(50000);

    private final ReturnRecordRepository returnRecordRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final BorrowerRepository borrowerRepository;
    private final AuditLogService auditLogService;
    private final InvoiceService invoiceService;

    public ReturnRecordServiceImpl(ReturnRecordRepository returnRecordRepository,
                                   BorrowRecordRepository borrowRecordRepository,
                                   BookRepository bookRepository,
                                   BorrowerRepository borrowerRepository,
                                   AuditLogService auditLogService,
                                   InvoiceService invoiceService) {
        this.returnRecordRepository = returnRecordRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookRepository = bookRepository;
        this.borrowerRepository = borrowerRepository;
        this.auditLogService = auditLogService;
        this.invoiceService = invoiceService;
    }

    @Override
    public List<ReturnRecord> findAll() {
        return returnRecordRepository.findAll();
    }

    @Override
    public List<ReturnRecord> searchByBorrowerName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return returnRecordRepository.findAll();
        }
        return returnRecordRepository.findByBorrowRecordBorrowerFullNameContainingIgnoreCase(keyword.trim());
    }

    @Override
    public ReturnRecord findById(Long id) {
        return returnRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu trả với id: " + id));
    }

    @Override
    public Optional<ReturnRecord> findByReturnCode(String returnCode) {
        if (returnCode == null || returnCode.trim().isEmpty()) {
            return Optional.empty();
        }
        return returnRecordRepository.findByReturnCode(returnCode.trim());
    }

    @Override
    @Transactional
    public ReturnRecord save(ReturnRecord formData) {
        if (formData.getBorrowRecord() == null || formData.getBorrowRecord().getId() == null) {
            throw new RuntimeException("Vui lòng chọn phiếu mượn.");
        }

        BorrowRecord borrowRecord = borrowRecordRepository.findById(formData.getBorrowRecord().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn."));

        if (returnRecordRepository.existsByBorrowRecordId(borrowRecord.getId())) {
            throw new RuntimeException("Phiếu mượn này đã được trả rồi.");
        }

        if (borrowRecord.getStatus() == BorrowStatus.RETURNED) {
            throw new RuntimeException("Phiếu mượn này đã ở trạng thái đã trả.");
        }

        Employee employee = formData.getEmployee();
        if (employee == null || employee.getId() == null) {
            throw new RuntimeException("Vui lòng chọn nhân viên xử lý.");
        }

        Book book = borrowRecord.getBook();
        Borrower borrower = borrowRecord.getBorrower();

        if (book == null) {
            throw new RuntimeException("Phiếu mượn chưa có thông tin sách.");
        }

        if (borrower == null) {
            throw new RuntimeException("Phiếu mượn chưa có thông tin người mượn.");
        }

        LocalDate returnDate = formData.getReturnDate() != null ? formData.getReturnDate() : LocalDate.now();
        LocalDate dueDate = borrowRecord.getDueDate();

        BookCondition bookCondition = formData.getBookCondition() != null
                ? formData.getBookCondition()
                : BookCondition.INTACT;

        int overdueDays = calculateOverdueDays(dueDate, returnDate);

        BigDecimal lateFine = DAILY_FINE.multiply(BigDecimal.valueOf(overdueDays));
        BigDecimal damageFine = calculateDamageFine(bookCondition);
        BigDecimal totalFine = lateFine.add(damageFine);

        ReturnRecord entity = new ReturnRecord();
        entity.setReturnCode(generateReturnCode());
        entity.setBorrowRecord(borrowRecord);
        entity.setEmployee(employee);
        entity.setReturnDate(returnDate);
        entity.setBookCondition(bookCondition);
        entity.setOverdueDays(overdueDays);
        entity.setFineAmount(totalFine);
        entity.setNote(formData.getNote());

        boolean hasDamage = bookCondition == BookCondition.MINOR_DAMAGE
                || bookCondition == BookCondition.SEVERE_DAMAGE;
        boolean hasLate = overdueDays > 0;

        if (hasDamage) {
            entity.setStatus(ReturnStatus.DAMAGED);
            entity.setFineReason(FineReason.DAMAGED);
            entity.setPaymentStatus(totalFine.compareTo(BigDecimal.ZERO) > 0
                    ? PaymentStatus.UNPAID
                    : PaymentStatus.PAID);
        } else if (hasLate) {
            entity.setStatus(ReturnStatus.LATE);
            entity.setFineReason(FineReason.LATE_RETURN);
            entity.setPaymentStatus(PaymentStatus.UNPAID);

            Integer lateReturnCount = borrower.getLateReturnCount() == null ? 0 : borrower.getLateReturnCount();
            borrower.setLateReturnCount(lateReturnCount + 1);
        } else {
            entity.setStatus(ReturnStatus.RETURNED);
            entity.setFineReason(FineReason.NONE);
            entity.setPaymentStatus(PaymentStatus.PAID);
        }

        Integer availableQuantity = book.getAvailableQuantity() == null ? 0 : book.getAvailableQuantity();
        Integer totalQuantity = book.getTotalQuantity() == null ? 0 : book.getTotalQuantity();

        if (shouldIncreaseAvailableQuantity(bookCondition)) {
            int newAvailableQuantity = availableQuantity + 1;
            if (newAvailableQuantity > totalQuantity) {
                newAvailableQuantity = totalQuantity;
            }
            book.setAvailableQuantity(newAvailableQuantity);
        }

        Integer currentBorrowedCount = borrower.getCurrentBorrowedCount() == null ? 0 : borrower.getCurrentBorrowedCount();
        borrower.setCurrentBorrowedCount(Math.max(currentBorrowedCount - 1, 0));

        borrowRecord.setStatus(BorrowStatus.RETURNED);

        bookRepository.save(book);
        borrowerRepository.save(borrower);
        borrowRecordRepository.save(borrowRecord);

        ReturnRecord saved = returnRecordRepository.save(entity);

        if (saved.getFineAmount() != null && saved.getFineAmount().compareTo(BigDecimal.ZERO) > 0) {
            invoiceService.createFromReturnRecord(saved);
        }

        auditLogService.log(
                "CREATE",
                "RETURN_RECORD",
                "ReturnRecord",
                saved.getId(),
                "Tạo phiếu trả: " + saved.getReturnCode()
        );

        return saved;
    }

    @Override
    @Transactional
    public ReturnRecord update(Long id, ReturnRecord formData) {
        ReturnRecord existing = findById(id);
        Invoice existingInvoice = invoiceService.findByReturnRecord(existing);

        if (existingInvoice != null && existingInvoice.getStatus() == InvoiceStatus.PAID) {
            throw new RuntimeException("Phiếu trả đã có hóa đơn thanh toán, không thể sửa.");
        }

        BorrowRecord borrowRecord = existing.getBorrowRecord();
        if (borrowRecord == null) {
            throw new RuntimeException("Phiếu trả không có phiếu mượn liên kết.");
        }

        Borrower borrower = borrowRecord.getBorrower();
        if (borrower == null) {
            throw new RuntimeException("Phiếu mượn không có người mượn.");
        }

        LocalDate oldReturnDate = existing.getReturnDate();
        int oldOverdueDays = existing.getOverdueDays() == null ? 0 : existing.getOverdueDays();
        BookCondition oldBookCondition = existing.getBookCondition() == null
                ? BookCondition.INTACT
                : existing.getBookCondition();
        ReturnStatus oldStatus = existing.getStatus();

        LocalDate newReturnDate = formData.getReturnDate() != null ? formData.getReturnDate() : oldReturnDate;
        BookCondition newBookCondition = formData.getBookCondition() != null
                ? formData.getBookCondition()
                : oldBookCondition;

        int newOverdueDays = calculateOverdueDays(borrowRecord.getDueDate(), newReturnDate);
        BigDecimal lateFine = DAILY_FINE.multiply(BigDecimal.valueOf(newOverdueDays));
        BigDecimal damageFine = calculateDamageFine(newBookCondition);
        BigDecimal newTotalFine = lateFine.add(damageFine);

        boolean oldHasLate = oldOverdueDays > 0;
        boolean newHasLate = newOverdueDays > 0;
        boolean newHasDamage = newBookCondition == BookCondition.MINOR_DAMAGE
                || newBookCondition == BookCondition.SEVERE_DAMAGE;

        if (!oldHasLate && newHasLate) {
            Integer lateReturnCount = borrower.getLateReturnCount() == null ? 0 : borrower.getLateReturnCount();
            borrower.setLateReturnCount(lateReturnCount + 1);
            borrowerRepository.save(borrower);
        } else if (oldHasLate && !newHasLate) {
            Integer lateReturnCount = borrower.getLateReturnCount() == null ? 0 : borrower.getLateReturnCount();
            borrower.setLateReturnCount(Math.max(lateReturnCount - 1, 0));
            borrowerRepository.save(borrower);
        }

        existing.setReturnDate(newReturnDate);
        existing.setBookCondition(newBookCondition);
        existing.setOverdueDays(newOverdueDays);
        existing.setFineAmount(newTotalFine);
        existing.setNote(formData.getNote());

        if (newHasDamage) {
            existing.setStatus(ReturnStatus.DAMAGED);
            existing.setFineReason(FineReason.DAMAGED);
            existing.setPaymentStatus(newTotalFine.compareTo(BigDecimal.ZERO) > 0
                    ? PaymentStatus.UNPAID
                    : PaymentStatus.PAID);
        } else if (newHasLate) {
            existing.setStatus(ReturnStatus.LATE);
            existing.setFineReason(FineReason.LATE_RETURN);
            existing.setPaymentStatus(PaymentStatus.UNPAID);
        } else {
            existing.setStatus(ReturnStatus.RETURNED);
            existing.setFineReason(FineReason.NONE);
            existing.setPaymentStatus(PaymentStatus.PAID);
        }

        ReturnRecord updated = returnRecordRepository.save(existing);

        if (updated.getFineAmount() != null && updated.getFineAmount().compareTo(BigDecimal.ZERO) > 0) {
            Invoice invoice = invoiceService.findByReturnRecord(updated);
            if (invoice == null) {
                invoiceService.createFromReturnRecord(updated);
            } else if (invoice.getStatus() != InvoiceStatus.PAID) {
                invoice.setAmount(updated.getFineAmount());
                invoice.setNote("Hóa đơn cập nhật từ phiếu trả: " + updated.getReturnCode());
            }
        } else {
            Invoice invoice = invoiceService.findByReturnRecord(updated);
            if (invoice != null && invoice.getStatus() != InvoiceStatus.PAID) {
                invoiceService.cancelInvoice(invoice.getId(), "Hủy vì phiếu trả không còn tiền phạt.");
            }
        }

        auditLogService.log(
                "UPDATE",
                "RETURN_RECORD",
                "ReturnRecord",
                updated.getId(),
                "Cập nhật phiếu trả: " + updated.getReturnCode()
        );

        return updated;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ReturnRecord existing = findById(id);
        Invoice invoice = invoiceService.findByReturnRecord(existing);

        if (invoice != null && invoice.getStatus() == InvoiceStatus.PAID) {
            throw new RuntimeException("Phiếu trả đã có hóa đơn thanh toán, không thể xóa.");
        }

        String returnCode = existing.getReturnCode();
        Long returnId = existing.getId();

        BorrowRecord borrowRecord = existing.getBorrowRecord();
        if (borrowRecord == null) {
            throw new RuntimeException("Phiếu trả không có phiếu mượn liên kết.");
        }

        Book book = borrowRecord.getBook();
        Borrower borrower = borrowRecord.getBorrower();

        if (book == null || borrower == null) {
            throw new RuntimeException("Phiếu mượn thiếu thông tin sách hoặc người mượn.");
        }

        Integer availableQuantity = book.getAvailableQuantity() == null ? 0 : book.getAvailableQuantity();

        if (shouldIncreaseAvailableQuantity(existing.getBookCondition())) {
            book.setAvailableQuantity(Math.max(availableQuantity - 1, 0));
        }

        Integer currentBorrowedCount = borrower.getCurrentBorrowedCount() == null ? 0 : borrower.getCurrentBorrowedCount();
        borrower.setCurrentBorrowedCount(currentBorrowedCount + 1);

        if (existing.getOverdueDays() != null && existing.getOverdueDays() > 0) {
            Integer lateReturnCount = borrower.getLateReturnCount() == null ? 0 : borrower.getLateReturnCount();
            borrower.setLateReturnCount(Math.max(lateReturnCount - 1, 0));
        }

        borrowRecord.setStatus(BorrowStatus.BORROWED);

        bookRepository.save(book);
        borrowerRepository.save(borrower);
        borrowRecordRepository.save(borrowRecord);

        if (invoice != null && invoice.getStatus() != InvoiceStatus.PAID) {
            invoiceService.cancelInvoice(invoice.getId(), "Hủy vì xóa phiếu trả: " + returnCode);
        }

        returnRecordRepository.delete(existing);

        auditLogService.log(
                "DELETE",
                "RETURN_RECORD",
                "ReturnRecord",
                returnId,
                "Xóa phiếu trả: " + returnCode
        );
    }

    private int calculateOverdueDays(LocalDate dueDate, LocalDate returnDate) {
        if (dueDate == null || returnDate == null) {
            return 0;
        }
        if (!returnDate.isAfter(dueDate)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(dueDate, returnDate);
    }

    private BigDecimal calculateDamageFine(BookCondition bookCondition) {
        if (bookCondition == BookCondition.MINOR_DAMAGE) {
            return MINOR_DAMAGE_FINE;
        }
        if (bookCondition == BookCondition.SEVERE_DAMAGE) {
            return SEVERE_DAMAGE_FINE;
        }
        return BigDecimal.ZERO;
    }

    private boolean shouldIncreaseAvailableQuantity(BookCondition bookCondition) {
        return bookCondition == BookCondition.INTACT
                || bookCondition == BookCondition.MINOR_DAMAGE;
    }

    private String generateReturnCode() {
        return "TRA-" + System.currentTimeMillis();
    }
}