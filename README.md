# 校园内部文件快传

## 1. 项目简介

本项目是一个基于 `Spring Boot + MySQL + Thymeleaf + 原生 HTML/CSS/JS` 实现的全栈 Web 应用，主题为“校园内部文件快传”。

系统面向校园内部用户，提供文件上传、个人文件管理、分享码访问、分享下载记录以及管理员查看系统数据等功能，适合作为课程全栈 Web 项目展示。

## 2. 核心功能

- 用户注册与登录
- 文件上传与个人文件列表管理
- 文件下载与逻辑删除
- 通过分享码开启文件共享
- 通过分享码查询并下载共享文件
- 自动记录共享文件的下载者信息
- 管理员查看用户列表和文件列表

## 3. 技术栈

- 后端：Spring Boot 3、Spring MVC、Spring Data JPA
- 前端：Thymeleaf、原生 HTML、CSS、JavaScript
- 数据库：MySQL 8
- 测试：Spring Boot Test、H2
- 构建工具：Maven Wrapper

## 4. 适用场景

本系统适用于校园内部的小型文件传输场景，例如：

- 学生之间传递课程资料
- 小组成员共享实验文档
- 校园内部临时分享文件

## 5. 功能页面

- `/login`：登录页
- `/register`：注册页
- `/dashboard`：系统首页
- `/files`：我的文件页面
- `/share`：分享码访问页面
- `/admin`：管理员页面

## 6. 数据库设计

项目运行时会自动执行 `src/main/resources/schema.sql` 和 `src/main/resources/data.sql`。

主要数据表如下：

- `users`
  - 保存用户账号、密码、身份号、角色、创建时间
- `file_record`
  - 保存上传文件信息、物理路径、大小、分享码、过期时间、所属用户等
- `download_log`
  - 保存共享文件下载记录和下载者用户名

默认会初始化一个管理员账号：

- 用户名：`admin`
- 密码：`admin123`

## 7. 运行环境要求

- JDK 21 或更高版本
- MySQL 8.x
- Maven Wrapper（项目已自带，无需单独安装 Maven）

## 8. 数据库配置

当前项目默认数据库配置位于 [application.yml](E:/DAERXIAZUOYE/web_program/.worktrees/campus-fast-transfer-bootstrap/src/main/resources/application.yml)：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_fast_transfer?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: 123456
```

请先确保本地 MySQL 服务已经启动，并提前创建数据库：

```sql
CREATE DATABASE campus_fast_transfer
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

如果你的 MySQL 用户名或密码不同，可以自行修改 `application.yml`。

## 9. 本地启动步骤

### 9.1 启动 MySQL

保证本地 MySQL 正在运行，并且 `campus_fast_transfer` 数据库已经创建完成。

### 9.2 进入项目目录

```powershell
cd E:\DAERXIAZUOYE\web_program\.worktrees\campus-fast-transfer-bootstrap
```

### 9.3 设置 JDK

如果本机已经正确配置 `JAVA_HOME`，可以跳过此步骤。

Windows PowerShell 示例：

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-23"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

### 9.4 启动项目

```powershell
.\mvnw.cmd spring-boot:run
```

启动成功后访问：

```text
http://localhost:8080/login
```

## 10. 快速启动脚本

如果你希望在这台 Windows 机器上更省事地启动项目，可以直接使用项目根目录下的快速启动脚本：

- [start-local.cmd](E:/DAERXIAZUOYE/web_program/.worktrees/campus-fast-transfer-bootstrap/start-local.cmd)
- [start-local.ps1](E:/DAERXIAZUOYE/web_program/.worktrees/campus-fast-transfer-bootstrap/start-local.ps1)

脚本会自动完成以下操作：

- 检查并使用 `C:\Program Files\Java\jdk-23`
- 检查 MySQL 是否已经监听 `3306`
- 如果 MySQL 未启动，则尝试自动拉起 `mysqld`
- 如果项目未运行，则在新 PowerShell 窗口中启动 Spring Boot
- 启动成功后自动打开登录页

如果你是直接双击运行，优先使用：

```text
start-local.cmd
```

如果你是在 PowerShell 中运行，可以使用：

```powershell
.\start-local.ps1
```

## 11. 测试运行

项目测试使用 H2 内存数据库，不依赖本地 MySQL。

```powershell
.\mvnw.cmd test
```

## 12. 演示建议流程

答辩或展示时可以按下面顺序演示：

1. 使用普通用户注册并登录
2. 上传一个文件
3. 为文件生成分享码
4. 使用另一个用户输入分享码并下载文件
5. 切换管理员账号登录
6. 在管理员页面查看用户信息和文件信息

## 13. 项目结构

```text
campus-fast-transfer-bootstrap
├─ src
│  ├─ main
│  │  ├─ java/com/campusfasttransfer
│  │  │  ├─ config
│  │  │  ├─ controller
│  │  │  ├─ dto
│  │  │  ├─ entity
│  │  │  ├─ repository
│  │  │  └─ service
│  │  └─ resources
│  │     ├─ static
│  │     ├─ templates
│  │     ├─ application.yml
│  │     ├─ schema.sql
│  │     └─ data.sql
│  └─ test
├─ uploads
├─ pom.xml
└─ README.md
```

## 14. 项目特点

- 前后端结构清晰，适合课程项目展示
- 使用 MySQL 持久化存储业务数据
- 使用 Thymeleaf 完成服务端渲染页面
- 支持分享码和下载日志，体现完整业务流程
- 管理员页面可展示系统级数据，满足全栈课程项目要求

## 15. 当前已验证内容

在当前开发环境中，以下流程已经完成手动冒烟验证：

- 普通用户注册成功
- 普通用户登录成功
- 文件上传成功
- 分享码生成成功
- 通过分享码查询文件成功
- 共享文件下载成功
- 下载日志记录成功
- 管理员页面访问成功

## 16. 补充说明

- 上传文件默认保存在项目根目录下的 `uploads` 文件夹中
- 分享码为系统自动生成的 8 位大写字符串
- 文件过期后将不能继续启用分享或通过分享码访问
- 管理员账号由 `data.sql` 自动初始化
