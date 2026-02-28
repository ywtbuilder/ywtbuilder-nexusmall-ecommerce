-- =================================================================
-- Mall V3 — Sample Product Seed Data (for development)
-- Run AFTER V100 base data has been seeded
-- =================================================================

-- ══════════════════ Brands ══════════════════
INSERT IGNORE INTO pms_brand (id, name, first_letter, sort, factory_status, show_status, product_count, logo, brand_story)
VALUES
    (1,  'Apple',    'A', 0, 1, 1, 3, '/images/brand/apple.png',    'Think Different'),
    (2,  'Samsung',  'S', 1, 1, 1, 2, '/images/brand/samsung.png',  'Inspire the World'),
    (3,  'Nike',     'N', 2, 1, 1, 2, '/images/brand/nike.png',     'Just Do It'),
    (4,  'Huawei',   'H', 3, 1, 1, 2, '/images/brand/huawei.png',   '构建万物互联的智能世界'),
    (5,  'Xiaomi',   'X', 4, 1, 1, 1, '/images/brand/xiaomi.png',   '让每个人都能享受科技的乐趣');

-- ══════════════════ Categories (2 levels) ══════════════════
-- Level 0 (top)
INSERT IGNORE INTO pms_product_category (id, parent_id, name, level, product_count, show_status, nav_status, sort, icon)
VALUES
    (1,  0, '手机数码',   0, 5, 1, 1, 0, 'phone'),
    (2,  0, '电脑办公',   0, 2, 1, 1, 1, 'laptop'),
    (3,  0, '运动户外',   0, 2, 1, 1, 2, 'sport'),
    (4,  0, '家用电器',   0, 1, 1, 1, 3, 'home');

-- Level 1 (sub)
INSERT IGNORE INTO pms_product_category (id, parent_id, name, level, product_count, show_status, nav_status, sort)
VALUES
    (11, 1, '手机通讯',   1, 3, 1, 0, 0),
    (12, 1, '手机配件',   1, 2, 1, 0, 1),
    (21, 2, '笔记本',     1, 1, 1, 0, 0),
    (22, 2, '平板电脑',   1, 1, 1, 0, 1),
    (31, 3, '运动鞋',     1, 1, 1, 0, 0),
    (32, 3, '运动服饰',   1, 1, 1, 0, 1),
    (41, 4, '智能穿戴',   1, 1, 1, 0, 0);

-- ══════════════════ Product Attribute Categories ══════════════════
INSERT IGNORE INTO pms_product_attribute_category (id, name, attribute_count, param_count)
VALUES
    (1, '手机',     3, 2),
    (2, '笔记本',   2, 2),
    (3, '运动鞋',   2, 1);

-- ══════════════════ Product Attributes ══════════════════
-- type: 0=规格, 1=参数
INSERT IGNORE INTO pms_product_attribute (id, product_attribute_category_id, name, select_type, input_type, input_list, sort, filter_type, search_type, related_status, hand_add_status, type)
VALUES
    -- 手机规格
    (1, 1, '颜色',   0, 1, '黑色,白色,蓝色,金色',     0, 0, 0, 0, 0, 0),
    (2, 1, '存储容量', 0, 1, '128GB,256GB,512GB,1TB',  1, 0, 0, 0, 0, 0),
    (3, 1, '版本',   0, 1, '标准版,Pro,Pro Max',       2, 0, 0, 0, 0, 0),
    -- 手机参数
    (4, 1, '屏幕尺寸', 0, 0, NULL, 3, 0, 0, 0, 0, 1),
    (5, 1, '电池容量', 0, 0, NULL, 4, 0, 0, 0, 0, 1),
    -- 笔记本规格
    (6, 2, '颜色',   0, 1, '银色,深空灰,午夜色',       0, 0, 0, 0, 0, 0),
    (7, 2, '内存',   0, 1, '8GB,16GB,32GB',            1, 0, 0, 0, 0, 0),
    -- 笔记本参数
    (8, 2, '处理器',  0, 0, NULL, 2, 0, 0, 0, 0, 1),
    (9, 2, '屏幕尺寸', 0, 0, NULL, 3, 0, 0, 0, 0, 1),
    -- 运动鞋规格
    (10, 3, '颜色',  0, 1, '黑色,白色,红色',            0, 0, 0, 0, 0, 0),
    (11, 3, '尺码',  0, 1, '38,39,40,41,42,43,44',     1, 0, 0, 0, 0, 0),
    -- 运动鞋参数
    (12, 3, '鞋面材质', 0, 0, NULL, 2, 0, 0, 0, 0, 1);

-- ══════════════════ Products (10 items) ══════════════════
INSERT IGNORE INTO pms_product
    (id, brand_id, product_category_id, product_attribute_category_id, name, pic, product_sn,
     delete_status, publish_status, new_status, recommand_status, verify_status, sort, sale,
     price, promotion_price, sub_title, original_price, stock, unit, brand_name, product_category_name,
     description, detail_title, keywords)
VALUES
    (1, 1, 11, 1, 'iPhone 15 Pro Max',
     '/images/product/iphone15promax.jpg', 'APPLE-IP15PM-001',
     0, 1, 1, 1, 1, 0, 1280,
     9999.00, 9499.00, 'A17 Pro 芯片 | 钛金属设计 | 4800 万像素', 10999.00, 500, '台',
     'Apple', '手机通讯',
     'iPhone 15 Pro Max，搭载 A17 Pro 芯片，全新钛金属设计。',
     'iPhone 15 Pro Max 详情', 'iPhone,苹果,A17,钛金属'),

    (2, 1, 11, 1, 'iPhone 15',
     '/images/product/iphone15.jpg', 'APPLE-IP15-001',
     0, 1, 1, 0, 1, 1, 3500,
     5999.00, 5699.00, 'A16 芯片 | 灵动岛 | 4800 万像素', 6299.00, 800, '台',
     'Apple', '手机通讯',
     'iPhone 15，全新设计，搭载 A16 芯片。',
     'iPhone 15 详情', 'iPhone,苹果,A16'),

    (3, 4, 11, 1, 'Huawei Mate 60 Pro',
     '/images/product/mate60pro.jpg', 'HW-M60P-001',
     0, 1, 1, 1, 1, 2, 950,
     6999.00, 6799.00, '麒麟芯片 | 卫星通信 | 昆仑玻璃', 7299.00, 300, '台',
     'Huawei', '手机通讯',
     '华为 Mate 60 Pro，突破性科技创新。',
     'Mate 60 Pro 详情', 'Huawei,华为,Mate60,麒麟'),

    (4, 2, 11, 1, 'Samsung Galaxy S24 Ultra',
     '/images/product/galaxys24ultra.jpg', 'SS-GS24U-001',
     0, 1, 0, 0, 1, 3, 620,
     9699.00, NULL, 'Galaxy AI | S Pen | 2 亿像素', 9699.00, 400, '台',
     'Samsung', '手机通讯',
     'Galaxy S24 Ultra，AI 智能体验。',
     'Galaxy S24 Ultra 详情', 'Samsung,三星,Galaxy,S24'),

    (5, 5, 11, 1, 'Xiaomi 14 Pro',
     '/images/product/xiaomi14pro.jpg', 'XM-14P-001',
     0, 1, 0, 1, 1, 4, 2100,
     4299.00, 3999.00, '骁龙 8 Gen 3 | 徕卡影像 | 小米澎湃 OS', 4599.00, 600, '台',
     'Xiaomi', '手机通讯',
     '小米 14 Pro，全面升级。',
     'Xiaomi 14 Pro 详情', 'Xiaomi,小米,骁龙,徕卡'),

    (6, 1, 21, 2, 'MacBook Pro 14 M3 Pro',
     '/images/product/macbookpro14.jpg', 'APPLE-MBP14-001',
     0, 1, 1, 1, 1, 0, 890,
     14999.00, NULL, 'M3 Pro 芯片 | 18GB 内存 | Liquid Retina XDR', 14999.00, 200, '台',
     'Apple', '笔记本',
     'MacBook Pro 14 寸，搭载 M3 Pro 芯片。',
     'MacBook Pro 14 详情', 'MacBook,苹果,M3,Pro'),

    (7, 4, 22, 2, 'Huawei MatePad Pro 13.2',
     '/images/product/matepadpro.jpg', 'HW-MPP132-001',
     0, 1, 0, 0, 1, 1, 340,
     5699.00, 5399.00, '星闪连接 | 柔性 OLED | 天生会画', 5899.00, 250, '台',
     'Huawei', '平板电脑',
     '华为 MatePad Pro 13.2 英寸，创作利器。',
     'MatePad Pro 详情', 'Huawei,华为,MatePad,平板'),

    (8, 3, 31, 3, 'Nike Air Max 270',
     '/images/product/airmax270.jpg', 'NIKE-AM270-001',
     0, 1, 0, 1, 1, 0, 5600,
     899.00, 699.00, '270 度气垫 | 轻量缓震', 999.00, 1000, '双',
     'Nike', '运动鞋',
     'Nike Air Max 270，极致舒适。',
     'Air Max 270 详情', 'Nike,AirMax,运动鞋,气垫'),

    (9, 3, 32, 3, 'Nike Dri-FIT 运动T恤',
     '/images/product/nikedrifit.jpg', 'NIKE-DF-001',
     0, 1, 0, 0, 1, 1, 12000,
     299.00, NULL, '速干透气 | 轻盈舒适', 349.00, 2000, '件',
     'Nike', '运动服饰',
     'Nike Dri-FIT 运动T恤，速干透气。',
     'Dri-FIT T恤详情', 'Nike,DriFIT,运动,T恤'),

    (10, 2, 41, 1, 'Samsung Galaxy Watch 6',
     '/images/product/galaxywatch6.jpg', 'SS-GW6-001',
     0, 1, 0, 0, 1, 0, 780,
     1999.00, 1799.00, 'BioActive 传感器 | 睡眠追踪', 2199.00, 500, '块',
     'Samsung', '智能穿戴',
     'Galaxy Watch 6，智能健康管理。',
     'Galaxy Watch 6 详情', 'Samsung,三星,Galaxy,Watch,智能手表');

-- ══════════════════ SKU Stock ══════════════════
INSERT IGNORE INTO pms_sku_stock
    (id, product_id, sku_code, price, stock, low_stock, lock_stock, sp_data)
VALUES
    -- iPhone 15 Pro Max SKUs
    (1,  1, 'IP15PM-BLK-256',  9999.00,  80, 10, 0, '[{"key":"颜色","value":"黑色"},{"key":"存储容量","value":"256GB"}]'),
    (2,  1, 'IP15PM-WHT-256',  9999.00,  60, 10, 0, '[{"key":"颜色","value":"白色"},{"key":"存储容量","value":"256GB"}]'),
    (3,  1, 'IP15PM-BLK-512', 11299.00,  40, 10, 0, '[{"key":"颜色","value":"黑色"},{"key":"存储容量","value":"512GB"}]'),
    (4,  1, 'IP15PM-BLU-256',  9999.00,  50, 10, 0, '[{"key":"颜色","value":"蓝色"},{"key":"存储容量","value":"256GB"}]'),
    -- iPhone 15 SKUs
    (5,  2, 'IP15-BLK-128',    5999.00, 150, 20, 0, '[{"key":"颜色","value":"黑色"},{"key":"存储容量","value":"128GB"}]'),
    (6,  2, 'IP15-WHT-128',    5999.00, 120, 20, 0, '[{"key":"颜色","value":"白色"},{"key":"存储容量","value":"128GB"}]'),
    (7,  2, 'IP15-BLU-256',    6599.00,  80, 10, 0, '[{"key":"颜色","value":"蓝色"},{"key":"存储容量","value":"256GB"}]'),
    -- Huawei Mate 60 Pro SKUs
    (8,  3, 'M60P-BLK-256',    6999.00, 100, 10, 0, '[{"key":"颜色","value":"黑色"},{"key":"存储容量","value":"256GB"}]'),
    (9,  3, 'M60P-WHT-512',    7599.00,  50, 10, 0, '[{"key":"颜色","value":"白色"},{"key":"存储容量","value":"512GB"}]'),
    -- Samsung Galaxy S24 Ultra SKUs
    (10, 4, 'GS24U-BLK-256',   9699.00, 100, 10, 0, '[{"key":"颜色","value":"黑色"},{"key":"存储容量","value":"256GB"}]'),
    (11, 4, 'GS24U-GLD-512',  10999.00,  60, 10, 0, '[{"key":"颜色","value":"金色"},{"key":"存储容量","value":"512GB"}]'),
    -- Xiaomi 14 Pro SKUs
    (12, 5, 'XM14P-BLK-256',   4299.00, 200, 20, 0, '[{"key":"颜色","value":"黑色"},{"key":"存储容量","value":"256GB"}]'),
    (13, 5, 'XM14P-WHT-512',   4699.00, 100, 10, 0, '[{"key":"颜色","value":"白色"},{"key":"存储容量","value":"512GB"}]'),
    -- MacBook Pro 14 SKUs
    (14, 6, 'MBP14-SLV-16',   14999.00,  60, 5,  0, '[{"key":"颜色","value":"银色"},{"key":"内存","value":"16GB"}]'),
    (15, 6, 'MBP14-GRY-32',   17999.00,  30, 5,  0, '[{"key":"颜色","value":"深空灰"},{"key":"内存","value":"32GB"}]'),
    -- Huawei MatePad Pro SKUs
    (16, 7, 'MPP132-BLK',      5699.00,  80, 10, 0, '[{"key":"颜色","value":"黑色"}]'),
    (17, 7, 'MPP132-WHT',      5699.00,  60, 10, 0, '[{"key":"颜色","value":"白色"}]'),
    -- Nike Air Max 270 SKUs
    (18, 8, 'AM270-BLK-42',     899.00, 200, 20, 0, '[{"key":"颜色","value":"黑色"},{"key":"尺码","value":"42"}]'),
    (19, 8, 'AM270-WHT-42',     899.00, 180, 20, 0, '[{"key":"颜色","value":"白色"},{"key":"尺码","value":"42"}]'),
    (20, 8, 'AM270-RED-41',     899.00, 150, 20, 0, '[{"key":"颜色","value":"红色"},{"key":"尺码","value":"41"}]'),
    -- Nike Dri-FIT SKUs
    (21, 9, 'DF-BLK-L',         299.00, 500, 50, 0, '[{"key":"颜色","value":"黑色"},{"key":"尺码","value":"L"}]'),
    (22, 9, 'DF-WHT-M',         299.00, 400, 50, 0, '[{"key":"颜色","value":"白色"},{"key":"尺码","value":"M"}]'),
    -- Galaxy Watch 6 SKUs
    (23, 10, 'GW6-BLK-44',     1999.00, 150, 15, 0, '[{"key":"颜色","value":"黑色"}]'),
    (24, 10, 'GW6-SLV-40',     1899.00, 120, 15, 0, '[{"key":"颜色","value":"银色"}]');

-- ══════════════════ Sample Coupons ══════════════════
INSERT IGNORE INTO sms_coupon
    (id, type, name, platform, count, amount, per_limit, min_point, start_time, end_time,
     use_type, note, publish_count, use_count, receive_count, enable_time, code, member_level)
VALUES
    (1, 0, '全品类满200减20',  0, 1000, 20.00, 1, 200.00,
     NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 0, '全场通用', 1000, 0, 0, NOW(), NULL, NULL),
    (2, 0, '手机专区满5000减300', 0, 500, 300.00, 1, 5000.00,
     NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, '仅限手机品类', 500, 0, 0, NOW(), NULL, NULL),
    (3, 0, '新人专享满100减50',  0, 2000, 50.00, 1, 100.00,
     NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), 0, '新注册用户专享', 2000, 0, 0, NOW(), NULL, NULL);

-- ══════════════════ Home Advertise (Banners) ══════════════════
INSERT IGNORE INTO sms_home_advertise
    (id, name, type, pic, start_time, end_time, status, click_count, order_count, url, note, sort)
VALUES
    (1, 'iPhone 15 Pro Max 首发', 1, '/images/banner/iphone15.jpg',
     NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), 1, 0, 0, '/product/1', '旗舰新品', 0),
    (2, '运动季大促', 1, '/images/banner/sport.jpg',
     NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, 0, 0, '/category/3', '运动品类折扣', 1),
    (3, '新人专享优惠券', 1, '/images/banner/newuser.jpg',
     NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), 1, 0, 0, '/coupon', '新用户领券中心', 2);

-- ══════════════════ Recommend Products (Home) ══════════════════
INSERT IGNORE INTO sms_home_recommend_product (id, product_id, product_name, recommend_status, sort)
VALUES
    (1, 1, 'iPhone 15 Pro Max',      1, 0),
    (2, 3, 'Huawei Mate 60 Pro',     1, 1),
    (3, 5, 'Xiaomi 14 Pro',          1, 2),
    (4, 6, 'MacBook Pro 14 M3 Pro',  1, 3),
    (5, 8, 'Nike Air Max 270',       1, 4);

-- ══════════════════ New Products (Home) ══════════════════
INSERT IGNORE INTO sms_home_new_product (id, product_id, product_name, recommend_status, sort)
VALUES
    (1, 1, 'iPhone 15 Pro Max',        1, 0),
    (2, 4, 'Samsung Galaxy S24 Ultra', 1, 1),
    (3, 6, 'MacBook Pro 14 M3 Pro',    1, 2);
