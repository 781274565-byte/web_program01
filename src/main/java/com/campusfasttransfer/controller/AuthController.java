package com.campusfasttransfer.controller;

import com.campusfasttransfer.dto.LoginForm;
import com.campusfasttransfer.dto.RegisterForm;
import com.campusfasttransfer.entity.User;
import com.campusfasttransfer.service.AuthService;
import com.campusfasttransfer.service.RegistrationConflictException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm loginForm,
                        BindingResult bindingResult,
                        HttpSession session,
                        Model model) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        return authService.authenticate(loginForm.getUsername(), loginForm.getPassword())
                .map(user -> {
                    session.setAttribute("currentUser", user);
                    return "redirect:/dashboard";
                })
                .orElseGet(() -> {
                    model.addAttribute("loginError", "Invalid username or password");
                    return "login";
                });
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm registerForm,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            authService.register(registerForm);
            redirectAttributes.addFlashAttribute("registrationSuccess", "Registration successful. Please sign in.");
            return "redirect:/login";
        } catch (RegistrationConflictException ex) {
            model.addAttribute("registrationError", ex.getMessage());
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        model.addAttribute("currentUser", currentUser);
        return "dashboard";
    }
}
