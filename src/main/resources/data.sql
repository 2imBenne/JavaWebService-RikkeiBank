-- Thêm Roles mẫu (sử dụng INSERT IGNORE để không báo lỗi nếu chạy lại nhiều lần)
INSERT IGNORE INTO roles (id, name) VALUES (1, 'ROLE_ADMIN');
INSERT IGNORE INTO roles (id, name) VALUES (2, 'ROLE_STAFF');
INSERT IGNORE INTO roles (id, name) VALUES (3, 'ROLE_CUSTOMER');

-- Thêm Users mẫu (Mật khẩu cho cả hai tài khoản đều là: 123456)
-- Bcrypt hash của "123456" là "$2a$10$fFbC2Aev2V7IFHg5k5UjeuQ..G3EyPLQ2pXNDKfCjm.POWS1ke0LS"
INSERT IGNORE INTO users (id, email, password, username, role_id) 
VALUES (1, 'admin@rikkeibank.com', '$2a$10$fFbC2Aev2V7IFHg5k5UjeuQ..G3EyPLQ2pXNDKfCjm.POWS1ke0LS', 'admin', 1);

INSERT IGNORE INTO users (id, email, password, username, role_id) 
VALUES (2, 'customer@rikkeibank.com', '$2a$10$fFbC2Aev2V7IFHg5k5UjeuQ..G3EyPLQ2pXNDKfCjm.POWS1ke0LS', 'customer', 3);
