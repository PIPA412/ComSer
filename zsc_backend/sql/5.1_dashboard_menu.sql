-- ============================================================
-- 数据驾驶舱菜单 & 权限
-- 用法：登录 MySQL 后执行 source 5.1_dashboard_menu.sql
-- 说明：使用 IGNORE 避免重复插入报错
-- ============================================================

-- 1. 插入数据驾驶舱菜单记录（已存在则跳过）
INSERT IGNORE INTO sys_menu VALUES
(110, '数据驾驶舱', 0, 8, 'dashboard', 'com/dashboard/index', NULL, '', 1, 0, 'C', '0', '0', 'com:dashboard:view', 'dashboard', 'admin', NOW(), '', NULL, '');

-- 2. 预警管理子菜单
INSERT IGNORE INTO sys_menu VALUES
(111, '预警规则', 110, 1, 'alertRule', 'com/dashboard/alertRule', NULL, '', 1, 0, 'C', '0', '0', 'com:dashboard:alert', 'edit', 'admin', NOW(), '', NULL, ''),
(112, '预警记录', 110, 2, 'alertRecord', 'com/dashboard/alertRecord', NULL, '', 1, 0, 'C', '0', '0', 'com:dashboard:alert', 'warning', 'admin', NOW(), '', NULL, '');

-- 3. 授权：超级管理员(role_id=1) + 社区管理员(role_id=2) 可访问
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 110);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 110);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 111);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 111);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 112);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 112);
