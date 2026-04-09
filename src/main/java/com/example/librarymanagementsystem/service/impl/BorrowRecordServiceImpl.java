package com.example.libmanagement.service.impl;

import com.example.libmanagement.entity.Book;
import com.example.libmanagement.entity.BorrowRecord;
import com.example.libmanagement.entity.Borrower;
import com.example.libmanagement.entity.Employee;
import com.example.libmanagement.enums.BorrowStatus;
import com.example.libmanagement.repository.BookRepository;
import com.example.libmanagement.repository.BorrowRecordRepository;
import com.example.libmanagement.repository.BorrowerRepository;
import com.example.libmanagement.repository.EmployeeRepository;
import com.example.libmanagement.service.BorrowRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BorrowRecordServiceImpl implements BorrowRecordService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BorrowerRepository borrowerRepository;
    private final EmployeeRepository employeeRepository;
    private final BookRepository bookRepository;

    public BorrowRecordServiceImpl(BorrowRecordRepository borrowRecordRepository,
                                   BorrowerRepository borrowerRepository,
                                   EmployeeRepository employeeRepository,
                                   BookRepository bookRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.borrowerRepository = borrowerRepository;
        this.employeeRepository = employeeRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public List<BorrowRecord> findAll() {
        return borrowRecordRepository.findAll();
    }

    @Override
    public BorrowRecord findById(Long id) {
        return borrowRecordRepository.findById(id).orElse(null);
    }

    @Override
    public BorrowRecord save(BorrowRecord borrowRecord) {
        if (borrowRecord.getBorrower() == null || borrowRecord.getBorrower().getId() == null) {
            throw new RuntimeException("Người mượn không hợp lệ");
        }
        if (borrowRecord.getEmployee() == null || borrowRecord.getEmployee().getId() == null) {
            throw new RuntimeException("Nhân viên không hợp lệ");
        }
        if (borrowRecord.getBook() == null || borrowRecord.getBook().getId() == null) {
            throw new RuntimeException("Sách không hợp lệ");
        }

        Borrower borrower = borrowerRepository.findById(borrowRecord.getBorrower().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người mượn"));

        Employee employee = employeeRepository.findById(borrowRecord.getEmployee().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        Book book = bookRepository.findById(borrowRecord.getBook().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));

        if (!borrower.isAllowedToBorrow()) {
            throw new RuntimeException("Người mượn hiện không được phép mượn sách");
        }

        Integer currentBorrowedCount = borrower.getCurrentBorrowedCount() == null ? 0 : borrower.getCurrentBorrowedCount();
        Integer maxBorrowLimit = borrower.getMaxBorrowLimit() == null ? 0 : borrower.getMaxBorrowLimit();

        if (currentBorrowedCount >= maxBorrowLimit) {
            throw new RuntimeException("Người mượn đã đạt giới hạn mượn sách");
        }

        if (book.getAvailableQuantity() == null || book.getAvailableQuantity() <= 0) {
            throw new RuntimeException("Sách đã hết trong kho");
        }

        borrowRecord.setBorrower(borrower);
        borrowRecord.setEmployee(employee);
        borrowRecord.setBook(book);

        if (borrowRecord.getBorrowDate() == null) {
            borrowRecord.setBorrowDate(LocalDate.now());
        }

        if (borrowRecord.getDueDate() == null) {
            borrowRecord.setDueDate(borrowRecord.getBorrowDate().plusDays(7));
        }

        if (borrowRecord.getDueDate().isBefore(borrowRecord.getBorrowDate())) {
            throw new RuntimeException("Hạn trả phải sau hoặc bằng ngày mượn");
        }

        borrowRecord.setStatus(BorrowStatus.BORROWED);

        if (!StringUtils.hasText(borrowRecord.getBorrowCode())) {
            borrowRecord.setBorrowCode("BR-" + System.currentTimeMillis());
        } else {
            borrowRecord.setBorrowCode(borrowRecord.getBorrowCode().trim());
        }

        if (borrowRecordRepository.findByBorrowCode(borrowRecord.getBorrowCode()).isPresent()) {
            throw new RuntimeException("Mã phiếu mượn đã tồn tại");
        }

        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        borrower.setCurrentBorrowedCount(currentBorrowedCount + 1);

        bookRepository.save(book);
        borrowerRepository.save(borrower);

        return borrowRecordRepository.save(borrowRecord);
    }

    @Override
    public BorrowRecord update(Long id, BorrowRecord borrowRecord) {
        BorrowRecord existing = findById(id);
        if (existing == null) {
            return null;
        }

        if (borrowRecord.getBorrower() == null || borrowRecord.getBorrower().getId() == null) {
            throw new RuntimeException("Người mượn không hợp lệ");
        }
        if (borrowRecord.getEmployee() == null || borrowRecord.getEmployee().getId() == null) {
            throw new RuntimeException("Nhân viên không hợp lệ");
        }
        if (borrowRecord.getBook() == null || borrowRecord.getBook().getId() == null) {
            throw new RuntimeException("Sách không hợp lệ");
        }

        Borrower oldBorrower = existing.getBorrower();
        Book oldBook = existing.getBook();
        BorrowStatus oldStatus = existing.getStatus();

        Borrower newBorrower = borrowerRepository.findById(borrowRecord.getBorrower().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người mượn"));

        Employee employee = employeeRepository.findById(borrowRecord.getEmployee().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        Book newBook = bookRepository.findById(borrowRecord.getBook().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));

        if (borrowRecord.getDueDate() != null && borrowRecord.getBorrowDate() != null
                && borrowRecord.getDueDate().isBefore(borrowRecord.getBorrowDate())) {
            throw new RuntimeException("Hạn trả phải sau hoặc bằng ngày mượn");
        }

        if (StringUtils.hasText(borrowRecord.getBorrowCode())) {
            Optional<BorrowRecord> sameCode = borrowRecordRepository.findByBorrowCode(borrowRecord.getBorrowCode().trim());
            if (sameCode.isPresent() && !sameCode.get().getId().equals(id)) {
                throw new RuntimeException("Mã phiếu mượn đã tồn tại");
            }
            existing.setBorrowCode(borrowRecord.getBorrowCode().trim());
        }

        BorrowStatus newStatus = borrowRecord.getStatus() == null ? BorrowStatus.BORROWED : borrowRecord.getStatus();

        boolean oldWasBorrowed = oldStatus == BorrowStatus.BORROWED;
        boolean newIsBorrowed = newStatus == BorrowStatus.BORROWED;

        boolean borrowerChanged = oldBorrower != null && newBorrower != null && !oldBorrower.getId().equals(newBorrower.getId());
        boolean bookChanged = oldBook != null && newBook != null && !oldBook.getId().equals(newBook.getId());

        if ((borrowerChanged || bookChanged || oldWasBorrowed != newIsBorrowed) && newIsBorrowed) {
            if (!newBorrower.isAllowedToBorrow()) {
                throw new RuntimeException("Người mượn hiện không được phép mượn sách");
            }

            int newCurrent = newBorrower.getCurrentBorrowedCount() == null ? 0 : newBorrower.getCurrentBorrowedCount();
            int newLimit = newBorrower.getMaxBorrowLimit() == null ? 0 : newBorrower.getMaxBorrowLimit();

            if (borrowerChanged || !oldWasBorrowed) {
                if (newCurrent >= newLimit) {
                    throw new RuntimeException("Người mượn đã đạt giới hạn mượn sách");
                }
            }

            if (bookChanged || !oldWasBorrowed) {
                if (newBook.getAvailableQuantity() == null || newBook.getAvailableQuantity() <= 0) {
                    throw new RuntimeException("Sách đã hết trong kho");
                }
            }
        }

        if (oldWasBorrowed) {
            if (oldBook != null) {
                int oldAvailable = oldBook.getAvailableQuantity() == null ? 0 : oldBook.getAvailableQuantity();
                oldBook.setAvailableQuantity(oldAvailable + 1);
                bookRepository.save(oldBook);
            }

            if (oldBorrower != null) {
                int oldCurrent = oldBorrower.getCurrentBorrowedCount() == null ? 0 : oldBorrower.getCurrentBorrowedCount();
                oldBorrower.setCurrentBorrowedCount(Math.max(oldCurrent - 1, 0));
                borrowerRepository.save(oldBorrower);
            }
        }

        if (newIsBorrowed) {
            int newAvailable = newBook.getAvailableQuantity() == null ? 0 : newBook.getAvailableQuantity();
            newBook.setAvailableQuantity(newAvailable - 1);
            bookRepository.save(newBook);

            int newCurrent = newBorrower.getCurrentBorrowedCount() == null ? 0 : newBorrower.getCurrentBorrowedCount();
            newBorrower.setCurrentBorrowedCount(newCurrent + 1);
            borrowerRepository.save(newBorrower);
        }

        existing.setBorrower(newBorrower);
        existing.setEmployee(employee);
        existing.setBook(newBook);
        existing.setBorrowDate(borrowRecord.getBorrowDate());
        existing.setDueDate(borrowRecord.getDueDate());
        existing.setStatus(newStatus);
        existing.setNote(borrowRecord.getNote());

        return borrowRecordRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        BorrowRecord existing = findById(id);
        if (existing == null) {
            return;
        }

        if (existing.getStatus() == BorrowStatus.BORROWED) {
            Book book = existing.getBook();
            Borrower borrower = existing.getBorrower();

            if (book != null) {
                int available = book.getAvailableQuantity() == null ? 0 : book.getAvailableQuantity();
                book.setAvailableQuantity(available + 1);
                bookRepository.save(book);
            }

            if (borrower != null) {
                int current = borrower.getCurrentBorrowedCount() == null ? 0 : borrower.getCurrentBorrowedCount();
                borrower.setCurrentBorrowedCount(Math.max(current - 1, 0));
                borrowerRepository.save(borrower);
            }
        }

        borrowRecordRepository.delete(existing);
    }

    @Override
    public Optional<BorrowRecord> findByBorrowCode(String borrowCode) {
        return borrowRecordRepository.findByBorrowCode(borrowCode);
    }

    @Override
    public List<BorrowRecord> searchByBorrowerName(String keyword) {
        return borrowRecordRepository.findByBorrowerFullNameContainingIgnoreCase(keyword);
    }
}