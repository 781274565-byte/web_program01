# 上传模块讲义

## 1. 这个模块解决什么问题

上传模块负责解决下面几个问题：

- 用户如何上传文件
- 文件保存到哪里
- 文件信息如何记录到数据库
- 用户如何查看自己的文件
- 用户如何下载或删除自己的文件

这个模块是整个项目最像“核心业务”的部分，因为它真正处理了文件。

---

## 2. 这个模块涉及哪些文件

### 2.1 前端页面

- `src/main/resources/templates/my-files.html`

### 2.2 Controller

- `src/main/java/com/campusfasttransfer/controller/FileController.java`

### 2.3 Service

- `src/main/java/com/campusfasttransfer/service/FileService.java`

### 2.4 DTO

- `src/main/java/com/campusfasttransfer/dto/UploadForm.java`

### 2.5 Entity

- `src/main/java/com/campusfasttransfer/entity/FileRecord.java`

### 2.6 Repository

- `src/main/java/com/campusfasttransfer/repository/FileRecordRepository.java`

### 2.7 配置文件

- `src/main/resources/application.yml`

---

## 3. 上传模块对应的数据库表

上传模块主要操作的是 `file_record` 表。

这个表记录的不是文件内容本身，而是文件的“描述信息”。

关键字段如下：

- `id`：文件编号
- `original_name`：用户上传时的原始名字
- `stored_name`：服务器保存时的名字
- `file_path`：文件在磁盘里的完整路径
- `file_size`：文件大小
- `content_type`：文件类型
- `share_code`：分享码
- `expire_time`：过期时间
- `is_shared`：是否开启分享
- `owner_id`：该文件属于哪个用户
- `uploaded_at`：上传时间
- `is_deleted`：是否逻辑删除

这里要特别注意：

- 文件本体不存数据库
- 数据库存的是“文件信息”

---

## 4. 文件到底保存在哪里

在 `application.yml` 中有这样一个配置：

```yaml
app:
  upload-dir: uploads
```

意思是：

- 上传文件保存在项目根目录下的 `uploads` 文件夹

这说明这个项目采用的是：

“数据库存文件信息，磁盘存真实文件内容”

这是一种很常见的做法，因为如果把所有文件直接存到数据库，会更重、更慢，也更不方便管理。

---

## 5. my-files.html 是怎么工作的

这个页面有两大部分：

1. 上传表单
2. 文件列表

### 5.1 上传表单

关键代码：

```html
<form class="files-form" th:action="@{/files/upload}" th:object="${uploadForm}" method="post" enctype="multipart/form-data">
```

这里要注意 `enctype="multipart/form-data"`。  
这句非常重要，因为文件上传必须使用这种编码方式。

表单中有两个输入：

```html
<input type="file" th:field="*{file}">
<input type="datetime-local" th:field="*{expireTime}">
```

这表示：

- 文件输入框绑定 `uploadForm.file`
- 时间输入框绑定 `uploadForm.expireTime`

### 5.2 文件列表

下面的表格使用：

```html
<tr th:each="file : ${files}">
```

表示遍历后端传来的 `files` 列表，把每个文件显示成一行。

每一行都可以做三个动作：

- 下载
- 开启分享
- 删除

---

## 6. UploadForm 是怎么接收表单数据的

文件：

- `dto/UploadForm.java`

字段有两个：

- `MultipartFile file`
- `LocalDateTime expireTime`

### 6.1 MultipartFile 是什么

`MultipartFile` 可以理解成：

“Spring Boot 用来接收上传文件的对象”

它可以拿到：

- 文件内容
- 原始文件名
- 文件大小
- 文件类型

### 6.2 expireTime 是什么

它表示这个文件的过期时间。

如果用户设置了这个值，系统后面生成分享码或下载时就会检查：

- 文件是否已经过期

---

## 7. FileController 是怎么工作的

文件：

- `controller/FileController.java`

它负责 4 个主要动作：

- 查看我的文件
- 上传文件
- 下载自己的文件
- 删除自己的文件

### 7.1 查看我的文件

```java
@GetMapping("/files")
public String myFiles(HttpSession session, Model model)
```

逻辑是：

1. 从 session 中取出当前登录用户
2. 查询这个用户自己的文件列表
3. 放到 `model` 里
4. 返回 `my-files.html`

### 7.2 上传文件

```java
@PostMapping("/files/upload")
public String upload(...)
```

先检查：

- 表单有没有错误
- 用户有没有真正选择文件

如果没选文件，就重新回到页面并显示错误：

```java
populateFilesPage(model, currentUser, uploadForm, "Please select a file to upload.");
```

如果选了文件，就调用：

```java
fileService.saveUploadedFile(uploadForm.getFile(), uploadForm.getExpireTime(), currentUser.getId());
```

也就是说，Controller 不自己保存文件，而是把工作交给 `FileService`。

### 7.3 下载自己的文件

下载时要做两次检查：

1. 这个文件是不是当前用户自己的
2. 文件在磁盘上还存在不存在

如果不满足，就返回 `404`。

### 7.4 删除自己的文件

```java
fileService.softDeleteOwnedFile(id, currentUser.getId());
```

这里不是把磁盘文件直接物理删除，而是逻辑删除。

---

## 8. FileService 是上传模块的核心

文件：

- `service/FileService.java`

这个类最值得认真理解。

### 8.1 初始化上传目录

构造方法中：

```java
this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
Files.createDirectories(this.uploadDirectory);
```

作用是：

- 把 `uploads` 目录转换成规范路径
- 如果目录不存在，就自动创建

所以项目第一次运行时，上传目录会自动生成。

### 8.2 保存上传文件

核心方法：

```java
public FileRecord saveUploadedFile(MultipartFile file, LocalDateTime expireTime, Long ownerId)
```

这个方法做了很多事，可以拆成 6 步：

#### 第一步：处理原始文件名

```java
String originalName = normalizeOriginalName(file.getOriginalFilename());
```

作用：

- 去掉可能带的路径
- 只保留文件名本身

#### 第二步：生成服务器保存文件名

```java
String storedName = UUID.randomUUID() + "_" + sanitizeFileName(originalName);
```

作用：

- 防止同名文件互相覆盖
- 避免不安全字符

#### 第三步：计算文件最终保存路径

```java
Path storedPath = uploadDirectory.resolve(storedName).normalize();
```

表示：

- 把文件保存到 `uploads/随机名_原文件名`

#### 第四步：把文件内容复制到磁盘

```java
Files.copy(inputStream, storedPath, StandardCopyOption.REPLACE_EXISTING);
```

这是真正把文件写进磁盘的地方。

#### 第五步：创建 FileRecord 对象

保存完磁盘文件后，再把文件信息组装成一个 `FileRecord` 对象，例如：

- 原始文件名
- 服务器文件名
- 文件路径
- 文件大小
- 归属用户
- 上传时间

#### 第六步：把 FileRecord 保存到数据库

```java
return fileRecordRepository.save(record);
```

这样数据库就记住了这个文件的信息。

### 8.3 为什么要有清理逻辑

如果文件已经写到磁盘，但数据库保存失败，会出现：

- 磁盘有文件
- 数据库没有记录

这种情况会造成“脏文件”。

所以这里写了：

```java
Files.deleteIfExists(storedPath);
```

意思是：

- 如果数据库保存失败，就把刚才写入磁盘的文件删掉

这是一种很好的防御式编程。

### 8.4 查询自己的文件

```java
findByOwnerIdAndDeletedFalseOrderByUploadedAtDesc(ownerId)
```

意思是：

- 查当前用户的文件
- 只查没有被删除的
- 按上传时间倒序排列

### 8.5 逻辑删除文件

```java
fileRecord.setDeleted(true);
```

这里不是从数据库里真正删除记录，而是把 `is_deleted` 设为 `true`。

这种做法的好处是：

- 数据不会立刻丢失
- 后面还可以做审计或恢复

---

## 9. FileRecordRepository 是怎么工作的

文件：

- `repository/FileRecordRepository.java`

关键方法有：

```java
List<FileRecord> findByOwnerIdAndDeletedFalseOrderByUploadedAtDesc(Long ownerId);
Optional<FileRecord> findByIdAndDeletedFalse(Long id);
Optional<FileRecord> findByShareCodeAndDeletedFalse(String shareCode);
Optional<FileRecord> findByIdAndOwnerIdAndDeletedFalse(Long id, Long ownerId);
```

最重要的是：

- 查询文件时都带了“未删除”条件
- 某些查询还带了“当前用户 id”条件

这就避免了用户越权访问别人的文件。

---

## 10. 上传模块完整流程

### 10.1 打开文件页

1. 用户访问 `/files`
2. `FileController.myFiles()` 从 session 中取当前用户
3. 调用 `FileService.listOwnedFiles()`
4. 查询数据库中当前用户自己的文件
5. 返回 `my-files.html`

### 10.2 上传文件

1. 用户在页面选择文件和过期时间
2. 提交到 `/files/upload`
3. `FileController.upload()` 接收 `UploadForm`
4. 调用 `FileService.saveUploadedFile()`
5. 文件写入 `uploads` 目录
6. 文件记录写入 `file_record` 表
7. 重定向回 `/files`

### 10.3 下载文件

1. 用户点击下载按钮
2. 请求 `/files/download/{id}`
3. Controller 检查文件是不是属于当前用户
4. Service 从磁盘读取文件
5. 返回下载响应给浏览器

### 10.4 删除文件

1. 用户点击删除
2. 请求 `/files/delete/{id}`
3. Controller 检查文件归属
4. Service 将该文件标记为已删除
5. 页面重定向回 `/files`

---

## 11. 这个模块最值得你记住的设计点

1. 文件本体存磁盘，不存数据库
2. 数据库存的是文件信息
3. 上传时服务器文件名会被重新生成
4. 只有文件拥有者才能下载或删除自己的文件
5. 删除采用逻辑删除，而不是直接删库

---

## 12. 你答辩时可以怎么讲上传模块

你可以这样说：

> 上传模块由 my-files 页面、UploadForm、FileController、FileService、FileRecord 和 FileRecordRepository 组成。用户上传文件后，系统先把文件保存到本地 uploads 目录，再把文件名、路径、大小、归属用户、过期时间等信息保存到 file_record 表中。文件列表页面只显示当前登录用户自己的文件，下载和删除时也会根据当前用户 id 进行限制，防止越权访问。

---

## 13. 推荐你的阅读顺序

1. `templates/my-files.html`
2. `dto/UploadForm.java`
3. `controller/FileController.java`
4. `service/FileService.java`
5. `entity/FileRecord.java`
6. `repository/FileRecordRepository.java`

如果这几步看顺了，上传模块你就掌握得差不多了。
