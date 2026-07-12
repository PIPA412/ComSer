-- ============================================================
-- 访客管理和公告功能完善 - 补丁 SQL
-- 用于已有数据库，添加新增的菜单权限
-- ============================================================
USE `com_ser`;

-- 1. 添加访客修改权限 (com:visitor:edit)
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 10507, '访客修改', 105, 7, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:visitor:edit', '#', 'admin', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 10507);

-- 2. 将新权限分配给有访客管理权限的角色
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.role_id, 10507
FROM `sys_role` r
WHERE EXISTS (
  SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = r.role_id AND rm.menu_id = 105
) AND NOT EXISTS (
  SELECT 1 FROM `sys_role_menu` rm2 WHERE rm2.role_id = r.role_id AND rm2.menu_id = 10507
);
