package com.campusfasttransfer.controller;

import com.campusfasttransfer.entity.User; //导入User实体类，后面用这个表示当前登录用户
import com.campusfasttransfer.service.AdminService; //导入AdminService，控制器通过这个获取管理员页面需要的数据
import jakarta.servlet.http.HttpSession; //导入HttpSession，用于从会话中读取当前登录用户信息
import java.util.Objects; //导入Objects工具类
import org.springframework.stereotype.Controller; //导入@Controller注解
import org.springframework.ui.Model; //导入 Model
import org.springframework.web.bind.annotation.GetMapping; //导入@GetMapping注解

@Controller
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admin") //发送GET请求到/admin时，调用下面这个方法
    public String adminPage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser"); //当前登录用户
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (!Objects.equals("ADMIN", currentUser.getRole())) {
            return "redirect:/dashboard";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", adminService.listUsers()); //获取用户列表，并传给页面
        model.addAttribute("files", adminService.listFiles()); //获取文件列表，并传给页面
        return "admin";
    }
}
