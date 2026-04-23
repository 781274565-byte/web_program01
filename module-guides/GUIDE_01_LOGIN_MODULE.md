# 登录模块讲义

## 1. 这个模块解决什么问题

登录模块负责解决两个最基础的问题：

- 用户如何注册账号
- 用户如何登录系统并保持登录状态

如果没有这个模块，系统就无法区分“谁在使用系统”，后面的上传、分享、管理员权限都会失去意义。

所以可以把登录模块理解成：

“整个项目的入口模块。”

---

## 2. 这个模块涉及哪些文件

### 2.1 前端页面

- `src/main/resources/templates/login.html`
- `src/main/resources/templates/register.html`
- `src/main/resources/templates/dashboard.html`

### 2.2 后端 Controller

- `src/main/java/com/campusfasttransfer/controller/AuthController.java`

### 2.3 后端 Service

- `src/main/java/com/campusfasttransfer/service/AuthService.java`
- `src/main/java/com/campusfasttransfer/service/RegistrationConflictException.java`

### 2.4 DTO

- `src/main/java/com/campusfasttransfer/dto/LoginForm.java`
- `src/main/java/com/campusfasttransfer/dto/RegisterForm.java`

### 2.5 数据库相关

- `src/main/java/com/campusfasttransfer/entity/User.java`
- `src/main/java/com/campusfasttransfer/repository/UserRepository.java`
- `src/main/resources/schema.sql`
- `src/main/resources/data.sql`

### 2.6 登录保护相关配置

- `src/main/java/com/campusfasttransfer/config/LoginInterceptor.java`
- `src/main/java/com/campusfasttransfer/config/WebConfig.java`

---

## 3. 用户表在这个模块中的作用

登录模块主要操作的是 `users` 表。

表中最关键的字段有：

- `id`：用户编号
- `username`：用户名
- `password`：密码
- `identity_no`：身份号
- `role`：用户角色
- `created_at`：创建时间

其中：

- 普通用户注册时，角色默认是 `USER`
- 管理员账号是系统初始化时写入的，角色是 `ADMIN`

在 `data.sql` 中，默认会插入一个管理员：

- 用户名：`admin`
- 密码：`admin123`

---

## 4. 前端页面是怎么写的

### 4.1 login.html

登录页中最关键的表单代码是：

```html
<form th:action="@{/login}" th:object="${loginForm}" method="post">
```

这句可以拆开理解：

- `th:action="@{/login}"`：提交到后端的 `/login`
- `th:object="${loginForm}"`：这个表单对应后端的 `loginForm`

两个输入框：

```html
<input type="text" th:field="*{username}">
<input type="password" th:field="*{password}">
```

表示：

- 用户名输入框绑定 `loginForm.username`
- 密码输入框绑定 `loginForm.password`

### 4.2 register.html

注册页和登录页结构类似，只是字段更多：

- `username`
- `password`
- `identityNo`

这些字段会绑定到 `RegisterForm`。

### 4.3 dashboard.html

登录成功后进入首页。

这个页面主要作用不是处理数据，而是做一个“功能导航页”，让用户进入：

- 文件页
- 分享页
- 管理员页
- 退出登录

这里有一个很关键的条件判断：

```html
<a th:if="${currentUser != null and currentUser.role == 'ADMIN'}" th:href="@{/admin}">Admin</a>
```

表示只有管理员才会看到 `Admin` 入口。

---

## 5. DTO 在登录模块里是怎么用的

### 5.1 LoginForm

文件：

- `dto/LoginForm.java`

字段只有两个：

- `username`
- `password`

这个类的作用是：

“接收登录表单提交上来的数据。”

### 5.2 RegisterForm

文件：

- `dto/RegisterForm.java`

字段有三个：

- `username`
- `password`
- `identityNo`

这个类的作用是：

“接收注册表单提交上来的数据。”

### 5.3 为什么不用 User 直接接表单

这是一个很好的思考点。

因为表单只需要一部分字段，不需要数据库对象里的全部内容。  
比如登录时只需要用户名和密码，不需要：

- `id`
- `role`
- `createdAt`

所以这里单独定义 DTO，会更清晰、更安全。

---

## 6. AuthController 是怎么工作的

文件：

- `controller/AuthController.java`

这个类负责处理以下请求：

- `GET /login`
- `POST /login`
- `GET /register`
- `POST /register`
- `GET /logout`
- `GET /dashboard`

### 6.1 打开登录页

```java
@GetMapping("/login")
public String loginPage(Model model) {
    if (!model.containsAttribute("loginForm")) {
        model.addAttribute("loginForm", new LoginForm());
    }
    return "login";
}
```

作用：

- 如果页面上还没有 `loginForm`
- 就先创建一个空的 `LoginForm`
- 然后返回 `login.html`

也就是说，这个方法负责“打开登录页面并准备表单对象”。

### 6.2 提交登录

```java
@PostMapping("/login")
public String login(@Valid @ModelAttribute("loginForm") LoginForm loginForm,
                    BindingResult bindingResult,
                    HttpSession session,
                    Model model)
```

关键点有 4 个：

1. `@PostMapping("/login")`  
表示这个方法处理登录提交。

2. `@ModelAttribute("loginForm") LoginForm loginForm`  
表示把表单数据封装到 `LoginForm` 对象里。

3. `BindingResult bindingResult`  
表示表单校验结果。

4. `HttpSession session`  
表示当前用户会话。

登录成功的关键代码：

```java
session.setAttribute("currentUser", user);
```

这句代码非常重要，它表示：

- 把当前登录用户保存到 session 中
- 以后项目会通过 `currentUser` 判断这个用户是不是已经登录

### 6.3 打开注册页

逻辑和登录页类似：

- 创建空的 `RegisterForm`
- 返回 `register.html`

### 6.4 提交注册

注册逻辑不是直接写数据库，而是：

```java
authService.register(registerForm);
```

如果注册成功：

```java
redirectAttributes.addFlashAttribute("registrationSuccess", "Registration successful. Please sign in.");
return "redirect:/login";
```

意思是：

- 给登录页带去一条“注册成功”的提示消息
- 然后跳转到登录页

### 6.5 退出登录

```java
session.invalidate();
```

意思是：

- 清空当前 session
- 用户退出登录

---

## 7. AuthService 是怎么工作的

文件：

- `service/AuthService.java`

这个类是登录模块的业务核心。

### 7.1 authenticate 方法

```java
public Optional<User> authenticate(String username, String password) {
    return userRepository.findByUsername(username)
            .filter(user -> Objects.equals(password, user.getPassword()));
}
```

可以按步骤理解：

1. 先按用户名查询数据库
2. 如果找到了用户，再比较密码
3. 密码正确就返回这个用户
4. 密码错误就返回空结果

### 7.2 register 方法

注册逻辑分成 4 步：

1. 检查用户名是否重复
2. 检查身份号是否重复
3. 创建新的 `User` 对象
4. 保存到数据库

其中有一句很重要：

```java
user.setRole(DEFAULT_ROLE);
```

这里的 `DEFAULT_ROLE` 是：

```java
private static final String DEFAULT_ROLE = "USER";
```

也就是说，普通注册进来的用户默认不是管理员。

---

## 8. UserRepository 是怎么查数据库的

文件：

- `repository/UserRepository.java`

这里定义了几个重要方法：

```java
Optional<User> findByUsername(String username);
boolean existsByUsername(String username);
boolean existsByIdentityNo(String identityNo);
```

这些方法看起来像普通方法名，但 Spring Data JPA 会根据名字自动理解它们的含义。

例如：

- `findByUsername`：按用户名查
- `existsByUsername`：检查用户名是否存在
- `existsByIdentityNo`：检查身份号是否存在

这让项目不需要手写 SQL，也能完成常见查询。

---

## 9. session 是怎么让用户保持登录状态的

这是登录模块最核心的知识点之一。

当用户登录成功后：

```java
session.setAttribute("currentUser", user);
```

系统就会在服务器端记住这个用户。

之后访问 `/dashboard`、`/files`、`/share`、`/admin` 时，拦截器会检查：

- session 是否存在
- `currentUser` 是否存在

如果不存在，就说明用户没登录，系统会强制跳回登录页。

所以你可以把 session 理解成：

“服务器帮当前浏览器保存的一份登录记录。”

---

## 10. LoginInterceptor 和 WebConfig 在登录模块中的意义

### 10.1 LoginInterceptor

它的作用是：

- 保护系统内部页面
- 防止未登录访问

逻辑就是：

- 没登录：跳 `/login`
- 已登录：允许继续访问

### 10.2 WebConfig

它告诉系统哪些页面需要拦截。

被保护的页面有：

- `/dashboard`
- `/files`
- `/share`
- `/admin`

不保护的页面有：

- `/login`
- `/register`
- 静态资源

所以这两个类一起构成了“登录权限控制系统”。

---

## 11. 登录模块完整流程

### 11.1 注册流程

1. 用户打开 `/register`
2. `AuthController.registerPage()` 返回注册页面
3. 用户填写用户名、密码、身份号
4. 提交到 `/register`
5. `AuthController.register()` 接收数据
6. `AuthService.register()` 处理业务
7. `UserRepository` 保存到数据库
8. 页面跳转回登录页

### 11.2 登录流程

1. 用户打开 `/login`
2. `AuthController.loginPage()` 返回登录页面
3. 用户输入用户名和密码
4. 提交到 `/login`
5. `AuthController.login()` 接收表单
6. `AuthService.authenticate()` 查数据库并校验密码
7. 登录成功则把 `currentUser` 保存到 session
8. 页面跳转到 `/dashboard`

### 11.3 退出流程

1. 用户点击退出
2. 访问 `/logout`
3. `AuthController.logout()` 清除 session
4. 返回 `/login`

---

## 12. 你答辩时可以怎么讲登录模块

你可以这样解释：

> 登录模块由登录页、注册页、AuthController、AuthService、UserRepository 和登录拦截器组成。注册时系统会先检查用户名和身份号是否重复，再将用户信息写入 users 表，默认角色是 USER。登录时系统根据用户名查询数据库，验证密码成功后把当前用户保存进 session。后续访问内部页面时，LoginInterceptor 会检查 session 中是否存在 currentUser，从而实现登录保护。

---

## 13. 你现在最应该记住的 5 个点

1. 登录表单提交的数据，先进入 DTO
2. Controller 只接请求，不负责复杂业务
3. 登录验证在 AuthService 中完成
4. 用户数据最终保存在 `users` 表
5. 登录成功后靠 session 保存状态

---

## 14. 推荐你的阅读顺序

建议按下面顺序读代码：

1. `templates/login.html`
2. `dto/LoginForm.java`
3. `controller/AuthController.java`
4. `service/AuthService.java`
5. `repository/UserRepository.java`
6. `config/LoginInterceptor.java`
7. `config/WebConfig.java`

如果这条线看懂了，登录模块你就基本掌握了。
