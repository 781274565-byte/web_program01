# Campus Fast Transfer System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Spring Boot + MySQL campus-only file transfer system with session login, upload/download, share-code access, expiration control, and an admin dashboard.

**Architecture:** Use a single Spring Boot MVC application with server-rendered Thymeleaf templates, static CSS/JS assets, and Spring Data JPA for persistence. Store physical files on the local filesystem and store user, file, and download-log metadata in MySQL so the project stays simple enough for a course demo while still covering full-stack requirements.

**Tech Stack:** Java 23, Spring Boot 3.x, Spring MVC, Thymeleaf, Spring Data JPA, MySQL, JUnit 5, Mockito, HTML5, CSS3, Vanilla JavaScript

---

## Planned File Map

**Create:**
- `pom.xml`
- `mvnw`
- `mvnw.cmd`
- `.gitignore`
- `src/main/java/com/campusfasttransfer/CampusFastTransferApplication.java`
- `src/main/java/com/campusfasttransfer/config/WebConfig.java`
- `src/main/java/com/campusfasttransfer/config/LoginInterceptor.java`
- `src/main/java/com/campusfasttransfer/controller/AuthController.java`
- `src/main/java/com/campusfasttransfer/controller/FileController.java`
- `src/main/java/com/campusfasttransfer/controller/ShareController.java`
- `src/main/java/com/campusfasttransfer/controller/AdminController.java`
- `src/main/java/com/campusfasttransfer/entity/User.java`
- `src/main/java/com/campusfasttransfer/entity/FileRecord.java`
- `src/main/java/com/campusfasttransfer/entity/DownloadLog.java`
- `src/main/java/com/campusfasttransfer/repository/UserRepository.java`
- `src/main/java/com/campusfasttransfer/repository/FileRecordRepository.java`
- `src/main/java/com/campusfasttransfer/repository/DownloadLogRepository.java`
- `src/main/java/com/campusfasttransfer/service/AuthService.java`
- `src/main/java/com/campusfasttransfer/service/FileService.java`
- `src/main/java/com/campusfasttransfer/service/ShareService.java`
- `src/main/java/com/campusfasttransfer/service/AdminService.java`
- `src/main/java/com/campusfasttransfer/dto/LoginForm.java`
- `src/main/java/com/campusfasttransfer/dto/RegisterForm.java`
- `src/main/java/com/campusfasttransfer/dto/UploadForm.java`
- `src/main/resources/application.yml`
- `src/main/resources/schema.sql`
- `src/main/resources/data.sql`
- `src/main/resources/templates/login.html`
- `src/main/resources/templates/register.html`
- `src/main/resources/templates/dashboard.html`
- `src/main/resources/templates/my-files.html`
- `src/main/resources/templates/share-access.html`
- `src/main/resources/templates/admin.html`
- `src/main/resources/static/css/app.css`
- `src/main/resources/static/js/app.js`
- `src/test/java/com/campusfasttransfer/AuthServiceTest.java`
- `src/test/java/com/campusfasttransfer/ShareServiceTest.java`
- `src/test/java/com/campusfasttransfer/FileControllerTest.java`

**Modify during implementation:**
- `pom.xml`
- `src/main/resources/application.yml`
- `src/main/resources/schema.sql`
- `src/main/resources/data.sql`
- `src/main/resources/static/css/app.css`
- `src/main/resources/static/js/app.js`

### Task 1: Bootstrap Repository and Spring Boot Skeleton

**Files:**
- Create: `.gitignore`
- Create: `pom.xml`
- Create: `src/main/java/com/campusfasttransfer/CampusFastTransferApplication.java`
- Create: `src/test/java/com/campusfasttransfer/CampusFastTransferApplicationTests.java`

- [ ] **Step 1: Initialize the git repository**

Run:

```powershell
& 'C:\Program Files\Git\cmd\git.exe' init
& 'C:\Program Files\Git\cmd\git.exe' branch -M main
```

Expected: a new `.git` folder exists and `git status` shows an empty repository on branch `main`.

- [ ] **Step 2: Create the Spring Boot starter project**

Run:

```powershell
curl.exe -G https://start.spring.io/starter.zip `
  --data-urlencode "type=maven-project" `
  --data-urlencode "language=java" `
  --data-urlencode "bootVersion=3.3.5" `
  --data-urlencode "baseDir=campus-fast-transfer" `
  --data-urlencode "groupId=com.campusfasttransfer" `
  --data-urlencode "artifactId=campus-fast-transfer" `
  --data-urlencode "name=campus-fast-transfer" `
  --data-urlencode "packageName=com.campusfasttransfer" `
  --data-urlencode "javaVersion=23" `
  --data-urlencode "dependencies=web,thymeleaf,data-jpa,validation,mysql" `
  -o starter.zip
Expand-Archive -Path .\starter.zip -DestinationPath .
Copy-Item .\campus-fast-transfer\* . -Recurse -Force
Remove-Item .\campus-fast-transfer -Recurse -Force
Remove-Item .\starter.zip -Force
```

Expected: `pom.xml`, `mvnw.cmd`, and `src/` exist in the project root.

- [ ] **Step 3: Add a simple `.gitignore`**

Create `E:\DAERXIAZUOYE\web_program\.gitignore`:

```gitignore
/target/
/.idea/
/.vscode/
*.iml
*.log
/.mvn/wrapper/maven-wrapper.jar
/uploads/
/.DS_Store
```

- [ ] **Step 4: Verify the generated project builds**

Run:

```powershell
.\mvnw.cmd test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit the clean starter**

Run:

```powershell
& 'C:\Program Files\Git\cmd\git.exe' add .
& 'C:\Program Files\Git\cmd\git.exe' commit -m "chore: bootstrap spring boot project"
```

Expected: one initial commit containing the generated project and `.gitignore`.

### Task 2: Add Database Schema, Seed Data, and Application Configuration

**Files:**
- Modify: `pom.xml`
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/schema.sql`
- Create: `src/main/resources/data.sql`

- [ ] **Step 1: Write the failing configuration test**

Create `src/test/java/com/campusfasttransfer/CampusFastTransferApplicationTests.java`:

```java
package com.campusfasttransfer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CampusFastTransferApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

Run:

```powershell
.\mvnw.cmd -Dtest=CampusFastTransferApplicationTests test
```

Expected: FAIL later if datasource configuration is incomplete.

- [ ] **Step 2: Add H2 as a test-only dependency**

Modify the dependency section in `pom.xml`:

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 3: Add runtime configuration and SQL scripts**

Create `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_fast_transfer?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: 123456
  jpa:
    hibernate:
      ddl-auto: none
    open-in-view: false
  sql:
    init:
      mode: always
server:
  port: 8080
app:
  upload-dir: uploads
```

Create `src/main/resources/schema.sql`:

```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    identity_no VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS file_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100),
    share_code VARCHAR(50) UNIQUE,
    expire_time DATETIME NULL,
    is_shared BOOLEAN NOT NULL,
    owner_id BIGINT NOT NULL,
    uploaded_at DATETIME NOT NULL,
    is_deleted BOOLEAN NOT NULL,
    CONSTRAINT fk_file_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS download_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id BIGINT NOT NULL,
    downloader_name VARCHAR(50) NOT NULL,
    download_time DATETIME NOT NULL,
    CONSTRAINT fk_log_file FOREIGN KEY (file_id) REFERENCES file_record(id)
);
```

Create `src/main/resources/data.sql`:

```sql
INSERT INTO users (username, password, identity_no, role, created_at)
SELECT 'admin', 'admin123', 'ADMIN0001', 'ADMIN', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);
```

- [ ] **Step 4: Make the test pass with a test profile**

Create `src/test/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: none
  sql:
    init:
      mode: always
```

Run:

```powershell
.\mvnw.cmd -Dtest=CampusFastTransferApplicationTests test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit the configuration baseline**

Run:

```powershell
& 'C:\Program Files\Git\cmd\git.exe' add pom.xml src/main/resources src/test/resources
& 'C:\Program Files\Git\cmd\git.exe' commit -m "feat: add database and application configuration"
```

### Task 3: Implement User Entity, Repository, and Authentication Service

**Files:**
- Create: `src/main/java/com/campusfasttransfer/entity/User.java`
- Create: `src/main/java/com/campusfasttransfer/repository/UserRepository.java`
- Create: `src/main/java/com/campusfasttransfer/dto/LoginForm.java`
- Create: `src/main/java/com/campusfasttransfer/dto/RegisterForm.java`
- Create: `src/main/java/com/campusfasttransfer/service/AuthService.java`
- Create: `src/test/java/com/campusfasttransfer/AuthServiceTest.java`

- [ ] **Step 1: Write the failing auth service test**

Create `src/test/java/com/campusfasttransfer/AuthServiceTest.java`:

```java
package com.campusfasttransfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.campusfasttransfer.entity.User;
import com.campusfasttransfer.repository.UserRepository;
import com.campusfasttransfer.service.AuthService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticateReturnsUserWhenPasswordMatches() {
        User user = new User(1L, "alice", "123456", "S123", "USER", LocalDateTime.now());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        Optional<User> result = authService.authenticate("alice", "123456");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("alice");
    }
}
```

Run:

```powershell
.\mvnw.cmd -Dtest=AuthServiceTest test
```

Expected: FAIL because the entity, repository, and service do not exist yet.

- [ ] **Step 2: Add the user domain classes**

Create `src/main/java/com/campusfasttransfer/entity/User.java`:

```java
package com.campusfasttransfer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String identityNo;
    private String role;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(Long id, String username, String password, String identityNo, String role, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.identityNo = identityNo;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getIdentityNo() { return identityNo; }
    public String getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setIdentityNo(String identityNo) { this.identityNo = identityNo; }
    public void setRole(String role) { this.role = role; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

Create `src/main/java/com/campusfasttransfer/repository/UserRepository.java`:

```java
package com.campusfasttransfer.repository;

import com.campusfasttransfer.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

Create `src/main/java/com/campusfasttransfer/dto/LoginForm.java`:

```java
package com.campusfasttransfer.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginForm {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

Create `src/main/java/com/campusfasttransfer/dto/RegisterForm.java`:

```java
package com.campusfasttransfer.dto;

import jakarta.validation.constraints.NotBlank;

public class RegisterForm {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String identityNo;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getIdentityNo() { return identityNo; }
    public void setIdentityNo(String identityNo) { this.identityNo = identityNo; }
}
```

- [ ] **Step 3: Implement the authentication service**

Create `src/main/java/com/campusfasttransfer/service/AuthService.java`:

```java
package com.campusfasttransfer.service;

import com.campusfasttransfer.dto.RegisterForm;
import com.campusfasttransfer.entity.User;
import com.campusfasttransfer.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
            .filter(user -> user.getPassword().equals(password));
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public User register(RegisterForm form) {
        User user = new User();
        user.setUsername(form.getUsername());
        user.setPassword(form.getPassword());
        user.setIdentityNo(form.getIdentityNo());
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}
```

- [ ] **Step 4: Run the auth tests**

Run:

```powershell
.\mvnw.cmd -Dtest=AuthServiceTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit the auth domain layer**

Run:

```powershell
& 'C:\Program Files\Git\cmd\git.exe' add src/main/java/com/campusfasttransfer/entity src/main/java/com/campusfasttransfer/repository src/main/java/com/campusfasttransfer/dto src/main/java/com/campusfasttransfer/service src/test/java/com/campusfasttransfer/AuthServiceTest.java
& 'C:\Program Files\Git\cmd\git.exe' commit -m "feat: implement user and auth service"
```

### Task 4: Implement Login, Registration, Session Guard, and Basic Pages

**Files:**
- Create: `src/main/java/com/campusfasttransfer/controller/AuthController.java`
- Create: `src/main/java/com/campusfasttransfer/config/LoginInterceptor.java`
- Create: `src/main/java/com/campusfasttransfer/config/WebConfig.java`
- Create: `src/main/resources/templates/login.html`
- Create: `src/main/resources/templates/register.html`
- Create: `src/main/resources/templates/dashboard.html`
- Create: `src/main/resources/static/css/app.css`

- [ ] **Step 1: Write the failing MVC authentication test**

Create `src/test/java/com/campusfasttransfer/FileControllerTest.java`:

```java
package com.campusfasttransfer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void dashboardRedirectsToLoginWhenSessionMissing() throws Exception {
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }
}
```

Run:

```powershell
.\mvnw.cmd -Dtest=FileControllerTest test
```

Expected: FAIL because the controller and interceptor are missing.

- [ ] **Step 2: Implement controller and session guard**

Create `src/main/java/com/campusfasttransfer/config/LoginInterceptor.java`:

```java
package com.campusfasttransfer.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object currentUser = request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            response.sendRedirect("/login");
            return false;
        }
        return true;
    }
}
```

Create `src/main/java/com/campusfasttransfer/config/WebConfig.java`:

```java
package com.campusfasttransfer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
            .addPathPatterns("/dashboard", "/files/**", "/admin/**")
            .excludePathPatterns("/login", "/register", "/css/**", "/js/**");
    }
}
```

Create `src/main/java/com/campusfasttransfer/controller/AuthController.java`:

```java
package com.campusfasttransfer.controller;

import com.campusfasttransfer.dto.LoginForm;
import com.campusfasttransfer.dto.RegisterForm;
import com.campusfasttransfer.entity.User;
import com.campusfasttransfer.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginForm loginForm, BindingResult bindingResult, HttpSession session, Model model) {
        if (bindingResult.hasErrors()) {
            return "login";
        }
        Optional<User> user = authService.authenticate(loginForm.getUsername(), loginForm.getPassword());
        if (user.isEmpty()) {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
        session.setAttribute("currentUser", user.get());
        return "redirect:/dashboard";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterForm registerForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        if (authService.usernameExists(registerForm.getUsername())) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }
        authService.register(registerForm);
        model.addAttribute("success", "Registration successful");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}
```

- [ ] **Step 3: Add the initial templates and styles**

Create `src/main/resources/templates/login.html`:

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
    <link rel="stylesheet" th:href="@{/css/app.css}">
</head>
<body>
<main class="container">
    <h1>Campus Fast Transfer</h1>
    <form method="post" th:action="@{/login}" th:object="${loginForm}" class="panel">
        <label>Username <input type="text" th:field="*{username}"></label>
        <label>Password <input type="password" th:field="*{password}"></label>
        <button type="submit">Login</button>
        <p class="error" th:text="${error}"></p>
    </form>
    <a th:href="@{/register}">Create account</a>
</main>
</body>
</html>
```

Create `src/main/resources/templates/register.html`:

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register</title>
    <link rel="stylesheet" th:href="@{/css/app.css}">
</head>
<body>
<main class="container">
    <h1>Register</h1>
    <form method="post" th:action="@{/register}" th:object="${registerForm}" class="panel">
        <label>Username <input type="text" th:field="*{username}"></label>
        <label>Password <input type="password" th:field="*{password}"></label>
        <label>Identity No <input type="text" th:field="*{identityNo}"></label>
        <button type="submit">Register</button>
        <p class="error" th:text="${error}"></p>
    </form>
</main>
</body>
</html>
```

Create `src/main/resources/templates/dashboard.html`:

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard</title>
    <link rel="stylesheet" th:href="@{/css/app.css}">
</head>
<body>
<main class="container">
    <h1>Dashboard</h1>
    <nav class="panel">
        <a th:href="@{/files}">My Files</a>
        <a th:href="@{/share}">Enter Share Code</a>
        <a th:href="@{/admin}">Admin</a>
        <a th:href="@{/logout}">Logout</a>
    </nav>
</main>
</body>
</html>
```

Create `src/main/resources/static/css/app.css`:

```css
body {
    margin: 0;
    font-family: "Segoe UI", sans-serif;
    background: #f5f7fb;
    color: #1f2937;
}

.container {
    max-width: 900px;
    margin: 40px auto;
    padding: 24px;
}

.panel {
    background: #ffffff;
    padding: 20px;
    border-radius: 12px;
    box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

label {
    display: block;
    margin-bottom: 12px;
}

input, button {
    width: 100%;
    padding: 10px 12px;
    margin-top: 6px;
    box-sizing: border-box;
}

.error {
    color: #c62828;
}
```

- [ ] **Step 4: Run the MVC test**

Run:

```powershell
.\mvnw.cmd -Dtest=FileControllerTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit the auth web flow**

Run:

```powershell
& 'C:\Program Files\Git\cmd\git.exe' add src/main/java/com/campusfasttransfer/controller src/main/java/com/campusfasttransfer/config src/main/resources/templates src/main/resources/static/css src/test/java/com/campusfasttransfer/FileControllerTest.java
& 'C:\Program Files\Git\cmd\git.exe' commit -m "feat: add login registration and session guard"
```

### Task 5: Implement File Entity, Upload, Listing, Owner Download, and Deletion

**Files:**
- Create: `src/main/java/com/campusfasttransfer/entity/FileRecord.java`
- Create: `src/main/java/com/campusfasttransfer/repository/FileRecordRepository.java`
- Create: `src/main/java/com/campusfasttransfer/dto/UploadForm.java`
- Create: `src/main/java/com/campusfasttransfer/service/FileService.java`
- Create: `src/main/java/com/campusfasttransfer/controller/FileController.java`
- Create: `src/main/resources/templates/my-files.html`

- [ ] **Step 1: Write the failing file service test**

Extend `src/test/java/com/campusfasttransfer/FileControllerTest.java` with:

```java
@Test
void filesPageRequiresLogin() throws Exception {
    mockMvc.perform(get("/files"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
}
```

Run:

```powershell
.\mvnw.cmd -Dtest=FileControllerTest test
```

Expected: FAIL because `/files` is not implemented.

- [ ] **Step 2: Add file entity and repository**

Create `src/main/java/com/campusfasttransfer/entity/FileRecord.java`:

```java
package com.campusfasttransfer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_record")
public class FileRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String originalName;
    private String storedName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private String shareCode;
    private LocalDateTime expireTime;
    private boolean isShared;
    private Long ownerId;
    private LocalDateTime uploadedAt;
    private boolean isDeleted;

    public Long getId() { return id; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getShareCode() { return shareCode; }
    public void setShareCode(String shareCode) { this.shareCode = shareCode; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
    public boolean isShared() { return isShared; }
    public void setShared(boolean shared) { isShared = shared; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
}
```

Create `src/main/java/com/campusfasttransfer/repository/FileRecordRepository.java`:

```java
package com.campusfasttransfer.repository;

import com.campusfasttransfer.entity.FileRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
    List<FileRecord> findByOwnerIdAndIsDeletedFalseOrderByUploadedAtDesc(Long ownerId);
    Optional<FileRecord> findByIdAndIsDeletedFalse(Long id);
    Optional<FileRecord> findByShareCodeAndIsDeletedFalse(String shareCode);
}
```

- [ ] **Step 3: Implement file service, controller, and page**

Create `src/main/java/com/campusfasttransfer/dto/UploadForm.java`:

```java
package com.campusfasttransfer.dto;

import java.time.LocalDateTime;
import org.springframework.web.multipart.MultipartFile;

public class UploadForm {
    private MultipartFile file;
    private LocalDateTime expireTime;

    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
}
```

Create `src/main/java/com/campusfasttransfer/service/FileService.java`:

```java
package com.campusfasttransfer.service;

import com.campusfasttransfer.entity.FileRecord;
import com.campusfasttransfer.repository.FileRecordRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private final FileRecordRepository fileRecordRepository;
    private final Path uploadDir;

    public FileService(FileRecordRepository fileRecordRepository, @Value("${app.upload-dir}") String uploadDir) throws IOException {
        this.fileRecordRepository = fileRecordRepository;
        this.uploadDir = Paths.get(uploadDir);
        Files.createDirectories(this.uploadDir);
    }

    public List<FileRecord> listUserFiles(Long ownerId) {
        return fileRecordRepository.findByOwnerIdAndIsDeletedFalseOrderByUploadedAtDesc(ownerId);
    }

    public Optional<FileRecord> findOwnedFile(Long fileId, Long ownerId) {
        return fileRecordRepository.findByIdAndIsDeletedFalse(fileId)
            .filter(file -> file.getOwnerId().equals(ownerId));
    }

    public FileRecord saveFile(Long ownerId, MultipartFile multipartFile, LocalDateTime expireTime) throws IOException {
        String storedName = UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();
        Path target = uploadDir.resolve(storedName);
        Files.copy(multipartFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        FileRecord record = new FileRecord();
        record.setOriginalName(multipartFile.getOriginalFilename());
        record.setStoredName(storedName);
        record.setFilePath(target.toString());
        record.setFileSize(multipartFile.getSize());
        record.setContentType(multipartFile.getContentType());
        record.setOwnerId(ownerId);
        record.setUploadedAt(LocalDateTime.now());
        record.setExpireTime(expireTime);
        record.setShared(false);
        record.setDeleted(false);
        return fileRecordRepository.save(record);
    }

    public Resource loadOwnedFile(Long fileId, Long ownerId) throws IOException {
        FileRecord record = findOwnedFile(fileId, ownerId)
            .orElseThrow(() -> new IllegalArgumentException("File not found"));
        InputStream inputStream = Files.newInputStream(Paths.get(record.getFilePath()));
        return new InputStreamResource(inputStream);
    }

    public void deleteOwnedFile(Long fileId, Long ownerId) {
        FileRecord record = findOwnedFile(fileId, ownerId)
            .orElseThrow(() -> new IllegalArgumentException("File not found"));
        record.setDeleted(true);
        fileRecordRepository.save(record);
    }
}
```

Create `src/main/java/com/campusfasttransfer/controller/FileController.java`:

```java
package com.campusfasttransfer.controller;

import com.campusfasttransfer.dto.UploadForm;
import com.campusfasttransfer.entity.FileRecord;
import com.campusfasttransfer.entity.User;
import com.campusfasttransfer.service.FileService;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/files")
    public String myFiles(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        model.addAttribute("files", fileService.listUserFiles(user.getId()));
        model.addAttribute("uploadForm", new UploadForm());
        return "my-files";
    }

    @PostMapping("/files/upload")
    public String upload(@ModelAttribute UploadForm uploadForm, HttpSession session, Model model) throws IOException {
        User user = (User) session.getAttribute("currentUser");
        if (uploadForm.getFile() == null || uploadForm.getFile().isEmpty()) {
            model.addAttribute("error", "Please select a file");
            return myFiles(session, model);
        }
        fileService.saveFile(user.getId(), uploadForm.getFile(), uploadForm.getExpireTime());
        return "redirect:/files";
    }

    @GetMapping("/files/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id, HttpSession session) throws IOException {
        User user = (User) session.getAttribute("currentUser");
        FileRecord fileRecord = fileService.findOwnedFile(id, user.getId())
            .orElseThrow(() -> new IllegalArgumentException("File not found"));
        Resource resource = fileService.loadOwnedFile(id, user.getId());
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileRecord.getOriginalName() + "\"")
            .body(resource);
    }

    @PostMapping("/files/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        fileService.deleteOwnedFile(id, user.getId());
        return "redirect:/files";
    }
}
```

Create `src/main/resources/templates/my-files.html`:

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Files</title>
    <link rel="stylesheet" th:href="@{/css/app.css}">
</head>
<body>
<main class="container">
    <h1>My Files</h1>
    <form method="post" enctype="multipart/form-data" th:action="@{/files/upload}" th:object="${uploadForm}" class="panel">
        <label>Select File <input type="file" th:field="*{file}"></label>
        <label>Expire Time <input type="datetime-local" th:field="*{expireTime}"></label>
        <button type="submit">Upload</button>
        <p class="error" th:text="${error}"></p>
    </form>
    <section class="panel">
        <table>
            <thead>
            <tr><th>Name</th><th>Size</th><th>Uploaded At</th><th>Actions</th></tr>
            </thead>
            <tbody>
            <tr th:each="file : ${files}">
                <td th:text="${file.originalName}"></td>
                <td th:text="${file.fileSize}"></td>
                <td th:text="${file.uploadedAt}"></td>
                <td>
                    <a th:href="@{'/files/download/' + ${file.id}}">Download</a>
                    <form method="post" th:action="@{'/files/delete/' + ${file.id}}" style="display:inline;">
                        <button type="submit">Delete</button>
                    </form>
                    <form method="post" th:action="@{'/share/enable/' + ${file.id}}" style="display:inline;">
                        <button type="submit">Share</button>
                    </form>
                </td>
            </tr>
            </tbody>
        </table>
    </section>
</main>
</body>
</html>
```

- [ ] **Step 4: Run the MVC test**

Run:

```powershell
.\mvnw.cmd -Dtest=FileControllerTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit upload and listing**

Run:

```powershell
& 'C:\Program Files\Git\cmd\git.exe' add src/main/java/com/campusfasttransfer/entity/FileRecord.java src/main/java/com/campusfasttransfer/repository/FileRecordRepository.java src/main/java/com/campusfasttransfer/dto/UploadForm.java src/main/java/com/campusfasttransfer/service/FileService.java src/main/java/com/campusfasttransfer/controller/FileController.java src/main/resources/templates/my-files.html src/test/java/com/campusfasttransfer/FileControllerTest.java
& 'C:\Program Files\Git\cmd\git.exe' commit -m "feat: add file upload and listing"
```

### Task 6: Implement Share Code, Expiration Checks, Share Download, and Download Logs

**Files:**
- Create: `src/main/java/com/campusfasttransfer/entity/DownloadLog.java`
- Create: `src/main/java/com/campusfasttransfer/repository/DownloadLogRepository.java`
- Create: `src/main/java/com/campusfasttransfer/service/ShareService.java`
- Create: `src/main/java/com/campusfasttransfer/controller/ShareController.java`
- Create: `src/main/resources/templates/share-access.html`
- Create: `src/test/java/com/campusfasttransfer/ShareServiceTest.java`

- [ ] **Step 1: Write the failing share logic test**

Create `src/test/java/com/campusfasttransfer/ShareServiceTest.java`:

```java
package com.campusfasttransfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.campusfasttransfer.entity.FileRecord;
import com.campusfasttransfer.repository.FileRecordRepository;
import com.campusfasttransfer.service.ShareService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShareServiceTest {

    @Mock
    private FileRecordRepository fileRecordRepository;

    @InjectMocks
    private ShareService shareService;

    @Test
    void validateShareRejectsExpiredFile() {
        FileRecord record = new FileRecord();
        record.setShareCode("ABC123");
        record.setShared(true);
        record.setExpireTime(LocalDateTime.now().minusDays(1));
        when(fileRecordRepository.findByShareCodeAndIsDeletedFalse("ABC123")).thenReturn(Optional.of(record));

        assertThat(shareService.validateShare("ABC123")).isEmpty();
    }
}
```

Run:

```powershell
.\mvnw.cmd -Dtest=ShareServiceTest test
```

Expected: FAIL because share logic does not exist yet.

- [ ] **Step 2: Add the log entity, repository, and share service**

Create `src/main/java/com/campusfasttransfer/entity/DownloadLog.java`:

```java
package com.campusfasttransfer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "download_log")
public class DownloadLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long fileId;
    private String downloaderName;
    private LocalDateTime downloadTime;

    public Long getId() { return id; }
    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }
    public String getDownloaderName() { return downloaderName; }
    public void setDownloaderName(String downloaderName) { this.downloaderName = downloaderName; }
    public LocalDateTime getDownloadTime() { return downloadTime; }
    public void setDownloadTime(LocalDateTime downloadTime) { this.downloadTime = downloadTime; }
}
```

Create `src/main/java/com/campusfasttransfer/repository/DownloadLogRepository.java`:

```java
package com.campusfasttransfer.repository;

import com.campusfasttransfer.entity.DownloadLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DownloadLogRepository extends JpaRepository<DownloadLog, Long> {
    List<DownloadLog> findByFileIdOrderByDownloadTimeDesc(Long fileId);
}
```

Create `src/main/java/com/campusfasttransfer/service/ShareService.java`:

```java
package com.campusfasttransfer.service;

import com.campusfasttransfer.entity.DownloadLog;
import com.campusfasttransfer.entity.FileRecord;
import com.campusfasttransfer.repository.DownloadLogRepository;
import com.campusfasttransfer.repository.FileRecordRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ShareService {

    private final FileRecordRepository fileRecordRepository;
    private final DownloadLogRepository downloadLogRepository;

    public ShareService(FileRecordRepository fileRecordRepository, DownloadLogRepository downloadLogRepository) {
        this.fileRecordRepository = fileRecordRepository;
        this.downloadLogRepository = downloadLogRepository;
    }

    public String enableShare(FileRecord record) {
        String shareCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        record.setShareCode(shareCode);
        record.setShared(true);
        fileRecordRepository.save(record);
        return shareCode;
    }

    public Optional<FileRecord> validateShare(String shareCode) {
        return fileRecordRepository.findByShareCodeAndIsDeletedFalse(shareCode)
            .filter(FileRecord::isShared)
            .filter(record -> record.getExpireTime() == null || record.getExpireTime().isAfter(LocalDateTime.now()));
    }

    public void logDownload(Long fileId, String username) {
        DownloadLog log = new DownloadLog();
        log.setFileId(fileId);
        log.setDownloaderName(username);
        log.setDownloadTime(LocalDateTime.now());
        downloadLogRepository.save(log);
    }

    public Resource loadSharedFile(FileRecord record) throws IOException {
        InputStream inputStream = Files.newInputStream(Paths.get(record.getFilePath()));
        return new InputStreamResource(inputStream);
    }
}
```

- [ ] **Step 3: Implement the share page and controller**

Create `src/main/java/com/campusfasttransfer/controller/ShareController.java`:

```java
package com.campusfasttransfer.controller;

import com.campusfasttransfer.entity.FileRecord;
import com.campusfasttransfer.entity.User;
import com.campusfasttransfer.service.FileService;
import com.campusfasttransfer.service.ShareService;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ShareController {

    private final ShareService shareService;
    private final FileService fileService;

    public ShareController(ShareService shareService, FileService fileService) {
        this.shareService = shareService;
        this.fileService = fileService;
    }

    @GetMapping("/share")
    public String sharePage() {
        return "share-access";
    }

    @PostMapping("/share/enable/{id}")
    public String enableShare(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        FileRecord fileRecord = fileService.findOwnedFile(id, currentUser.getId())
            .orElseThrow(() -> new IllegalArgumentException("File not found"));
        String shareCode = shareService.enableShare(fileRecord);
        return "redirect:/share?code=" + shareCode;
    }

    @PostMapping("/share")
    public String validate(@RequestParam String shareCode, HttpSession session, Model model) {
        Optional<FileRecord> fileRecord = shareService.validateShare(shareCode);
        if (fileRecord.isEmpty()) {
            model.addAttribute("error", "Share code not found or expired");
            return "share-access";
        }
        model.addAttribute("fileRecord", fileRecord.get());
        return "share-access";
    }

    @GetMapping("/share/download/{shareCode}")
    public ResponseEntity<Resource> downloadByShareCode(@PathVariable String shareCode, HttpSession session) throws IOException {
        User currentUser = (User) session.getAttribute("currentUser");
        FileRecord fileRecord = shareService.validateShare(shareCode)
            .orElseThrow(() -> new IllegalArgumentException("Share code not found or expired"));
        shareService.logDownload(fileRecord.getId(), currentUser.getUsername());
        Resource resource = shareService.loadSharedFile(fileRecord);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileRecord.getOriginalName() + "\"")
            .body(resource);
    }
}
```

Create `src/main/resources/templates/share-access.html`:

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Share Access</title>
    <link rel="stylesheet" th:href="@{/css/app.css}">
</head>
<body>
<main class="container">
    <h1>Share Code Access</h1>
    <form method="post" th:action="@{/share}" class="panel">
        <label>Share Code <input type="text" name="shareCode"></label>
        <button type="submit">Check File</button>
        <p class="error" th:text="${error}"></p>
    </form>
    <section class="panel" th:if="${fileRecord != null}">
        <p><strong>Name:</strong> <span th:text="${fileRecord.originalName}"></span></p>
        <p><strong>Size:</strong> <span th:text="${fileRecord.fileSize}"></span></p>
        <p><strong>Share Code:</strong> <span th:text="${fileRecord.shareCode}"></span></p>
        <a th:href="@{'/share/download/' + ${fileRecord.shareCode}}">Download Shared File</a>
    </section>
</main>
</body>
</html>
```

- [ ] **Step 4: Run the share test**

Run:

```powershell
.\mvnw.cmd -Dtest=ShareServiceTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit sharing and logging**

Run:

```powershell
& 'C:\Program Files\Git\cmd\git.exe' add src/main/java/com/campusfasttransfer/entity/DownloadLog.java src/main/java/com/campusfasttransfer/repository/DownloadLogRepository.java src/main/java/com/campusfasttransfer/service/ShareService.java src/main/java/com/campusfasttransfer/controller/ShareController.java src/main/resources/templates/share-access.html src/test/java/com/campusfasttransfer/ShareServiceTest.java
& 'C:\Program Files\Git\cmd\git.exe' commit -m "feat: add share validation and download logging"
```

### Task 7: Implement Admin Page and Final Frontend Cleanup

**Files:**
- Create: `src/main/java/com/campusfasttransfer/service/AdminService.java`
- Create: `src/main/java/com/campusfasttransfer/controller/AdminController.java`
- Create: `src/main/resources/templates/admin.html`
- Modify: `src/main/resources/static/css/app.css`
- Create: `src/main/resources/static/js/app.js`

- [ ] **Step 1: Write the failing admin access test**

Extend `src/test/java/com/campusfasttransfer/FileControllerTest.java` with:

```java
@Test
void adminRequiresLogin() throws Exception {
    mockMvc.perform(get("/admin"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
}
```

Run:

```powershell
.\mvnw.cmd -Dtest=FileControllerTest test
```

Expected: FAIL because `/admin` is not implemented.

- [ ] **Step 2: Implement admin service and controller**

Create `src/main/java/com/campusfasttransfer/service/AdminService.java`:

```java
package com.campusfasttransfer.service;

import com.campusfasttransfer.entity.FileRecord;
import com.campusfasttransfer.entity.User;
import com.campusfasttransfer.repository.FileRecordRepository;
import com.campusfasttransfer.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final FileRecordRepository fileRecordRepository;

    public AdminService(UserRepository userRepository, FileRecordRepository fileRecordRepository) {
        this.userRepository = userRepository;
        this.fileRecordRepository = fileRecordRepository;
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public List<FileRecord> listFiles() {
        return fileRecordRepository.findAll();
    }
}
```

Create `src/main/java/com/campusfasttransfer/controller/AdminController.java`:

```java
package com.campusfasttransfer.controller;

import com.campusfasttransfer.entity.User;
import com.campusfasttransfer.service.AdminService;
import jakarta.servlet.http.HttpSession;
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
    public String admin(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (!"ADMIN".equals(user.getRole())) {
            model.addAttribute("error", "Access denied");
            return "dashboard";
        }
        model.addAttribute("users", adminService.listUsers());
        model.addAttribute("files", adminService.listFiles());
        return "admin";
    }
}
```

- [ ] **Step 3: Add the admin template and small frontend enhancements**

Create `src/main/resources/templates/admin.html`:

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard</title>
    <link rel="stylesheet" th:href="@{/css/app.css}">
    <script defer th:src="@{/js/app.js}"></script>
</head>
<body>
<main class="container">
    <h1>Admin Dashboard</h1>
    <section class="panel">
        <h2>Users</h2>
        <ul>
            <li th:each="user : ${users}" th:text="${user.username + ' - ' + user.role}"></li>
        </ul>
    </section>
    <section class="panel">
        <h2>Files</h2>
        <ul>
            <li th:each="file : ${files}" th:text="${file.originalName + ' - ' + file.ownerId}"></li>
        </ul>
    </section>
</main>
</body>
</html>
```

Create `src/main/resources/static/js/app.js`:

```javascript
document.addEventListener("DOMContentLoaded", () => {
  const errorBox = document.querySelector(".error");
  if (errorBox && errorBox.textContent.trim() !== "") {
    errorBox.scrollIntoView({ behavior: "smooth", block: "center" });
  }
});
```

Append to `src/main/resources/static/css/app.css`:

```css
nav a {
    display: inline-block;
    margin-right: 12px;
    color: #1d4ed8;
    text-decoration: none;
}

table {
    width: 100%;
    border-collapse: collapse;
}

th, td {
    text-align: left;
    padding: 10px;
    border-bottom: 1px solid #e5e7eb;
}
```

- [ ] **Step 4: Run the MVC tests**

Run:

```powershell
.\mvnw.cmd -Dtest=FileControllerTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit the admin module**

Run:

```powershell
& 'C:\Program Files\Git\cmd\git.exe' add src/main/java/com/campusfasttransfer/service/AdminService.java src/main/java/com/campusfasttransfer/controller/AdminController.java src/main/resources/templates/admin.html src/main/resources/static/css/app.css src/main/resources/static/js/app.js src/test/java/com/campusfasttransfer/FileControllerTest.java
& 'C:\Program Files\Git\cmd\git.exe' commit -m "feat: add admin dashboard"
```

### Task 8: End-to-End Verification and Project Readiness

**Files:**
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/resources/templates/*.html`
- Modify: `src/main/resources/static/css/app.css`

- [ ] **Step 1: Run the full automated test suite**

Run:

```powershell
.\mvnw.cmd test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 2: Create the MySQL database and run the application**

Run:

```sql
CREATE DATABASE campus_fast_transfer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Then run:

```powershell
.\mvnw.cmd spring-boot:run
```

Expected: application starts on `http://localhost:8080`.

- [ ] **Step 3: Perform manual smoke tests**

Verify these actions:

```text
1. Register a normal user account
2. Log in with that account
3. Upload a file and confirm it appears in My Files
4. Open the share page and verify expired codes are rejected
5. Log in as admin/admin123 and open /admin
```

Expected: all five flows complete without server errors.

- [ ] **Step 4: Write the report assets**

Prepare these deliverables:

```text
- ERD from users, file_record, download_log
- Page flow diagram from login -> dashboard -> files/share/admin
- Contribution table for each teammate
- Future work list copied from the approved design spec
```

- [ ] **Step 5: Commit the verified project state**

Run:

```powershell
& 'C:\Program Files\Git\cmd\git.exe' add .
& 'C:\Program Files\Git\cmd\git.exe' commit -m "docs: verify application flow and prepare report assets"
```
