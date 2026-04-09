package com.example.librarymanagementsystem.controller;


import com.example.librarymanagementsystem.entity.Book;
import com.example.librarymanagementsystem.service.BookService;
import com.example.librarymanagementsystem.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final CategoryService categoryService;

    public BookController(BookService bookService, CategoryService categoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listBooks(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer publicationYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model
    ) {
        Page<Book> bookPage = bookService.searchBooks(
                keyword, categoryId, author, publicationYear, page, size, sortField, sortDir
        );

        model.addAttribute("bookPage", bookPage);
        model.addAttribute("books", bookPage.getContent());

        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("authors", bookService.getAllAuthors());
        model.addAttribute("years", bookService.getAllPublicationYears());

        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("author", author);
        model.addAttribute("publicationYear", publicationYear);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "books/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.findAll());
        return "books/add";
    }

    @PostMapping("/save")
    public String saveBook(@ModelAttribute Book book,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            bookService.save(book);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sách thành công");
            return "redirect:/books";
        } catch (Exception e) {
            String raw = e.getMessage();
            String message = "Thêm sách thất bại. Vui lòng kiểm tra lại dữ liệu.";

            if (raw != null) {
                String lower = raw.toLowerCase();
                if (lower.contains("duplicate entry") && lower.contains("barcode")) {
                    message = "Mã barcode đã tồn tại. Vui lòng nhập barcode khác.";
                } else if (lower.contains("duplicate entry")) {
                    message = "Dữ liệu bị trùng. Vui lòng kiểm tra lại.";
                }
            }

            model.addAttribute("book", book);
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("errorMessage", message);
            return "books/add";
        }
    }
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Book book = bookService.findById(id);
        if (book == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sách cần sửa");
            return "redirect:/books";
        }

        model.addAttribute("book", book);
        model.addAttribute("categories", categoryService.findAll());
        return "books/edit";
    }

    @PostMapping("/update/{id}")
    public String updateBook(@PathVariable Long id,
                             @ModelAttribute Book book,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            Book updatedBook = bookService.update(id, book);

            if (updatedBook == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sách để cập nhật");
                return "redirect:/books";
            }

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sách thành công");
            return "redirect:/books";
        } catch (RuntimeException e) {
            book.setId(id);
            model.addAttribute("book", book);
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("errorMessage", e.getMessage());
            return "books/edit";
        } catch (Exception e) {
            book.setId(id);
            model.addAttribute("book", book);
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("errorMessage", "Cập nhật sách thất bại. Vui lòng kiểm tra lại dữ liệu.");
            return "books/edit";
        }
    }

    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable Long id,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Book book = bookService.findById(id);
        if (book == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy chi tiết sách");
            return "redirect:/books";
        }

        model.addAttribute("book", book);
        return "books/detail";
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sách thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xóa sách thất bại");
        }
        return "redirect:/books";
    }
}