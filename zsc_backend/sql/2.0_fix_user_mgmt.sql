-- ============================================================
-- 快速修复脚本（适用于已运行旧 SQL 的现有数据库）
-- 三合一：加列 + 改组件路径 + 改路由路径避免冲突
-- ============================================================
USE `com_ser`;

-- 1. 安全添加缺失的 route_name 列
DELIMITER //
DROP PROCEDURE IF EXISTS add_route_name_if_missing//
CREATE PROCEDURE add_route_name_if_missing()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'com_ser'
      AND TABLE_NAME = 'sys_menu'
      AND COLUMN_NAME = 'route_name'
  ) THEN
    ALTER TABLE `sys_menu`
    ADD COLUMN `route_name` VARCHAR(50) DEFAULT '' COMMENT '路由名称'
    AFTER `query`;
  END IF;
END//
DELIMITER ;
CALL add_route_name_if_missing();
DROP PROCEDURE IF EXISTS add_route_name_if_missing;

-- 2. 修正组件路径 + 路由路径（避免与 /user/profile 冲突）
UPDATE `sys_menu`
SET `component` = 'admin/user/index',
    `path` = 'system/user'
WHERE `menu_id` = 2;
