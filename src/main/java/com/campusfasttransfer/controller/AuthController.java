//Spring MVC控制器，处理登录、注册、退出和进入首页的认证相关请求
package com.campusfasttransfer.controller;

import com.campusfasttransfer.dto.LoginForm; //导入登录表单对象，接收用户提交的用户名和密码
import com.campusfasttransfer.dto.RegisterForm; //导入注册表单对象，接收注册时输入的信息
import com.campusfasttransfer.entity.User; //导入User实体类，表示系统里的用户对象
import com.campusfasttransfer.service.AuthService; //导入认证服务类，控制器将登录/注册逻辑交给它处理
import com.campusfasttransfer.service.RegistrationConflictException; //导入自定义异常，用来表示注册时发生冲突,like用户名重复
import jakarta.servlet.http.HttpSession; //导入HttpSession，用于把当前登录用户保存到会话里
import jakarta.validation.Valid; //导入 @Valid，用于触发表单校验
import org.springframework.stereotype.Controller; //导入@Controller，告诉Spring这是一个控制器类
import org.springframework.ui.Model; //导入Model，用于把数据传给页面模板
import org.springframework.validation.BindingResult; //导入BindingResult用于接收表单校验结果
import org.springframework.web.bind.annotation.GetMapping; //导入@GetMapping，用于处理GET请求
import org.springframework.web.bind.annotation.ModelAttribute; //导入@ModelAttribute，用于把请求参数绑定到Java对象
import org.springframework.web.bind.annotation.PostMapping; //导入@PostMapping，用于处理POST请求
import org.springframework.web.servlet.mvc.support.RedirectAttributes; //导入RedirectAttributes，用于重定向时临时携带提示信息

@Controller //标记为Spring MVC控制器
//AuthController负责认证相关接口
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login") //处理GET/login请求，(打开登录页)
    public String loginPage(Model model) {
        //如果模型里还没有loginForm，就说明这次不是带旧表单数据返回来
        if (!model.containsAttribute("loginForm")) {
            //往页面里放一个新的登录表单对象，给页面表单绑定使用
            model.addAttribute("loginForm", new LoginForm());
        }
        return "login"; //渲染login.html
    }

    @PostMapping("/login") //处理POST/login，(用户提交登录表单)
    //把请求参数绑定到loginForm对象上，并用@Valid按LoginForm上的校验规则检查
    public String login(@Valid @ModelAttribute("loginForm") LoginForm loginForm,
                        //接收表单校验结果(if用户名or密码为空，这里会记录错误)
                        BindingResult bindingResult,
                        //拿到当前会话对象，登录成功后要把用户存进去
                        HttpSession session,                   
                        Model model) {
        //如果表单校验没通过，就不继续认证                    
        if (bindingResult.hasErrors()) {
            return "login";
        }

        //调用认证服务，用用户名和密码去校验用户身份
        return authService.authenticate(loginForm.getUsername(), loginForm.getPassword())
                //认证成功
                .map(user -> {
                    session.setAttribute("currentUser", user); //把当前登录用户放进session(键名:currentUser),这样后续请求可以取出来判断是谁登录
                    return "redirect:/dashboard"; //重定向到/dashboard
                })
                //认证失败
                .orElseGet(() -> {
                    model.addAttribute("loginError", "Invalid username or password"); //放错误提示
                    return "login";
                });
    }

    //处理GET/register，(打开注册页)
    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        return "register";
    }

    //处理POST/register，(提交注册表单)
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm registerForm,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        //开始尝试执行注册逻辑(因为注册过程可能抛出冲突异常)
        try {
            authService.register(registerForm); //调用服务层真正创建用户,检查用户名/身份证是否重复
            redirectAttributes.addFlashAttribute("registrationSuccess", "Registration successful. Please sign in.");//flash:把“注册成功”的提示带到登录页
            return "redirect:/login";
        } catch (RegistrationConflictException ex) { //注册时发现冲突，就捕获这个自定义异常
            model.addAttribute("registrationError", ex.getMessage()); //把异常里的错误信息放到模型里，让注册页显示给用户看
            return "register";
        }
    }

    //处理GET/logout，(退出登录)
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); //使当前 session 失效
        return "redirect:/login";
    }

    //处理GET/dashboard，(登录后的首页)
    @GetMapping("/dashboard")
    //session取当前用户，model往页面传值
    public String dashboard(HttpSession session, Model model) {
        //从session里取出之前登录时存进去的currentUser，并强转成User
        User currentUser = (User) session.getAttribute("currentUser");
        model.addAttribute("currentUser", currentUser);
        return "dashboard";
    }
}
