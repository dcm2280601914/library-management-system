package com.example.librarymanagementsystem.service;


public interface EmailNotificationService {

    void sendBorrowReminderToBorrower(Long borrowerId);
}