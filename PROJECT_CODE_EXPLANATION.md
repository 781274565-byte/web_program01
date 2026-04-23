# 校园内部文件快传项目代码讲解报告

## 1. 项目总览

这个项目是一个基于 `Spring Boot + MySQL + Thymeleaf + 原生 HTML/CSS/JS` 的全栈 Web 项目，主题是“校园内部文件快传”。

它的核心目标是：

- 让用户注册和登录系统
- 让用户上传、查看、下载、删除自己的文件
- 让用户为文件生成分享码
- 让其他登录用户通过分享码下载文件
- 让管理员查看系统中的用户和文件信息

从整体结构上看，这个项目并不是前后端分离项目，而是“后端渲染页面”的项目。也就是说：

- 前端页面由后端直接返回
- 页面里既有 HTML，也有 Thymeleaf 模板语法
- 后端用 Java 处理业务逻辑
- 数据库存储业务数据
- 文件内容保存在服务器本地磁盘

---

## 2. 项目可以分成哪几层

为了更容易理解整个系统，可以把它分成 5 层。

### 2.1 页面层

这一层就是用户在浏览器里直接看到的内容，例如：

- 登录页
- 注册页
- 文件列表页
- 分享页
- 管理员页

对应目录：

- `src/main/resources/templates`
- `src/main/resources/static/css`
- `src/main/resources/static/js`

### 2.2 控制器层 Controller

这一层负责接收浏览器发来的请求，并决定调用哪个业务方法。

例如：

- 用户访问 `/login`
- 用户提交 `/register`
- 用户访问 `/files`
- 用户提交 `/share`

对应目录：

- `src/main/java/com/campusfasttransfer/controller`

### 2.3 业务层 Service

这一层负责真正的业务逻辑。

例如：

- 检查用户名密码是否正确
- 生成分享码
- 把文件保存到本地
- 把文件信息写入数据库
- 记录下载日志

对应目录：

- `src/main/java/com/campusfasttransfer/service`

### 2.4 数据访问层 Repository

这一层负责和数据库打交道。

例如：

- 查找某个用户名是否存在
- 查询某个用户的文件列表
- 查找某个分享码对应的文件
- 保存下载日志

对应目录：

- `src/main/java/com/campusfasttransfer/repository`

### 2.5 数据库层

这一层负责真实保存数据。

对应文件：

- `src/main/resources/schema.sql`
- `src/main/resources/data.sql`

---

## 3. 项目启动时会发生什么

项目入口文件是：

- `src/main/java/com/campusfasttransfer/CampusFastTransferApplication.java`

核心代码只有一行：

```java
SpringApplication.run(CampusFastTransferApplication.class, args);
```

这句代码的作用是：启动整个 Spring Boot 应用。

启动时，程序会自动完成下面这些事情：

1. 读取配置文件 `application.yml`
2. 连接 MySQL 数据库
3. 创建并初始化数据表
4. 扫描 Controller、Service、Repository 等类
5. 启动内置 Web 服务器 Tomcat
6. 监听 `8080` 端口

所以浏览器访问：

```text
http://localhost:8080/login
```

本质上就是在访问这个 Java 项目提供的网页。

---

## 4. 配置文件是怎么控制项目的

配置文件是：

- `src/main/resources/application.yml`

它的主要作用有三个：

### 4.1 配置数据库

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_fast_transfer?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: 123456
```

意思是：

- 项目连接本机 MySQL
- 数据库名叫 `campus_fast_transfer`
- 用户名是 `root`
- 密码是 `123456`

### 4.2 配置端口

```yaml
server:
  port: 8080
```

意思是：项目运行在 `8080` 端口。

### 4.3 配置上传目录

```yaml
app:
  upload-dir: uploads
```

意思是：用户上传的文件，会保存到项目根目录下的 `uploads` 文件夹中。

---

## 5. 数据库设计说明

这个项目一共有 3 张核心表。

### 5.1 users 表

作用：保存用户账号信息。

主要字段：

- `id`：用户编号
- `username`：用户名
- `password`：密码
- `identity_no`：身份号
- `role`：角色
- `created_at`：创建时间

一个用户在系统中一定有一个角色：

- 普通用户：`USER`
- 管理员：`ADMIN`

### 5.2 file_record 表

作用：保存文件信息。

主要字段：

- `id`：文件编号
- `original_name`：原始文件名
- `stored_name`：保存在服务器上的文件名
- `file_path`：文件路径
- `file_size`：文件大小
- `content_type`：文件类型
- `share_code`：分享码
- `expire_time`：过期时间
- `is_shared`：是否已开启分享
- `owner_id`：文件属于哪个用户
- `uploaded_at`：上传时间
- `is_deleted`：是否被逻辑删除

### 5.3 download_log 表

作用：记录共享文件的下载情况。

主要字段：

- `id`：记录编号
- `file_id`：下载的是哪个文件
- `downloader_name`：下载者用户名
- `download_time`：下载时间

### 5.4 三张表之间的关系

可以这样理解：

- 一个用户可以上传多个文件
- 一个文件可以被多次下载
- 每次下载会生成一条日志

也就是说：

- `users` 和 `file_record` 是“一对多”
- `file_record` 和 `download_log` 也是“一对多”

### 5.5 默认管理员数据

`data.sql` 里会自动插入一个管理员：

- 用户名：`admin`
- 密码：`admin123`

这样系统启动后，不需要手动创建管理员账号。

---

## 6. Entity、DTO、Repository 分别是什么

这是理解 Spring Boot 项目的关键知识点。

### 6.1 Entity

Entity 可以理解成：数据库表在 Java 中的映射对象。

对应类：

- `entity/User.java`
- `entity/FileRecord.java`
- `entity/DownloadLog.java`

例如在 `User.java` 中：

```java
@Column(nullable = false, length = 64)
private String username;
```

意思是：

- 这个 Java 字段叫 `username`
- 它对应数据库中的一个列
- 不能为空
- 最大长度 64

所以，Entity 的本质就是：

“把数据库里的一行数据，变成 Java 对象。”

### 6.2 DTO

DTO 可以理解成：接收页面表单数据的中间对象。

对应类：

- `dto/LoginForm.java`
- `dto/RegisterForm.java`
- `dto/UploadForm.java`

例如登录只需要：

- 用户名
- 密码

所以 `LoginForm` 只有这两个字段。

DTO 不一定直接对应数据库，它更多是为了接收页面传过来的数据。

### 6.3 Repository

Repository 是访问数据库的接口。

对应类：

- `repository/UserRepository.java`
- `repository/FileRecordRepository.java`
- `repository/DownloadLogRepository.java`

例如：

```java
Optional<User> findByUsername(String username);
```

这句虽然没有手写 SQL，但 Spring Data JPA 会自动帮你生成“按用户名查询用户”的数据库操作。

所以 Repository 的作用就是：

“帮 Service 查数据库和写数据库。”

---

## 7. 配置类是怎么保护登录状态的

配置类主要有两个：

- `config/LoginInterceptor.java`
- `config/WebConfig.java`

### 7.1 LoginInterceptor

它的核心逻辑是：

```java
HttpSession session = request.getSession(false);
if (session == null || session.getAttribute("currentUser") == null) {
    response.sendRedirect(request.getContextPath() + "/login");
    return false;
}
```

意思是：

- 先看 session 是否存在
- 再看 session 中是否有 `currentUser`
- 如果没有，说明用户没登录
- 没登录就强制跳转到 `/login`

### 7.2 WebConfig

它的作用是配置哪些路径需要登录后才能访问。

例如：

- `/dashboard`
- `/files`
- `/share`
- `/admin`

这些页面都需要登录。

但是下面这些不需要登录：

- `/login`
- `/register`
- `/css/**`
- `/js/**`

所以这两个类配合起来，实现了“未登录无法进入系统内部页面”。

---

## 8. Controller 层详细说明

Controller 层负责接收浏览器请求。

### 8.1 AuthController

文件：

- `controller/AuthController.java`

作用：

- 打开登录页
- 提交登录
- 打开注册页
- 提交注册
- 退出登录
- 打开首页

#### 登录流程

```java
authService.authenticate(loginForm.getUsername(), loginForm.getPassword())
```

意思是：把用户名和密码交给 `AuthService` 去验证。

如果成功：

```java
session.setAttribute("currentUser", user);
```

这句非常重要，它表示：

- 把当前登录用户放进 session
- 后续页面通过 session 知道“现在是谁登录了”

#### 注册流程

注册时，Controller 并不直接操作数据库，而是调用：

```java
authService.register(registerForm);
```

如果注册成功，页面会重定向回登录页。

### 8.2 FileController

文件：

- `controller/FileController.java`

作用：

- 打开“我的文件”页面
- 上传文件
- 下载用户自己的文件
- 删除用户自己的文件

这里有一个非常重要的设计：

- 下载和删除时，必须带上当前用户的 id
- 只能操作属于自己的文件

也就是说，这不是“任何人都能下载任何文件”，而是有所有权控制的。

### 8.3 ShareController

文件：

- `controller/ShareController.java`

作用：

- 显示分享页
- 为文件开启分享
- 输入分享码查询文件
- 通过分享码下载文件

它和 `FileController` 的区别是：

- `FileController` 处理的是“我自己的文件”
- `ShareController` 处理的是“通过分享码访问文件”

### 8.4 AdminController

文件：

- `controller/AdminController.java`

作用：

- 打开管理员页面
- 判断当前用户是不是管理员
- 加载全站用户和文件数据

这里用了角色判断：

```java
if (!Objects.equals("ADMIN", currentUser.getRole())) {
    return "redirect:/dashboard";
}
```

意思是：

- 如果当前用户不是管理员
- 就不能进入管理员页面

---

## 9. Service 层详细说明

Service 层是整个项目真正处理业务逻辑的地方。

### 9.1 AuthService

文件：

- `service/AuthService.java`

作用：

- 登录验证
- 用户注册

#### 登录逻辑

```java
return userRepository.findByUsername(username)
        .filter(user -> Objects.equals(password, user.getPassword()));
```

意思是：

1. 先按用户名查用户
2. 再比较密码是否相同
3. 如果相同，返回这个用户
4. 如果不同，返回空结果

#### 注册逻辑

注册时先检查：

- 用户名是否已存在
- 身份号是否已存在

然后创建新用户：

- `username`
- `password`
- `identityNo`
- `role = USER`
- `createdAt = 当前时间`

最后保存到数据库。

### 9.2 FileService

文件：

- `service/FileService.java`

作用：

- 初始化上传目录
- 保存文件到磁盘
- 保存文件信息到数据库
- 查询当前用户文件列表
- 加载文件供下载
- 逻辑删除文件

#### 这个类最重要的设计思想

这个项目里：

- 文件本体放在本地磁盘
- 文件信息放在数据库

例如用户上传一个 PDF：

- PDF 真正保存到 `uploads` 文件夹
- 数据库保存它的名字、路径、大小、归属用户

#### 为什么要生成新的文件名

```java
String storedName = UUID.randomUUID() + "_" + sanitizeFileName(originalName);
```

这是为了防止重名。

如果两个用户都上传 `report.pdf`，原始文件名会一样。  
所以服务器保存时要生成唯一文件名，避免覆盖。

#### 为什么有 `sanitizeFileName`

```java
private String sanitizeFileName(String originalName) {
    return originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
}
```

作用是：

- 把危险或不规范字符替换掉
- 避免路径问题
- 提高文件保存安全性

### 9.3 ShareService

文件：

- `service/ShareService.java`

作用：

- 生成分享码
- 校验分享码
- 加载共享文件
- 写下载日志

#### 生成分享码

```java
shareCode = UUID.randomUUID().toString().replace("-", "")
        .substring(0, 8)
        .toUpperCase(Locale.ROOT);
```

它会生成一个 8 位大写随机字符串，例如：

```text
F70AF890
```

#### 校验分享码

校验时会检查：

- 分享码是否存在
- 文件是否未删除
- 文件是否已开启分享
- 文件是否未过期

如果这些条件都满足，分享码才算有效。

#### 写下载日志

```java
downloadLog.setFileId(fileId);
downloadLog.setDownloaderName(username);
downloadLog.setDownloadTime(LocalDateTime.now());
```

意思是：

- 哪个文件被下载了
- 谁下载的
- 什么时候下载的

### 9.4 AdminService

文件：

- `service/AdminService.java`

作用：

- 查询所有用户
- 查询所有文件

这个类本身不复杂，但它的存在非常重要。  
因为它说明管理员功能也被单独封装成了自己的业务层。

---

## 10. 前端模板页面是怎么工作的

这个项目的页面不是纯静态 HTML，而是 Thymeleaf 模板。

也就是说：

- HTML 决定页面结构
- Thymeleaf 把后端数据插入页面

### 10.1 login.html

作用：登录页面。

关键代码：

```html
<form th:action="@{/login}" th:object="${loginForm}" method="post">
```

意思是：

- 表单提交到 `/login`
- 这个表单对应后端的 `loginForm`

输入框：

```html
<input type="text" th:field="*{username}">
<input type="password" th:field="*{password}">
```

表示这两个输入框分别绑定：

- `loginForm.username`
- `loginForm.password`

### 10.2 register.html

作用：注册页面。

绑定字段：

- `username`
- `password`
- `identityNo`

这正好对应 `RegisterForm` 中的 3 个字段。

### 10.3 dashboard.html

作用：登录成功后的首页。

它会显示：

- 当前登录用户名
- 文件入口
- 分享入口
- 管理员入口
- 退出入口

这里有一段条件显示：

```html
<a th:if="${currentUser != null and currentUser.role == 'ADMIN'}" th:href="@{/admin}">Admin</a>
```

意思是：只有管理员才会看到 Admin 按钮。

### 10.4 my-files.html

作用：文件管理页面。

包含两部分：

1. 上传表单
2. 文件列表表格

上传表单对应：

- `UploadForm.file`
- `UploadForm.expireTime`

文件列表使用：

```html
<tr th:each="file : ${files}">
```

表示遍历后端传来的 `files` 集合，把每个文件显示成一行。

### 10.5 share-access.html

作用：分享码查询页面。

它有两个用途：

1. 显示刚生成的分享码
2. 让用户输入分享码查询文件

如果查询成功，还会显示：

- 文件名
- 文件大小
- 分享码
- 下载按钮

### 10.6 admin.html

作用：管理员页面。

它分成两块：

- 用户表格
- 文件表格

管理员打开这个页面后，可以看到：

- 系统中有哪些用户
- 每个文件是谁上传的
- 文件是否已共享或已删除

---

## 11. CSS 和 JS 在这个项目中的作用

### 11.1 app.css

文件：

- `static/css/app.css`

作用：控制页面样式。

它做了这些事：

- 定义颜色变量
- 设置背景色和卡片阴影
- 设置表单输入框样式
- 设置按钮样式
- 设置表格样式
- 设置移动端响应式布局

所以 CSS 负责的是：

“页面好不好看、排版是否整齐、手机上是否能正常显示。”

### 11.2 app.js

文件：

- `static/js/app.js`

目前它只实现了一个小功能：

- 如果某个按钮带有 `data-confirm` 属性
- 点击按钮时先弹出确认框

这个 JS 不是主流程核心，但它体现了前端行为增强的思路。

---

## 12. 一次完整请求是怎么从前端走到数据库的

这一部分是最重要的，因为它能把整个项目串起来。

### 12.1 登录流程

1. 浏览器访问 `/login`
2. `AuthController.loginPage()` 返回 `login.html`
3. 用户输入用户名和密码后提交表单
4. `AuthController.login()` 接收 `LoginForm`
5. `AuthService.authenticate()` 调用 `UserRepository.findByUsername()`
6. 数据库查找用户
7. 如果用户名密码正确，把用户保存进 session
8. 重定向到 `/dashboard`

### 12.2 上传文件流程

1. 用户打开 `/files`
2. `FileController.myFiles()` 查询当前用户文件列表
3. 页面显示已有文件
4. 用户选择文件并提交上传
5. `FileController.upload()` 接收 `UploadForm`
6. `FileService.saveUploadedFile()` 把文件保存到 `uploads`
7. 同时把文件信息保存到 `file_record` 表
8. 页面重定向回 `/files`

### 12.3 开启分享流程

1. 用户点击“Enable Share”
2. `ShareController.enableShare()` 先确认该文件属于当前用户
3. `ShareService.enableShare()` 生成分享码
4. 文件记录被更新：
   - `is_shared = true`
   - `share_code = 随机码`
5. 页面跳转到 `/share?code=分享码`

### 12.4 分享码下载流程

1. 另一个用户打开 `/share`
2. 输入分享码
3. `ShareController.lookupShare()` 调用 `ShareService.validateShare()`
4. 系统检查分享码是否有效、文件是否过期
5. 如果有效，页面显示文件信息
6. 用户点击下载
7. `ShareController.downloadSharedFile()` 调用 `ShareService.loadSharedFile()`
8. 文件返回给浏览器下载
9. `ShareService.logDownload()` 把下载行为写入 `download_log`

### 12.5 管理员查看后台流程

1. 管理员登录
2. 访问 `/admin`
3. `AdminController.adminPage()` 判断当前用户角色是不是 `ADMIN`
4. `AdminService.listUsers()` 查询用户列表
5. `AdminService.listFiles()` 查询文件列表
6. 数据显示在 `admin.html`

---

## 13. 这个项目最核心的几个联系

理解这个项目，关键不是记住所有代码，而是记住这些联系。

### 13.1 HTML 表单字段和 DTO 的联系

例如：

```html
<input type="text" th:field="*{username}">
```

对应：

```java
private String username;
```

所以前端表单字段和后端 DTO 字段是一一对应的。

### 13.2 Controller 和 Service 的联系

Controller 不直接处理复杂业务，而是调用 Service。

这表示：

- Controller 负责接收请求
- Service 负责真正处理业务

### 13.3 Service 和 Repository 的联系

Service 不直接写 SQL，而是调用 Repository。

这表示：

- Service 关注业务逻辑
- Repository 关注数据库操作

### 13.4 Entity 和数据库表的联系

例如：

- `User` 对应 `users`
- `FileRecord` 对应 `file_record`
- `DownloadLog` 对应 `download_log`

### 13.5 数据库和磁盘文件的联系

这一点非常重要：

- 文件内容存在磁盘
- 文件信息存在数据库

这是一种非常常见的文件系统设计方式。

---

## 14. 项目中你最应该掌握的知识点

如果你是大二学生，只需要先重点吃透下面这些内容：

- 什么是 Controller、Service、Repository 三层结构
- 什么是 session 登录
- 什么是 Thymeleaf 模板绑定
- 什么是数据库表和 Java 类映射
- 什么是文件上传与下载
- 什么是分享码功能
- 什么是管理员权限控制

你不需要一开始就把 Spring Boot 所有细节都搞懂。  
先把“数据和请求是怎么流动的”搞清楚，已经很不错了。

---

## 15. 如果老师问“这个项目是怎么工作的”，可以这样回答

可以参考下面这段说明：

> 本项目使用 Spring Boot 作为后端框架，Thymeleaf 作为模板引擎，MySQL 作为数据库。用户登录后，系统会把当前用户信息保存到 session 中，再通过拦截器保护系统内部页面。用户上传文件时，文件本体会保存到服务器本地的 uploads 目录，文件信息会保存到 MySQL 的 file_record 表中。用户可以为文件生成分享码，其他登录用户可以通过分享码查询并下载文件，系统同时会把下载记录保存到 download_log 表中。管理员账号可以访问 /admin 页面，查看全站用户信息和文件信息。

这段话能说顺，基本说明你已经理解了整个项目。

---

## 16. 推荐你的复习顺序

为了更容易掌握代码，建议你按下面顺序学习：

### 第一步：先看登录模块

重点文件：

- `templates/login.html`
- `controller/AuthController.java`
- `service/AuthService.java`
- `repository/UserRepository.java`

目标：理解表单提交、登录验证、session 保存。

### 第二步：再看上传模块

重点文件：

- `templates/my-files.html`
- `controller/FileController.java`
- `service/FileService.java`
- `entity/FileRecord.java`

目标：理解文件上传后“磁盘保存 + 数据库存信息”的思路。

### 第三步：最后看分享模块

重点文件：

- `templates/share-access.html`
- `controller/ShareController.java`
- `service/ShareService.java`
- `entity/DownloadLog.java`

目标：理解分享码生成、校验、下载日志记录。

### 第四步：补看管理员模块

重点文件：

- `templates/admin.html`
- `controller/AdminController.java`
- `service/AdminService.java`

目标：理解角色权限控制。

---

## 17. 总结

这个项目虽然看起来文件不少，但核心思路其实很清楚：

- 前端页面负责展示和提交表单
- Controller 接收请求
- Service 处理业务逻辑
- Repository 访问数据库
- 数据库保存用户、文件、日志
- 磁盘保存真实上传文件

如果你已经能看懂这几条主线：

- 登录怎么完成
- 文件怎么上传
- 分享码怎么生成
- 下载日志怎么记录
- 管理员怎么查看数据

那么你其实已经真正理解了这个项目的大部分结构。
