package com.campusfasttransfer.controller;

import com.campusfasttransfer.entity.User;
import com.campusfasttransfer.service.AdminService;
import jakarta.servlet.http.HttpSession;
import java.util.Objects;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admin")
    public String adminPage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (!Objects.equals("ADMIN", currentUser.getRole())) {
            return "redirect:/dashboard";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", adminService.listUsers());
        model.addAttribute("files", adminService.listFiles());
        return "admin";
    }
}
