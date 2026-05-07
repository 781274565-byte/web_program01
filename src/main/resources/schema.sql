CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(255) NOT NULL,
    identity_no VARCHAR(32) NOT NULL,
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id), /*把id设为主键*/
    CONSTRAINT uk_users_username UNIQUE (username), /*给username加唯一约束，用户名不能重复*/
    CONSTRAINT uk_users_identity_no UNIQUE (identity_no) /*给identity_no加唯一约束，身份号不能重复*/
);

CREATE TABLE IF NOT EXISTS file_record (
    id BIGINT NOT NULL AUTO_INCREMENT,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100),
    share_code VARCHAR(50) UNIQUE,
    expire_time TIMESTAMP NULL,
    is_shared TINYINT(1) NOT NULL DEFAULT 0,
    owner_id BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id), /*把id设为主键*/
    CONSTRAINT fk_file_owner FOREIGN KEY (owner_id) REFERENCES users (id) /*把owner_id设为外键，指向users的id，表示每个文件都属于某个用户*/
);

CREATE TABLE IF NOT EXISTS download_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    file_id BIGINT NOT NULL,
    downloader_name VARCHAR(64) NOT NULL,
    download_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id), /*把id设为主键*/
    CONSTRAINT fk_log_file FOREIGN KEY (file_id) REFERENCES file_record (id) /*把file_id设为外键，关联到file_record，表示这条日志对应哪个文件*/
);

ALTER TABLE download_log MODIFY COLUMN downloader_name VARCHAR(64) NOT NULL;
