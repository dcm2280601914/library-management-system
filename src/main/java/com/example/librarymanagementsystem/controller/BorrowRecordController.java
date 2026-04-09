package com.example.librarymanagementsystem.controller;

import com.example.librarymanagementsystem.entity.Book;
import com.example.librarymanagementsystem.entity.BorrowRecord;
import com.example.librarymanagementsystem.entity.Borrower;
import com.example.librarymanagementsystem.entity.Employee;
import com.example.librarymanagementsystem.enums.BorrowStatus;
import com.example.librarymanagementsystem.service.BookService;
import com.example.librarymanagementsystem.service.BorrowRecordService;
import com.example.librarymanagementsystem.service.BorrowerService;
import com.example.librarymanagementsystem.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/borrow-records")
public class BorrowRecordController {

    private final BorrowRecordService borrowRecordService;
    private final BorrowerService borrowerService;
    private final EmployeeService employeeService;
    private final BookService bookService;

    public BorrowRecordController(BorrowRecordService borrowRecordService,
                                  BorrowerService borrowerService,
                                  EmployeeService employeeService,
                                  BookService bookService) {
        this.borrowRecordService = borrowRecordService;
        this.borrowerService = borrowerService;
        this.employeeService = employeeService;
        this.bookService = bookService;
    }

    @GetMapping
    public String listBorrowRecords(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<BorrowRecord> borrowRecords;
        if (keyword != null && !keyword.trim().isEmpty()) {
            borrowRecords = borrowRecordService.searchByBorrowerName(keyword);
        } else {
            borrowRecords = borrowRecordService.findAll();
        }
        model.addAttribute("borrowRecords", borrowRecords);
        model.addAttribute("keyword", keyword);
        return "borrow-records/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setBorrower(new Borrower());
        borrowRecord.setEmployee(new Employee());
        borrowRecord.setBook(new Book());

        model.addAttribute("borrowRecord", borrowRecord);
        model.addAttribute("borrowers", borrowerService.findAll());
        model.addAttribute("employees", employeeService.findAll());
        model.addAttribute("books", bookService.findAll());
        model.addAttribute("statuses", Arrays.asList(BorrowStatus.values()));
        return "borrow-records/add";
    }

    @PostMapping("/save")
    public String saveBorrowRecord(@ModelAttribute("borrowRecord") BorrowRecord borrowRecord,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            borrowRecordService.save(borrowRecord);
            redirectAttributes.addFlashAttribute("successMessage", "Lập phiếu mượn thành công.");
            return "redirect:/borrow-records";
        } catch (RuntimeException e) {
            if (borrowRecord.getBorrower() == null) {
                borrowRecord.setBorrower(new Borrower());
            }
            if (borrowRecord.getEmployee() == null) {
                borrowRecord.setEmployee(new Employee());
            }
            if (borrowRecord.getBook() == null) {
                borrowRecord.setBook(new Book());
            }

            model.addAttribute("borrowRecord", borrowRecord);
            model.addAttribute("borrowers", borrowerService.findAll());
            model.addAttribute("employees", employeeService.findAll());
            model.addAttribute("books", bookService.findAll());
            model.addAttribute("statuses", Arrays.asList(BorrowStatus.values()));
            model.addAttribute("errorMessage", e.getMessage());
            return "borrow-records/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        BorrowRecord borrowRecord = borrowRecordService.findById(id);
        if (borrowRecord == null) {
            return "redirect:/borrow-records";
        }
        model.addAttribute("borrowRecord", borrowRecord);
        model.addAttribute("borrowers", borrowerService.findAll());
        model.addAttribute("employees", employeeService.findAll());
        model.addAttribute("books", bookService.findAll());
        model.addAttribute("statuses", Arrays.asList(BorrowStatus.values()));
        return "borrow-records/edit";
    }

    @PostMapping("/update/{id}")
    public String updateBorrowRecord(@PathVariable Long id,
                                     @ModelAttribute("borrowRecord") BorrowRecord borrowRecord,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        try {
            borrowRecordService.update(id, borrowRecord);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phiếu mượn thành công.");
            return "redirect:/borrow-records";
        } catch (RuntimeException e) {
            model.addAttribute("borrowRecord", borrowRecord);
            model.addAttribute("borrowers", borrowerService.findAll());
            model.addAttribute("employees", employeeService.findAll());
            model.addAttribute("books", bookService.findAll());
            model.addAttribute("statuses", Arrays.asList(BorrowStatus.values()));
            model.addAttribute("errorMessage", e.getMessage());
            return "borrow-records/edit";
        }
    }

    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable Long id, Model model) {
        BorrowRecord borrowRecord = borrowRecordService.findById(id);
        if (borrowRecord == null) {
            return "redirect:/borrow-records";
        }
        model.addAttribute("borrowRecord", borrowRecord);
        return "borrow-records/detail";
    }

    @GetMapping("/delete/{id}")
    public String deleteBorrowRecord(@PathVariable Long id) {
        borrowRecordService.delete(id);
        return "redirect:/borrow-records";
    }
}