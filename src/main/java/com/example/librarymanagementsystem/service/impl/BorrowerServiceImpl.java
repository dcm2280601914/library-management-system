package com.example.libmanagement.service.impl;

import com.example.libmanagement.dto.BorrowHistoryDto;
import com.example.libmanagement.entity.BorrowRecord;
import com.example.libmanagement.entity.Borrower;
import com.example.libmanagement.entity.ReturnRecord;
import com.example.libmanagement.enums.BorrowerStatus;
import com.example.libmanagement.enums.MembershipLevel;
import com.example.libmanagement.repository.BorrowRecordRepository;
import com.example.libmanagement.repository.BorrowerRepository;
import com.example.libmanagement.repository.ReturnRecordRepository;
import com.example.libmanagement.service.BorrowerService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class BorrowerServiceImpl implements BorrowerService {

    private final BorrowerRepository borrowerRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final ReturnRecordRepository returnRecordRepository;

    public BorrowerServiceImpl(BorrowerRepository borrowerRepository,
                               BorrowRecordRepository borrowRecordRepository,
                               ReturnRecordRepository returnRecordRepository) {
        this.borrowerRepository = borrowerRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.returnRecordRepository = returnRecordRepository;
    }

    @Override
    public List<Borrower> findAll() {
        return borrowerRepository.findAll();
    }

    @Override
    public Page<Borrower> searchBorrowers(String keyword,
                                          BorrowerStatus status,
                                          MembershipLevel membershipLevel,
                                          Boolean active,
                                          int page,
                                          int size,
                                          String sortField,
                                          String sortDir) {

        String safeSortField = resolveSortField(sortField);
        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(safeSortField).descending()
                : Sort.by(safeSortField).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Borrower> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";

                Predicate fullNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("fullName")), likeKeyword
                );
                Predicate emailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), likeKeyword
                );
                Predicate phonePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("phone")), likeKeyword
                );
                Predicate cardNumberPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("cardNumber")), likeKeyword
                );
                Predicate qrCodePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("qrCode")), likeKeyword
                );

                predicates.add(criteriaBuilder.or(
                        fullNamePredicate,
                        emailPredicate,
                        phonePredicate,
                        cardNumberPredicate,
                        qrCodePredicate
                ));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (membershipLevel != null) {
                predicates.add(criteriaBuilder.equal(root.get("membershipLevel"), membershipLevel));
            }

            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return borrowerRepository.findAll(specification, pageable);
    }

    @Override
    public Borrower getBorrowerById(Long id) {
        return borrowerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người mượn với ID: " + id));
    }

    @Override
    public Borrower saveBorrower(Borrower borrower) {
        normalizeData(borrower);
        validateUniqueFields(borrower, null);
        applyBusinessRules(borrower);
        return borrowerRepository.save(borrower);
    }

    @Override
    public Borrower updateBorrower(Long id, Borrower borrower) {
        Borrower existing = getBorrowerById(id);

        existing.setFullName(borrower.getFullName());
        existing.setPhone(borrower.getPhone());
        existing.setEmail(borrower.getEmail());
        existing.setAddress(borrower.getAddress());
        existing.setCardNumber(borrower.getCardNumber());
        existing.setQrCode(borrower.getQrCode());
        existing.setCreatedDate(borrower.getCreatedDate());

        existing.setStatus(borrower.getStatus());
        existing.setMembershipLevel(borrower.getMembershipLevel());
        existing.setReputationScore(borrower.getReputationScore());
        existing.setCurrentBorrowedCount(borrower.getCurrentBorrowedCount());
        existing.setLateReturnCount(borrower.getLateReturnCount());
        existing.setActive(borrower.getActive());

        normalizeData(existing);
        validateUniqueFields(existing, id);
        applyBusinessRules(existing);

        return borrowerRepository.save(existing);
    }

    @Override
    public void deleteBorrower(Long id) {
        Borrower borrower = getBorrowerById(id);
        borrowerRepository.delete(borrower);
    }

    @Override
    public boolean canBorrow(Long borrowerId) {
        Borrower borrower = getBorrowerById(borrowerId);
        return borrower.isAllowedToBorrow();
    }

    @Override
    public List<BorrowHistoryDto> getBorrowHistory(Long borrowerId) {
        List<BorrowRecord> borrowRecords = borrowRecordRepository.findByBorrowerIdOrderByBorrowDateDesc(borrowerId);

        List<BorrowHistoryDto> history = new ArrayList<>();

        for (BorrowRecord record : borrowRecords) {
            BorrowHistoryDto dto = new BorrowHistoryDto();
            dto.setBorrowRecordId(record.getId());
            dto.setBorrowCode(record.getBorrowCode());
            dto.setBookTitle(record.getBook() != null ? record.getBook().getTitle() : "Không xác định");
            dto.setBorrowDate(record.getBorrowDate());
            dto.setDueDate(record.getDueDate());
            dto.setBorrowStatus(record.getStatus());

            Optional<ReturnRecord> returnRecordOpt = returnRecordRepository.findByBorrowRecordId(record.getId());

            if (returnRecordOpt.isPresent()) {
                ReturnRecord returnRecord = returnRecordOpt.get();
                dto.setReturnDate(returnRecord.getReturnDate());
                dto.setReturnStatus(returnRecord.getStatus());
                dto.setFineAmount(returnRecord.getFineAmount() != null ? returnRecord.getFineAmount() : BigDecimal.ZERO);
            } else {
                dto.setReturnDate(null);
                dto.setReturnStatus(null);
                dto.setFineAmount(BigDecimal.ZERO);
            }

            boolean overdue = record.getDueDate() != null
                    && LocalDate.now().isAfter(record.getDueDate())
                    && !returnRecordRepository.existsByBorrowRecordId(record.getId());

            dto.setOverdue(overdue);

            history.add(dto);
        }

        return history;
    }

    @Override
    public Borrower findByQrCode(String qrCode) {
        return borrowerRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người mượn với mã QR/Barcode: " + qrCode));
    }

    private void normalizeData(Borrower borrower) {
        if (borrower.getFullName() != null) {
            borrower.setFullName(borrower.getFullName().trim());
        }

        if (borrower.getPhone() != null) {
            borrower.setPhone(borrower.getPhone().trim());
            if (borrower.getPhone().isEmpty()) {
                borrower.setPhone(null);
            }
        }

        if (borrower.getEmail() != null) {
            borrower.setEmail(borrower.getEmail().trim());
            if (borrower.getEmail().isEmpty()) {
                borrower.setEmail(null);
            }
        }

        if (borrower.getAddress() != null) {
            borrower.setAddress(borrower.getAddress().trim());
            if (borrower.getAddress().isEmpty()) {
                borrower.setAddress(null);
            }
        }

        if (borrower.getCardNumber() != null) {
            borrower.setCardNumber(borrower.getCardNumber().trim());
            if (borrower.getCardNumber().isEmpty()) {
                borrower.setCardNumber(null);
            }
        }

        if (borrower.getQrCode() != null) {
            borrower.setQrCode(borrower.getQrCode().trim());
            if (borrower.getQrCode().isEmpty()) {
                borrower.setQrCode(null);
            }
        }
    }

    private void validateUniqueFields(Borrower borrower, Long currentId) {
        if (StringUtils.hasText(borrower.getEmail())) {
            boolean exists = (currentId == null)
                    ? borrowerRepository.existsByEmail(borrower.getEmail())
                    : borrowerRepository.existsByEmailAndIdNot(borrower.getEmail(), currentId);
            if (exists) {
                throw new IllegalArgumentException("Email đã tồn tại.");
            }
        }

        if (StringUtils.hasText(borrower.getPhone())) {
            boolean exists = (currentId == null)
                    ? borrowerRepository.existsByPhone(borrower.getPhone())
                    : borrowerRepository.existsByPhoneAndIdNot(borrower.getPhone(), currentId);
            if (exists) {
                throw new IllegalArgumentException("Số điện thoại đã tồn tại.");
            }
        }

        if (StringUtils.hasText(borrower.getCardNumber())) {
            boolean exists = (currentId == null)
                    ? borrowerRepository.existsByCardNumber(borrower.getCardNumber())
                    : borrowerRepository.existsByCardNumberAndIdNot(borrower.getCardNumber(), currentId);
            if (exists) {
                throw new IllegalArgumentException("Mã thẻ đã tồn tại.");
            }
        }

        if (StringUtils.hasText(borrower.getQrCode())) {
            boolean exists = (currentId == null)
                    ? borrowerRepository.existsByQrCode(borrower.getQrCode())
                    : borrowerRepository.existsByQrCodeAndIdNot(borrower.getQrCode(), currentId);
            if (exists) {
                throw new IllegalArgumentException("Mã QR/Barcode đã tồn tại.");
            }
        }
    }

    private void applyBusinessRules(Borrower borrower) {
        if (borrower.getStatus() == null) {
            borrower.setStatus(BorrowerStatus.ACTIVE);
        }

        if (borrower.getMembershipLevel() == null) {
            borrower.setMembershipLevel(MembershipLevel.BRONZE);
        }

        if (borrower.getReputationScore() == null) {
            borrower.setReputationScore(100);
        }

        if (borrower.getCurrentBorrowedCount() == null || borrower.getCurrentBorrowedCount() < 0) {
            borrower.setCurrentBorrowedCount(0);
        }

        if (borrower.getLateReturnCount() == null || borrower.getLateReturnCount() < 0) {
            borrower.setLateReturnCount(0);
        }

        if (borrower.getActive() == null) {
            borrower.setActive(true);
        }

        if (!StringUtils.hasText(borrower.getQrCode())) {
            String base = StringUtils.hasText(borrower.getCardNumber())
                    ? borrower.getCardNumber()
                    : String.valueOf(System.currentTimeMillis());
            borrower.setQrCode("LIB-" + base);
        }

        borrower.updateMaxBorrowLimitByMembership();
        borrower.updateStatusByViolation();

        if (borrower.getCurrentBorrowedCount() > borrower.getMaxBorrowLimit()) {
            borrower.setCurrentBorrowedCount(borrower.getMaxBorrowLimit());
        }
    }

    private String resolveSortField(String sortField) {
        Set<String> allowedFields = Set.of(
                "id",
                "fullName",
                "email",
                "phone",
                "cardNumber",
                "qrCode",
                "status",
                "membershipLevel",
                "reputationScore",
                "currentBorrowedCount",
                "maxBorrowLimit",
                "createdDate"
        );

        return allowedFields.contains(sortField) ? sortField : "id";
    }
}