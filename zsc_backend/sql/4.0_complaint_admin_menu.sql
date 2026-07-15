-- ============================================
-- 投诉建议 - 补充管理员端菜单
-- 问题：3.0 把菜单107改成目录后，只加了居民端子菜单，
--       漏掉了管理端的 工单管理 和 数据统计
-- ============================================

-- 1. 新增"工单管理"子菜单 (管理员列表页，原 menu 107 的功能)
INSERT INTO sys_menu VALUES
(10710, '工单管理', 107, 3, 'manage', 'com/complaint/index', NULL, '', 1, 0, 'C', '0', '0', 'com:complaint:list', 'list', 'admin', NOW(), '', NULL, '投诉建议工单管理');

-- 2. 新增"数据统计"子菜单 (统计看板)
INSERT INTO sys_menu VALUES
(10711, '数据统计', 107, 4, 'stats', 'com/complaint/stats', NULL, '', 1, 0, 'C', '0', '0', 'com:complaint:list', 'chart', 'admin', NOW(), '', NULL, '投诉建议数据统计');

-- 3. "我要投诉"排序号调为 1，"我要建议"调为 2（让管理菜单在前）
UPDATE sys_menu SET order_num = 1 WHERE menu_id = 10710;
UPDATE sys_menu SET order_num = 2 WHERE menu_id = 10711;
UPDATE sys_menu SET order_num = 3 WHERE menu_id = 10708;
UPDATE sys_menu SET order_num = 4 WHERE menu_id = 10709;

-- 4. 授权给超级管理员(1)、社区管理员(2)、普通角色(3)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM (SELECT 1 AS role_id UNION ALL SELECT 2 UNION ALL SELECT 3) r
CROSS JOIN (SELECT 10710 AS menu_id UNION ALL SELECT 10711) m
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
);
