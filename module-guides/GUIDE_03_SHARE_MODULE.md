# 分享模块讲义

## 1. 这个模块解决什么问题

分享模块负责解决：

- 用户如何给自己的文件生成分享码
- 其他用户如何输入分享码查看文件
- 其他用户如何下载共享文件
- 系统如何记录下载日志

你可以把它理解成：

“在已有文件管理功能基础上，增加一个受控分享能力。”

---

## 2. 这个模块涉及哪些文件

### 2.1 前端页面

- `src/main/resources/templates/share-access.html`
- `src/main/resources/templates/my-files.html`

### 2.2 Controller

- `src/main/java/com/campusfasttransfer/controller/ShareController.java`

### 2.3 Service

- `src/main/java/com/campusfasttransfer/service/ShareService.java`

### 2.4 Entity

- `src/main/java/com/campusfasttransfer/entity/FileRecord.java`
- `src/main/java/com/campusfasttransfer/entity/DownloadLog.java`

### 2.5 Repository

- `src/main/java/com/campusfasttransfer/repository/FileRecordRepository.java`
- `src/main/java/com/campusfasttransfer/repository/DownloadLogRepository.java`

---

## 3. 分享功能依赖哪些数据库字段

分享功能主要依赖 `file_record` 表中的 3 个字段：

- `share_code`
- `is_shared`
- `expire_time`

这 3 个字段一起决定：

- 文件有没有开启分享
- 分享码是什么
- 分享是否已经过期

此外，下载日志保存在 `download_log` 表中，字段有：

- `file_id`
- `downloader_name`
- `download_time`

---

## 4. 前端页面是怎么配合分享功能的

### 4.1 my-files.html 中的分享按钮

在文件列表中，每行都有一个按钮：

```html
<form th:action="@{|/share/enable/${file.id}|}" method="post">
    <button class="button-link" type="submit" th:text="${file.shared ? 'Refresh Share' : 'Enable Share'}">Enable Share</button>
</form>
```

这句表示：

- 点击按钮后，请求 `/share/enable/{文件id}`
- 如果文件未分享，显示 `Enable Share`
- 如果文件已分享，显示 `Refresh Share`

### 4.2 share-access.html 的作用

这个页面承担了两个任务：

1. 显示刚生成的分享码
2. 让用户输入分享码查询文件

页面上有一个输入框：

```html
<input type="text" name="shareCode" th:value="${shareCode}" placeholder="Enter share code">
```

用户输入分享码后提交到：

```html
<form class="files-form" th:action="@{/share}" method="post">
```

如果查询成功，页面会显示：

- 文件名
- 文件大小
- 分享码
- 下载按钮

---

## 5. ShareController 是怎么工作的

文件：

- `controller/ShareController.java`

这个类主要处理 4 个动作：

- 打开分享页
- 给文件开启分享
- 根据分享码查文件
- 根据分享码下载文件

### 5.1 打开分享页

```java
@GetMapping("/share")
public String sharePage(...)
```

这个方法负责把页面需要的数据准备好，例如：

- 当前用户
- 当前分享码
- 错误信息

如果链接里带了参数：

- `?code=ABCD1234`
- `?error=xxxx`

页面就会显示成功提示或错误提示。

### 5.2 开启分享

```java
@PostMapping("/share/enable/{id}")
public String enableShare(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes)
```

这个方法会先做一件非常重要的事：

```java
Optional<FileRecord> fileRecord = fileService.findOwnedFile(id, currentUser.getId());
```

意思是：

- 只能给“自己拥有的文件”开启分享

如果不是自己的文件，就不能开启分享。

然后再调用：

```java
String shareCode = shareService.enableShare(fileRecord.get());
```

生成分享码成功后，页面会跳转到：

```text
/share?code=分享码
```

### 5.3 查询分享码

```java
@PostMapping("/share")
public String lookupShare(...)
```

这个方法先判断：

- 用户有没有输入分享码

如果没有，就提示错误。

如果有，就调用：

```java
Optional<FileRecord> fileRecord = shareService.validateShare(shareCode);
```

如果分享码有效，就把文件信息放到页面里显示出来。

### 5.4 下载共享文件

```java
@GetMapping("/share/download/{shareCode}")
public ResponseEntity<Resource> downloadSharedFile(...)
```

这个方法会做 3 件事：

1. 检查分享码是否有效
2. 从磁盘中加载文件
3. 记录下载日志

其中最关键的一句是：

```java
shareService.logDownload(fileRecord.get().getId(), currentUser.getUsername());
```

也就是说，每次共享文件被下载时，系统都会记录是谁下载的。

---

## 6. ShareService 是分享模块的核心

文件：

- `service/ShareService.java`

这个类负责：

- 生成分享码
- 检查分享码
- 读取共享文件
- 记录下载日志

### 6.1 enableShare 方法

```java
public String enableShare(FileRecord record)
```

第一步先检查文件是否过期：

```java
if (!isNotExpired(record)) {
    throw new IllegalStateException("Cannot enable sharing for an expired file.");
}
```

意思是：

- 如果文件已经过期
- 不允许开启分享

然后调用 `generateUniqueShareCode()` 生成随机码，并保存到数据库中：

- `shareCode`
- `shared = true`

### 6.2 generateUniqueShareCode 方法

```java
shareCode = UUID.randomUUID().toString().replace("-", "")
        .substring(0, 8)
        .toUpperCase(Locale.ROOT);
```

它会生成一个 8 位大写随机字符串。

然后再循环检查这个分享码是否已经被用过：

```java
while (fileRecordRepository.findByShareCodeAndDeletedFalse(shareCode).isPresent())
```

如果重复，就重新生成。

所以最终得到的是“未被占用的随机分享码”。

### 6.3 validateShare 方法

这个方法是分享模块的“校验核心”。

它会依次检查：

1. 分享码是否为空
2. 数据库中有没有这条记录
3. 这个文件是否已开启分享
4. 文件是否未过期

只有全部通过，才返回该文件。

### 6.4 loadSharedFile 方法

作用是：

- 根据文件记录计算文件路径
- 检查磁盘文件是否存在
- 返回文件资源给浏览器下载

### 6.5 logDownload 方法

作用是：

- 创建一条新的下载日志
- 保存下载者用户名
- 保存下载时间

这样管理员或系统以后就能知道：

- 哪个文件被下载了
- 谁下载的
- 下载时间是什么时候

---

## 7. 分享模块完整流程

### 7.1 开启分享流程

1. 用户进入“我的文件”页面
2. 点击某个文件的 `Enable Share`
3. 请求 `/share/enable/{id}`
4. Controller 检查文件是不是当前用户自己的
5. Service 检查文件是否过期
6. 生成唯一分享码
7. 更新数据库中 `share_code` 和 `is_shared`
8. 页面跳转到 `/share?code=生成的分享码`

### 7.2 分享码查询流程

1. 用户进入 `/share`
2. 输入分享码
3. 提交到 `/share`
4. Controller 调用 `ShareService.validateShare()`
5. 系统检查分享码是否存在、文件是否已共享、文件是否未删除、是否未过期
6. 如果通过，页面显示文件信息

### 7.3 分享码下载流程

1. 用户点击下载共享文件
2. 请求 `/share/download/{shareCode}`
3. 系统再次校验分享码
4. 从磁盘中读取文件
5. 返回给浏览器下载
6. 同时写入 `download_log`

---

## 8. 这个模块最值得你记住的设计点

1. 分享码是随机生成的，不是用户自己填的
2. 分享码必须唯一
3. 文件过期后不能继续分享
4. 共享下载前必须再次校验分享码
5. 每次下载共享文件都会记录日志

---

## 9. 你答辩时可以怎么讲分享模块

可以这样说：

> 分享模块在已有文件上传功能基础上增加了分享能力。用户只能对自己的文件开启分享，系统会为文件生成唯一的 8 位大写分享码，并把共享状态和分享码写入 file_record 表中。其他登录用户在分享页输入分享码后，系统会检查该分享码是否存在、文件是否已共享、是否未删除以及是否未过期。校验通过后可以下载文件，系统还会把下载者用户名和下载时间写入 download_log 表，实现共享文件下载记录功能。

---

## 10. 推荐你的阅读顺序

1. `templates/my-files.html`
2. `templates/share-access.html`
3. `controller/ShareController.java`
4. `service/ShareService.java`
5. `entity/FileRecord.java`
6. `entity/DownloadLog.java`
7. `repository/FileRecordRepository.java`
8. `repository/DownloadLogRepository.java`

如果这条主线看明白，分享模块你就理解得很扎实了。
