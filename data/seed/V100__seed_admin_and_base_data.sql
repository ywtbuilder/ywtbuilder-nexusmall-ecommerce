-- Seed data: Default admin user (password: macro123)
-- BCrypt hash for 'macro123'

INSERT IGNORE INTO ums_admin (id, username, password, icon, email, nick_name, note, create_time, login_time, status)
VALUES (1, 'admin', '$2a$10$NZ5o7r2E.ayT2s7rCRfau.CDGnD7pAk2eDJJgsBUz8HJi4P3b/Bii', NULL, 'admin@mall.com', '系统管理员', '超级管理员', NOW(), NOW(), 1);

-- Default roles
INSERT IGNORE INTO ums_role (id, name, description, admin_count, create_time, status, sort)
VALUES
    (1, '超级管理员', '拥有所有权限', 1, NOW(), 1, 0),
    (2, '商品管理员', '商品相关权限', 0, NOW(), 1, 1),
    (3, '订单管理员', '订单相关权限', 0, NOW(), 1, 2);

-- Admin-role relation
INSERT IGNORE INTO ums_admin_role_relation (id, admin_id, role_id) VALUES (1, 1, 1);

-- Default member level
INSERT IGNORE INTO ums_member_level (id, name, growth_point, default_status, free_freight_point, comment_growth_point, priviledge_free_freight, priviledge_sign_in, priviledge_comment, priviledge_promotion, priviledge_member_price, priviledge_birthday, note)
VALUES
    (1, '普通会员',    0, 1, 199, 5, 0, 0, 0, 0, 0, 0, '默认等级'),
    (2, '银牌会员', 1000, 0,  99, 10, 1, 0, 1, 0, 0, 0, NULL),
    (3, '金牌会员', 5000, 0,  49, 20, 1, 1, 1, 1, 1, 0, NULL),
    (4, '白金会员',15000, 0,   0, 50, 1, 1, 1, 1, 1, 1, NULL);

-- Default return reasons
INSERT IGNORE INTO oms_order_return_reason (id, name, sort, status, create_time)
VALUES
    (1, '质量问题', 0, 1, NOW()),
    (2, '尺码太大', 1, 1, NOW()),
    (3, '尺码太小', 2, 1, NOW()),
    (4, '颜色不符', 3, 1, NOW()),
    (5, '其他原因', 4, 1, NOW()),
    (6, '不想要了', 5, 1, NOW()),
    (7, '发错货',   6, 1, NOW());
