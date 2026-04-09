package com.example.librarymanagementsystem.controller;

import com.example.librarymanagementsystem.dto.BorrowHistoryDto;
import com.example.librarymanagementsystem.entity.Borrower;
import com.example.librarymanagementsystem.enums.BorrowerStatus;
import com.example.librarymanagementsystem.enums.MembershipLevel;
import com.example.librarymanagementsystem.service.BorrowerService;
import com.example.librarymanagementsystem.service.EmailNotificationService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/borrowers")
public class BorrowerController {

    private final BorrowerService borrowerService;
    private final EmailNotificationService emailNotificationService;

    public BorrowerController(BorrowerService borrowerService,
                              EmailNotificationService emailNotificationService) {
        this.borrowerService = borrowerService;
        this.emailNotificationService = emailNotificationService;
    }

    @ModelAttribute("statuses")
    public BorrowerStatus[] statuses() {
        return BorrowerStatus.values();
    }

    @ModelAttribute("membershipLevels")
    public MembershipLevel[] membershipLevels() {
        return MembershipLevel.values();
    }

    @GetMapping
    public String listBorrowers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) BorrowerStatus status,
            @RequestParam(required = false) MembershipLevel membershipLevel,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model
    ) {
        Page<Borrower> borrowerPage = borrowerService.searchBorrowers(
                keyword, status, membershipLevel, active, page, size, sortField, sortDir
        );

        model.addAttribute("borrowerPage", borrowerPage);
        model.addAttribute("borrowers", borrowerPage.getContent());

        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedMembershipLevel", membershipLevel);
        model.addAttribute("selectedActive", active);

        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equalsIgnoreCase("asc") ? "desc" : "asc");

        return "borrowers/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        Borrower borrower = new Borrower();
        borrower.updateMaxBorrowLimitByMembership();

        model.addAttribute("borrower", borrower);
        return "borrowers/add";
    }

    @PostMapping("/save")
    public String saveBorrower(@ModelAttribute("borrower") Borrower borrower,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            borrowerService.saveBorrower(borrower);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm người mượn thành công.");
            return "redirect:/borrowers";
        } catch (IllegalArgumentException e) {
            model.addAttribute("borrower", borrower);
            model.addAttribute("errorMessage", e.getMessage());
            return "borrowers/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Borrower borrower = borrowerService.getBorrowerById(id);
        model.addAttribute("borrower", borrower);
        return "borrowers/edit";
    }

    @PostMapping("/update/{id}")
    public String updateBorrower(@PathVariable Long id,
                                 @ModelAttribute("borrower") Borrower borrower,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            borrowerService.updateBorrower(id, borrower);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật người mượn thành công.");
            return "redirect:/borrowers";
        } catch (IllegalArgumentException e) {
            borrower.setId(id);
            model.addAttribute("borrower", borrower);
            model.addAttribute("errorMessage", e.getMessage());
            return "borrowers/edit";
        }
    }

    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable Long id, Model model) {
        Borrower borrower = borrowerService.getBorrowerById(id);
        List<BorrowHistoryDto> borrowHistory = borrowerService.getBorrowHistory(id);

        model.addAttribute("borrower", borrower);
        model.addAttribute("borrowHistory", borrowHistory);
        return "borrowers/detail";
    }

    @GetMapping("/delete/{id}")
    public String deleteBorrower(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            borrowerService.deleteBorrower(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa người mượn thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa người mượn này.");
        }
        return "redirect:/borrowers";
    }

    @GetMapping("/send-notification/{id}")
    public String sendNotification(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            emailNotificationService.sendBorrowReminderToBorrower(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi email thông báo cho người mượn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/borrowers";
    }

    @GetMapping("/scan")
    public String showScanPage() {
        return "borrowers/scan";
    }

    @PostMapping("/scan")
    public String handleScan(@RequestParam("qrCode") String qrCode,
                             RedirectAttributes redirectAttributes) {
        try {
            Borrower borrower = borrowerService.findByQrCode(qrCode);
            return "redirect:/borrowers/detail/" + borrower.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người mượn với mã đã quét.");
            return "redirect:/borrowers/scan";
        }
    }
}