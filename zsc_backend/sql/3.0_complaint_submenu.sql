-- 投诉建议菜单重构: 改为目录 + 两个子菜单
-- 1. 将 menu 107 从 C(菜单) 改为 M(目录)
UPDATE sys_menu SET menu_type = 'M', component = NULL WHERE menu_id = 107;

-- 2. 创建 "我要投诉" 子菜单 (10708)
INSERT INTO sys_menu VALUES
(10708, '我要投诉', 107, 1, 'complaint', 'com/complaint/complaint', NULL, '', 1, 0, 'C', '0', '0', 'com:complaint:complaint:list', 'edit', 'admin', NOW(), '', NULL, '');

-- 3. 创建 "我要建议" 子菜单 (10709)
INSERT INTO sys_menu VALUES
(10709, '我要建议', 107, 2, 'suggestion', 'com/complaint/suggestion', NULL, '', 1, 0, 'C', '0', '0', 'com:complaint:suggestion:list', 'edit', 'admin', NOW(), '', NULL, '');

-- 4. 授权给三个角色
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 10708 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 10708);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 10709 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 10709);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 2, 10708 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 2 AND menu_id = 10708);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 2, 10709 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 2 AND menu_id = 10709);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 3, 10708 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 3 AND menu_id = 10708);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 3, 10709 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 3 AND menu_id = 10709);
