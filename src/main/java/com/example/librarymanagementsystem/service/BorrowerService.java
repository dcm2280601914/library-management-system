package com.example.libmanagement.service;

import com.example.libmanagement.dto.BorrowHistoryDto;
import com.example.libmanagement.entity.Borrower;
import com.example.libmanagement.enums.BorrowerStatus;
import com.example.libmanagement.enums.MembershipLevel;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BorrowerService {

    List<Borrower> findAll();

    Page<Borrower> searchBorrowers(
            String keyword,
            BorrowerStatus status,
            MembershipLevel membershipLevel,
            Boolean active,
            int page,
            int size,
            String sortField,
            String sortDir
    );

    Borrower getBorrowerById(Long id);

    Borrower saveBorrower(Borrower borrower);

    Borrower updateBorrower(Long id, Borrower borrower);

    void deleteBorrower(Long id);

    boolean canBorrow(Long borrowerId);

    List<BorrowHistoryDto> getBorrowHistory(Long borrowerId);

    Borrower findByQrCode(String qrCode);
}