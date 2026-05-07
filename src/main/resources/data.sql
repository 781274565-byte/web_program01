/*如果users表里还没有用户名为admin的用户，就插入一条管理员账号数据，避免重复插入*/
INSERT INTO users (username, password, identity_no, role, created_at)
SELECT 'admin', 'admin123', 'ADMIN0001', 'ADMIN', NOW() /*用查询结果当作要插入的数据*/
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);
