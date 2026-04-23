# Campus Fast Transfer System Design

## 1. Project Summary

### 1.1 Project Name

Campus Fast Transfer System

### 1.2 Project Positioning

This project is a campus-only file transfer and sharing web application for students and teachers. It helps users upload files, generate share codes, download shared files, and manage their own transfer history in a simple and organized way.

### 1.3 Problem Statement

In campus life, users frequently exchange assignments, presentation slides, registration forms, club materials, and temporary documents. Existing tools such as instant messaging apps often have weak file organization, expired links, or confusing chat-based retrieval. A dedicated campus file transfer system can provide a clearer upload-share-download workflow with account-based management.

### 1.4 Target Users

- Students who need to share homework, notes, and project files
- Teachers who need to distribute course materials
- Student organizations that need temporary internal file sharing
- Administrators who need to monitor platform usage

## 2. Rationale

The system is designed to solve a realistic campus communication problem: users need a lightweight way to transfer files inside the school without relying on chat history to find them later. The project is suitable for the course because it clearly demonstrates dynamic web development with frontend pages, backend business logic, login and session handling, and relational database design.

The project also matches the Part B requirement of building a useful service with a clear "Why" and "How":

- Why: campus users need a simple, organized, and reusable file transfer tool
- How: Spring Boot handles business logic, MySQL stores user and file metadata, and plain HTML/CSS/JavaScript provides a straightforward frontend

## 3. Scope

### 3.1 In Scope

- User registration, login, logout, and session-based authentication
- File upload and file metadata storage
- File listing and personal file management
- File download for authorized users
- Share code generation for uploaded files
- Download by share code
- Optional expiration time for shared files
- Administrator view for users and files

### 3.2 Out of Scope

- Chunk upload and breakpoint resume
- Cloud object storage integration
- Email verification and password reset by email
- Drag-and-drop upload
- Real-time notifications
- Complex visual effects or advanced responsive animation

## 4. Technical Stack

### 4.1 Frontend

- HTML5
- CSS3
- Vanilla JavaScript

### 4.2 Backend

- Spring Boot
- Spring MVC
- Session-based authentication

### 4.3 Database

- MySQL

### 4.4 Project Style

The frontend should be clean, readable, and practical rather than highly decorative. The design target is a simple academic project interface that is easy to demo and easy to maintain.

## 5. Core Features

### 5.1 User Account Module

- Register with username, password, and student or staff identity text
- Log in with username and password
- Maintain login state through session
- Log out and clear session
- Use a predefined seeded admin account for administration

### 5.2 File Upload Module

- Upload a file after login
- Store the file on the server filesystem
- Save file metadata in the database
- Record upload time, file size, owner, share status, and expiration time

### 5.3 Personal File Management Module

- View files uploaded by the current user
- Search or filter own files by file name
- Delete own files
- Generate or regenerate a share code for a file
- View file share status and expiration time

### 5.4 Share and Download Module

- Open a share page by entering a share code
- Validate whether the code exists and whether the file is expired
- Download the target file if the share is valid
- Record each download in a download log table

### 5.5 Administrator Module

- View all registered users
- View all uploaded files
- View file owner, upload time, share status, and expiration state
- Delete illegal or expired records if needed

## 6. Unique Points

The project is intentionally simple, but it still has several distinctive points for the course:

- Campus-only use case instead of a generic public transfer website
- Share-code-based download combined with login-based upload management
- Expiration control for temporary file sharing
- Download log records for later statistics and management
- Clear role distinction between normal users and administrator

## 7. Architecture

### 7.1 Overall Architecture

The system uses a classic three-layer structure:

- Presentation layer: HTML pages, CSS, and JavaScript for forms, tables, and interaction
- Business layer: Spring Boot controllers and services to handle login, upload, sharing, and management
- Data layer: MySQL tables for users, files, and download logs

Files themselves are stored on the server filesystem, while metadata is stored in MySQL. This keeps the database design simple and avoids storing large binary content in database rows for this course project.

### 7.2 Suggested Backend Modules

- `AuthController` / `AuthService`: register, login, logout
- `FileController` / `FileService`: upload, list, delete, download
- `ShareController` / `ShareService`: generate share code, validate share code
- `AdminController` / `AdminService`: user and file administration

### 7.3 Data Flow

Upload flow:

1. User logs in
2. User submits file upload form
3. Backend validates session and file
4. Backend stores the physical file on disk
5. Backend stores metadata in MySQL
6. Frontend refreshes the personal file list

Share download flow:

1. User enters a share code
2. Backend checks whether the share code exists
3. Backend checks whether the file is expired or deleted
4. Backend records the download log
5. Backend returns the file for download

## 8. Database Design

### 8.1 Entity Relationship Overview

There are three main entities:

- User
- FileRecord
- DownloadLog

Relationship summary:

- One user can upload many files
- One file can have many download log records
- One administrator can manage many user and file records logically, but admin operations do not need a separate table

### 8.2 ERD Description

`User (1) ---- (N) FileRecord`

`FileRecord (1) ---- (N) DownloadLog`

### 8.3 Table Design

#### User

- `id` bigint primary key
- `username` varchar unique
- `password` varchar
- `identity_no` varchar
- `role` varchar
- `created_at` datetime

Notes:

- `identity_no` can store student number or staff number
- self-registered accounts are stored as `USER`
- administrator accounts are created in advance by initialization data or database seed script

#### FileRecord

- `id` bigint primary key
- `original_name` varchar
- `stored_name` varchar
- `file_path` varchar
- `file_size` bigint
- `content_type` varchar
- `share_code` varchar unique
- `expire_time` datetime nullable
- `is_shared` boolean
- `owner_id` bigint foreign key references user(id)
- `uploaded_at` datetime
- `is_deleted` boolean

Notes:

- `stored_name` is used to avoid file name collisions on disk
- `expire_time` is nullable because some files may be shared without an expiration limit
- `is_deleted` supports soft deletion if desired; if implementation is simplified, hard deletion may also be used

#### DownloadLog

- `id` bigint primary key
- `file_id` bigint foreign key references file_record(id)
- `downloader_name` varchar
- `download_time` datetime

Notes:

- `downloader_name` can store the current username when logged in
- If anonymous code download is later added, this field can still store a text label such as `guest`

## 9. Page Flow

### 9.1 Main Pages

- Login page
- Register page
- User dashboard page
- Upload page
- My files page
- Share code access page
- Admin dashboard page

### 9.2 Page Navigation

Guest flow:

1. Open login page
2. Go to register page if no account exists
3. Log in successfully
4. Enter user dashboard

Normal user flow:

1. Open dashboard
2. Upload a file
3. View uploaded files
4. Generate a share code
5. Send share code to another campus user
6. Another user enters the share code page and downloads the file

Admin flow:

1. Log in with admin account
2. Enter admin dashboard
3. View all users and files
4. Manage abnormal or expired content

## 10. Session and Permission Design

The system should use HTTP session to maintain login state. After successful login, the backend stores the user ID and role in session.

Permission rules:

- Only logged-in users can upload files
- Only file owners can delete their own files
- Only administrators can access the admin dashboard
- Download by share code requires login and is allowed only when the file exists and is not expired

This rule keeps the project aligned with the campus-only positioning. Users still use a share code to locate the file quickly, but the actual download remains restricted to authenticated campus users.

## 11. Error Handling

The system should provide clear, simple error messages for common cases:

- Wrong username or password
- Registration failure because username already exists
- Upload failure because no file was selected
- Share code not found
- Share code expired
- Unauthorized page access
- File missing on server

Error responses do not need to be complex; a clear message block on the page is enough for this project.

## 12. Frontend Design Direction

The interface should use a simple and practical campus system style:

- Light background
- Clear header and navigation bar
- Card or table layout for file records
- Plain forms with obvious labels
- Limited color palette
- Responsive enough for laptop demo and basic mobile viewing

The priority is usability, not visual decoration. The layout should look orderly and professional enough for presentation.

## 13. Testing Plan

The following manual test cases should be prepared:

- User registration works with a new username
- Existing username cannot register again
- User can log in and log out
- Unauthenticated user cannot upload a file
- Logged-in user can upload and view a file
- User can generate a share code
- Valid share code allows download
- Expired share code blocks download
- Normal user cannot open admin page
- Administrator can view all users and files

## 14. Deliverables Mapping to Part B

### 14.1 Source Code

The source code will include frontend pages, Spring Boot backend logic, file storage logic, and MySQL database scripts.

### 14.2 Project Report

The report should include:

- Rationale: campus file transfer pain points and user groups
- Features: upload, share code, expiration, personal management, admin module
- Architecture: ERD and page flow
- Contribution: each teammate's responsibility
- Future work: limitations and next-step improvements

### 14.3 Presentation

A 10-minute presentation can be divided into:

- Problem and motivation
- System architecture
- Feature demo
- Database design
- Team contribution

## 15. Team Contribution Suggestion

For a 3-4 student group, a reasonable split is:

- Member A: login, registration, session control
- Member B: upload, download, file storage
- Member C: share code, expiration logic, file management
- Member D: frontend polish, admin page, report and presentation support

This split is only a planning suggestion and can be adjusted to match the real team.

## 16. Future Work

The demo version has several limitations that should be stated clearly in the report:

- No chunk upload or resume support for very large files
- Files are stored on a local server instead of distributed storage
- No email verification or password recovery
- Limited search and filtering features
- Basic security only, without antivirus scanning or stronger audit tools

Possible future improvements:

- Add chunk upload and breakpoint resume
- Add stricter campus identity verification
- Add file type preview for images or PDF files
- Add storage quota per user
- Add download statistics charts for administrators

## 17. Recommended Implementation Order

1. Build project structure and database tables
2. Complete register and login with session
3. Complete file upload and file listing
4. Complete file download and deletion
5. Complete share code and expiration logic
6. Complete administrator pages
7. Improve frontend layout and test all flows
