# 管理员模块讲义

## 1. 这个模块解决什么问题

管理员模块负责解决：

- 如何区分普通用户和管理员
- 管理员如何查看系统中的全部用户
- 管理员如何查看系统中的全部文件

这个模块虽然代码量不大，但它体现了“权限控制”的思想。

也就是说：

- 普通用户只能看自己的内容
- 管理员可以看系统级数据

---

## 2. 这个模块涉及哪些文件

### 2.1 前端页面

- `src/main/resources/templates/admin.html`
- `src/main/resources/templates/dashboard.html`

### 2.2 Controller

- `src/main/java/com/campusfasttransfer/controller/AdminController.java`

### 2.3 Service

- `src/main/java/com/campusfasttransfer/service/AdminService.java`

### 2.4 Entity

- `src/main/java/com/campusfasttransfer/entity/User.java`
- `src/main/java/com/campusfasttransfer/entity/FileRecord.java`

### 2.5 Repository

- `src/main/java/com/campusfasttransfer/repository/UserRepository.java`
- `src/main/java/com/campusfasttransfer/repository/FileRecordRepository.java`

### 2.6 登录保护相关

- `src/main/java/com/campusfasttransfer/config/LoginInterceptor.java`
- `src/main/java/com/campusfasttransfer/config/WebConfig.java`

---

## 3. 管理员身份是如何区分的

管理员身份并不是单独一张表，而是由 `users` 表中的 `role` 字段决定。

例如：

- 普通用户：`USER`
- 管理员：`ADMIN`

系统初始化时会自动插入管理员账号：

- 用户名：`admin`
- 密码：`admin123`
- 角色：`ADMIN`

所以管理员和普通用户的本质区别，不在于页面不一样，而在于数据库中 `role` 不一样。

---

## 4. dashboard.html 为什么能显示 Admin 按钮

在首页中有这样一段代码：

```html
<a th:if="${currentUser != null and currentUser.role == 'ADMIN'}" th:href="@{/admin}">Admin</a>
```

这句的意思是：

- 如果当前用户已登录
- 并且当前用户角色是 `ADMIN`
- 才显示管理员入口

也就是说：

- 普通用户登录后，看不到这个按钮
- 管理员登录后，才能看到这个按钮

这是管理员功能的第一层控制：页面显示控制。

---

## 5. AdminController 是怎么保护管理员页面的

文件：

- `controller/AdminController.java`

这是管理员模块的核心入口。

### 5.1 先判断有没有登录

```java
User currentUser = (User) session.getAttribute("currentUser");
if (currentUser == null) {
    return "redirect:/login";
}
```

意思是：

- 如果 session 里没有当前用户
- 说明没有登录
- 直接跳回登录页

### 5.2 再判断是不是管理员

```java
if (!Objects.equals("ADMIN", currentUser.getRole())) {
    return "redirect:/dashboard";
}
```

意思是：

- 如果当前用户不是管理员
- 不允许进入管理员页
- 强制跳回首页

这是管理员功能的第二层控制：后端权限控制。

### 5.3 加载管理员页面数据

如果身份检查通过，就会调用：

```java
model.addAttribute("users", adminService.listUsers());
model.addAttribute("files", adminService.listFiles());
```

表示把：

- 所有用户
- 所有文件

放到页面中显示。

---

## 6. AdminService 是怎么取数据的

文件：

- `service/AdminService.java`

这个类很短，但很清楚。

### 6.1 查询用户列表

```java
public List<User> listUsers() {
    return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
}
```

作用：

- 查询全部用户
- 按创建时间倒序排列

### 6.2 查询文件列表

```java
public List<FileRecord> listFiles() {
    return fileRecordRepository.findAll(Sort.by(Sort.Direction.DESC, "uploadedAt"));
}
```

作用：

- 查询全部文件
- 按上传时间倒序排列

所以管理员看到的是“全站数据”，而不是自己的数据。

---

## 7. admin.html 页面是怎么显示数据的

管理员页面分两块：

1. 用户表格
2. 文件表格

### 7.1 用户表格

```html
<tr th:each="user : ${users}">
```

表示遍历所有用户。

显示内容有：

- 用户名
- 角色
- 创建时间

### 7.2 文件表格

```html
<tr th:each="file : ${files}">
```

表示遍历所有文件。

显示内容有：

- 文件名
- 所属用户 id
- 文件大小
- 文件状态
- 上传时间

其中状态显示这一句很值得注意：

```html
<td th:text="${file.deleted ? 'Deleted' : (file.shared ? 'Shared' : 'Private')}">Private</td>
```

意思是：

- 如果已删除，显示 `Deleted`
- 否则如果已分享，显示 `Shared`
- 否则显示 `Private`

这说明页面不仅在展示数据，还在做简单的状态判断。

---

## 8. 管理员模块和其他模块是什么关系

管理员模块并不是独立系统，它依赖整个项目之前已经存在的数据。

例如：

- 登录模块提供管理员身份
- 上传模块产生文件记录
- 分享模块改变文件的共享状态

管理员模块本身不产生这些数据，而是读取这些模块产生的数据。

你可以把它理解成：

“管理员模块是一个总览窗口。”

它把其他模块产生的结果集中显示出来。

---

## 9. 管理员模块完整流程

### 9.1 登录为管理员

1. 管理员用 `admin / admin123` 登录
2. 登录成功后，session 中保存 `currentUser`
3. 这个用户的 `role` 是 `ADMIN`

### 9.2 首页显示管理员入口

1. 系统进入 `/dashboard`
2. 页面判断当前用户角色是否为 `ADMIN`
3. 如果是，就显示 `Admin` 按钮

### 9.3 进入管理员页面

1. 管理员点击 `/admin`
2. `AdminController` 先检查是否已登录
3. 再检查角色是否为 `ADMIN`
4. 如果通过，就调用 `AdminService`
5. 加载全站用户和文件
6. 页面显示结果

---

## 10. 这个模块最值得你记住的设计点

1. 管理员身份靠 `role` 字段区分
2. 页面层和后端层都做了管理员控制
3. 管理员查看的是全站数据，不是个人数据
4. 管理员模块本质是“查询和展示”
5. 这个模块体现了权限控制思想

---

## 11. 你答辩时可以怎么讲管理员模块

你可以这样说：

> 管理员模块通过 users 表中的 role 字段区分普通用户和管理员。管理员登录后，在首页可以看到 Admin 入口。访问 /admin 时，后端会再次检查当前 session 用户是否存在，并验证角色是否为 ADMIN，防止普通用户越权访问。验证通过后，AdminService 会查询系统中的全部用户和全部文件，并在管理员页面中以表格形式展示。

---

## 12. 推荐你的阅读顺序

1. `templates/dashboard.html`
2. `templates/admin.html`
3. `controller/AdminController.java`
4. `service/AdminService.java`
5. `entity/User.java`
6. `entity/FileRecord.java`

只要看懂这几步，你就能把管理员模块讲清楚。
