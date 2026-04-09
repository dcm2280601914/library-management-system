package com.example.libmanagement.repository;

import com.example.libmanagement.entity.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BorrowerRepository extends JpaRepository<Borrower, Long>, JpaSpecificationExecutor<Borrower> {

    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByPhone(String phone);
    boolean existsByPhoneAndIdNot(String phone, Long id);

    boolean existsByCardNumber(String cardNumber);
    boolean existsByCardNumberAndIdNot(String cardNumber, Long id);

    boolean existsByQrCode(String qrCode);
    boolean existsByQrCodeAndIdNot(String qrCode, Long id);

    Optional<Borrower> findByQrCode(String qrCode);
}