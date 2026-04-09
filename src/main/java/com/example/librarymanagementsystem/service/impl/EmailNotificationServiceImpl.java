package com.example.libmanagement.service.impl;

import com.example.libmanagement.entity.BorrowRecord;
import com.example.libmanagement.entity.Borrower;
import com.example.libmanagement.repository.BorrowRecordRepository;
import com.example.libmanagement.repository.BorrowerRepository;
import com.example.libmanagement.repository.ReturnRecordRepository;
import com.example.libmanagement.service.EmailNotificationService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final JavaMailSender mailSender;
    private final BorrowerRepository borrowerRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final ReturnRecordRepository returnRecordRepository;

    public EmailNotificationServiceImpl(JavaMailSender mailSender,
                                        BorrowerRepository borrowerRepository,
                                        BorrowRecordRepository borrowRecordRepository,
                                        ReturnRecordRepository returnRecordRepository) {
        this.mailSender = mailSender;
        this.borrowerRepository = borrowerRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.returnRecordRepository = returnRecordRepository;
    }

    @Override
    public void sendBorrowReminderToBorrower(Long borrowerId) {
        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người mượn."));

        if (!StringUtils.hasText(borrower.getEmail())) {
            throw new IllegalArgumentException("Người mượn chưa có email để gửi thông báo.");
        }

        List<BorrowRecord> records = borrowRecordRepository.findByBorrowerIdOrderByBorrowDateDesc(borrowerId);

        StringBuilder content = new StringBuilder();
        content.append("Xin chào ").append(borrower.getFullName()).append(",\n\n");
        content.append("Đây là email nhắc hạn trả sách từ thư viện.\n\n");

        int count = 0;

        for (BorrowRecord record : records) {
            boolean returned = returnRecordRepository.existsByBorrowRecordId(record.getId());
            if (returned) {
                continue;
            }

            if (record.getDueDate() == null) {
                continue;
            }

            long days = ChronoUnit.DAYS.between(LocalDate.now(), record.getDueDate());

            content.append("- Sách: ")
                    .append(record.getBook() != null ? record.getBook().getTitle() : "Không xác định")
                    .append("\n");
            content.append("  Mã phiếu mượn: ").append(record.getBorrowCode()).append("\n");
            content.append("  Ngày mượn: ").append(record.getBorrowDate()).append("\n");
            content.append("  Hạn trả: ").append(record.getDueDate()).append("\n");

            if (days < 0) {
                content.append("  Trạng thái: Đã quá hạn ").append(Math.abs(days)).append(" ngày\n\n");
            } else if (days <= 3) {
                content.append("  Trạng thái: Sắp đến hạn, còn ").append(days).append(" ngày\n\n");
            } else {
                content.append("  Trạng thái: Đang mượn\n\n");
            }

            count++;
        }

        if (count == 0) {
            throw new IllegalArgumentException("Người mượn hiện không có sách cần nhắc hạn.");
        }

        content.append("Vui lòng trả sách đúng hạn để tránh phát sinh tiền phạt và vi phạm.\n\n");
        content.append("Trân trọng,\n");
        content.append("Thư viện");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(borrower.getEmail());
        message.setSubject("Thông báo nhắc hạn trả sách");
        message.setText(content.toString());

        mailSender.send(message);
    }
}