package com.example.libmanagement.service;


public interface EmailNotificationService {

    void sendBorrowReminderToBorrower(Long borrowerId);
}