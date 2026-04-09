package com.example.librarymanagementsystem.controller;

import com.example.librarymanagementsystem.entity.BorrowRecord;
import com.example.librarymanagementsystem.entity.ReturnRecord;
import com.example.librarymanagementsystem.enums.BookCondition;
import com.example.librarymanagementsystem.enums.BorrowStatus;
import com.example.librarymanagementsystem.enums.FineReason;
import com.example.librarymanagementsystem.enums.PaymentStatus;
import com.example.librarymanagementsystem.enums.ReturnStatus;
import com.example.librarymanagementsystem.repository.BorrowRecordRepository;
import com.example.librarymanagementsystem.service.ReturnRecordService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/return-records")
public class ReturnRecordController {

    private final ReturnRecordService returnRecordService;
    private final BorrowRecordRepository borrowRecordRepository;

    public ReturnRecordController(ReturnRecordService returnRecordService,
                                  BorrowRecordRepository borrowRecordRepository) {
        this.returnRecordService = returnRecordService;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    @GetMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Model model) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("returnRecords", returnRecordService.searchByBorrowerName(keyword));
        return "return-records/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        ReturnRecord returnRecord = new ReturnRecord();
        returnRecord.setBorrowRecord(new BorrowRecord());
        returnRecord.setBookCondition(BookCondition.INTACT);

        model.addAttribute("returnRecord", returnRecord);
        model.addAttribute("borrowRecords",
                borrowRecordRepository.findByStatusOrderByBorrowDateDesc(BorrowStatus.BORROWED));
        model.addAttribute("bookConditions", BookCondition.values());

        return "return-records/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("returnRecord") ReturnRecord returnRecord,
                      Model model) {
        try {
            returnRecordService.save(returnRecord);
            return "redirect:/return-records";
        } catch (Exception e) {
            if (returnRecord.getBorrowRecord() == null) {
                returnRecord.setBorrowRecord(new BorrowRecord());
            }

            model.addAttribute("returnRecord", returnRecord);
            model.addAttribute("borrowRecords",
                    borrowRecordRepository.findByStatusOrderByBorrowDateDesc(BorrowStatus.BORROWED));
            model.addAttribute("bookConditions", BookCondition.values());
            model.addAttribute("errorMessage", e.getMessage());

            return "return-records/add";
        }
    }

    @PostMapping("/quick-confirm")
    public String quickConfirm(@RequestParam("borrowCode") String borrowCode,
                               @RequestParam(value = "bookCondition", defaultValue = "INTACT") BookCondition bookCondition,
                               RedirectAttributes redirectAttributes) {
        try {
            BorrowRecord borrowRecord = borrowRecordRepository.findByBorrowCode(borrowCode.trim())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn với mã: " + borrowCode));

            ReturnRecord returnRecord = new ReturnRecord();
            returnRecord.setBorrowRecord(borrowRecord);
            returnRecord.setReturnDate(LocalDate.now());
            returnRecord.setBookCondition(bookCondition);

            ReturnRecord saved = returnRecordService.save(returnRecord);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Xác nhận trả nhanh thành công. Mã phiếu trả: " + saved.getReturnCode());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/return-records";
    }

    @GetMapping("/{id}/receipt")
    public String printReceipt(@PathVariable Long id, Model model) {
        model.addAttribute("returnRecord", returnRecordService.findById(id));
        return "return-records/receipt";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("returnRecord", returnRecordService.findById(id));
        model.addAttribute("returnStatuses", ReturnStatus.values());
        model.addAttribute("fineReasons", FineReason.values());
        model.addAttribute("paymentStatuses", PaymentStatus.values());
        model.addAttribute("bookConditions", BookCondition.values());

        return "return-records/edit";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("returnRecord") ReturnRecord returnRecord,
                         Model model) {
        try {
            returnRecordService.update(id, returnRecord);
            return "redirect:/return-records";
        } catch (Exception e) {
            model.addAttribute("returnRecord", returnRecord);
            model.addAttribute("returnStatuses", ReturnStatus.values());
            model.addAttribute("fineReasons", FineReason.values());
            model.addAttribute("paymentStatuses", PaymentStatus.values());
            model.addAttribute("bookConditions", BookCondition.values());
            model.addAttribute("errorMessage", e.getMessage());

            return "return-records/edit";
        }
    }
}