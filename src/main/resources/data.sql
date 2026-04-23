INSERT INTO users (username, password, identity_no, role, created_at)
SELECT 'admin', 'admin123', 'ADMIN0001', 'ADMIN', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);
