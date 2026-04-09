package com.example.libmanagement.entity;

import com.example.libmanagement.enums.BorrowerStatus;
import com.example.libmanagement.enums.MembershipLevel;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "borrowers")
public class Borrower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "phone", length = 20, unique = true)
    private String phone;

    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "card_number", length = 30, unique = true)
    private String cardNumber;

    @Column(name = "qr_code", length = 100, unique = true)
    private String qrCode;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BorrowerStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_level", nullable = false, length = 20)
    private MembershipLevel membershipLevel;

    @Column(name = "reputation_score", nullable = false)
    private Integer reputationScore;

    @Column(name = "current_borrowed_count", nullable = false)
    private Integer currentBorrowedCount;

    @Column(name = "max_borrow_limit", nullable = false)
    private Integer maxBorrowLimit;

    @Column(name = "late_return_count", nullable = false)
    private Integer lateReturnCount;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @OneToMany(mappedBy = "borrower", fetch = FetchType.LAZY)
    @OrderBy("borrowDate DESC")
    private List<BorrowRecord> borrowRecords = new ArrayList<>();

    public Borrower() {
    }

    @PrePersist
    public void prePersist() {
        if (createdDate == null) {
            createdDate = LocalDate.now();
        }
        if (status == null) {
            status = BorrowerStatus.ACTIVE;
        }
        if (membershipLevel == null) {
            membershipLevel = MembershipLevel.BRONZE;
        }
        if (reputationScore == null) {
            reputationScore = 100;
        }
        if (currentBorrowedCount == null) {
            currentBorrowedCount = 0;
        }
        if (maxBorrowLimit == null) {
            maxBorrowLimit = 3;
        }
        if (lateReturnCount == null) {
            lateReturnCount = 0;
        }
        if (active == null) {
            active = true;
        }
        if (qrCode == null || qrCode.trim().isEmpty()) {
            String base = (cardNumber != null && !cardNumber.trim().isEmpty())
                    ? cardNumber.trim()
                    : String.valueOf(System.currentTimeMillis());
            qrCode = "LIB-" + base;
        }

        updateMaxBorrowLimitByMembership();
        updateStatusByViolation();
    }

    @PreUpdate
    public void preUpdate() {
        if (status == null) {
            status = BorrowerStatus.ACTIVE;
        }
        if (membershipLevel == null) {
            membershipLevel = MembershipLevel.BRONZE;
        }
        if (reputationScore == null) {
            reputationScore = 100;
        }
        if (currentBorrowedCount == null) {
            currentBorrowedCount = 0;
        }
        if (maxBorrowLimit == null) {
            maxBorrowLimit = 3;
        }
        if (lateReturnCount == null) {
            lateReturnCount = 0;
        }
        if (active == null) {
            active = true;
        }
        if (qrCode == null || qrCode.trim().isEmpty()) {
            String base = (cardNumber != null && !cardNumber.trim().isEmpty())
                    ? cardNumber.trim()
                    : String.valueOf(System.currentTimeMillis());
            qrCode = "LIB-" + base;
        }

        updateMaxBorrowLimitByMembership();
        updateStatusByViolation();
    }

    @Transient
    public int getRemainingBorrowLimit() {
        int current = currentBorrowedCount != null ? currentBorrowedCount : 0;
        int max = maxBorrowLimit != null ? maxBorrowLimit : 0;
        return Math.max(max - current, 0);
    }

    @Transient
    public String getBorrowProgress() {
        int current = currentBorrowedCount != null ? currentBorrowedCount : 0;
        int max = maxBorrowLimit != null ? maxBorrowLimit : 0;
        return current + "/" + max;
    }

    @Transient
    public boolean isAllowedToBorrow() {
        return Boolean.TRUE.equals(active)
                && status == BorrowerStatus.ACTIVE
                && currentBorrowedCount != null
                && maxBorrowLimit != null
                && currentBorrowedCount < maxBorrowLimit;
    }

    @Transient
    public boolean canReceiveEmailNotification() {
        return Boolean.TRUE.equals(active)
                && email != null
                && !email.trim().isEmpty();
    }

    @Transient
    public boolean hasQrCode() {
        return qrCode != null && !qrCode.trim().isEmpty();
    }

    public void updateMaxBorrowLimitByMembership() {
        if (membershipLevel == null) {
            this.maxBorrowLimit = 3;
            return;
        }

        switch (membershipLevel) {
            case BRONZE:
                this.maxBorrowLimit = 3;
                break;
            case SILVER:
                this.maxBorrowLimit = 5;
                break;
            case GOLD:
                this.maxBorrowLimit = 7;
                break;
            default:
                this.maxBorrowLimit = 3;
        }
    }

    public void updateMembershipLevelByReputation() {
        if (reputationScore == null) {
            this.membershipLevel = MembershipLevel.BRONZE;
            return;
        }

        if (reputationScore >= 200) {
            this.membershipLevel = MembershipLevel.GOLD;
        } else if (reputationScore >= 120) {
            this.membershipLevel = MembershipLevel.SILVER;
        } else {
            this.membershipLevel = MembershipLevel.BRONZE;
        }
    }

    public void updateStatusByViolation() {
        if (lateReturnCount != null && lateReturnCount >= 3) {
            this.status = BorrowerStatus.LOCKED;
        } else if (this.status == null) {
            this.status = BorrowerStatus.ACTIVE;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public BorrowerStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowerStatus status) {
        this.status = status;
    }

    public MembershipLevel getMembershipLevel() {
        return membershipLevel;
    }

    public void setMembershipLevel(MembershipLevel membershipLevel) {
        this.membershipLevel = membershipLevel;
    }

    public Integer getReputationScore() {
        return reputationScore;
    }

    public void setReputationScore(Integer reputationScore) {
        this.reputationScore = reputationScore;
    }

    public Integer getCurrentBorrowedCount() {
        return currentBorrowedCount;
    }

    public void setCurrentBorrowedCount(Integer currentBorrowedCount) {
        this.currentBorrowedCount = currentBorrowedCount;
    }

    public Integer getMaxBorrowLimit() {
        return maxBorrowLimit;
    }

    public void setMaxBorrowLimit(Integer maxBorrowLimit) {
        this.maxBorrowLimit = maxBorrowLimit;
    }

    public Integer getLateReturnCount() {
        return lateReturnCount;
    }

    public void setLateReturnCount(Integer lateReturnCount) {
        this.lateReturnCount = lateReturnCount;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<BorrowRecord> getBorrowRecords() {
        return borrowRecords;
    }

    public void setBorrowRecords(List<BorrowRecord> borrowRecords) {
        this.borrowRecords = borrowRecords;
    }
}