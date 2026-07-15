-- ============================================================
-- 苏小晗 — 费用管理 + 停车管理 数据库修改
-- 适用版本：基于 1.0_init.sql 之后
-- ============================================================

-- ==================== 1. 新建表 ====================
DROP TABLE IF EXISTS `com_parking_monthly_apply`;
CREATE TABLE `com_parking_monthly_apply` (
  `apply_id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `owner_id` bigint DEFAULT NULL COMMENT '业主ID',
  `vehicle_id` bigint DEFAULT NULL COMMENT '车辆ID',
  `spot_id` bigint DEFAULT NULL COMMENT '车位ID',
  `months` int DEFAULT '1' COMMENT '申请月数',
  `amount` decimal(10,2) DEFAULT '0.00' COMMENT '费用',
  `status` varchar(20) DEFAULT '待审核' COMMENT '状态（待审核/已通过/已拒绝）',
  `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
  `approve_by` varchar(64) DEFAULT NULL COMMENT '审核人',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`apply_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='月卡申请表';

-- ==================== 2. sys_menu 新增菜单 ====================

-- 2.1 居民端子菜单（挂载在103/104下面）
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(10915, '我的账单', 103, 1, 'my-bill', 'resident/fee/index', '1', '0', 'C', '0', '0', '', 'money', 'admin', NOW()),
(10916, '停车服务', 104, 5, 'my-parking', 'resident/parking/index', '1', '0', 'C', '0', '0', '', 'user', 'admin', NOW()),
(10920, '缴费记录', 103, 5, 'my-payment', 'resident/fee/payment', '1', '0', 'C', '0', '0', '', 'money', 'admin', NOW());

-- 2.2 管理员端顶级菜单（双菜单分离）
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(10929, '费用管理', 0, 2, 'admin-fee', 'com/fee/index', '1', '0', 'C', '0', '0', 'com:fee:item:list', 'money', 'admin', NOW()),
(10930, '停车管理', 0, 3, 'admin-parking', 'com/parking/index', '1', '0', 'C', '0', '0', 'com:parking:spot:list', 'coordinate', 'admin', NOW());

-- 2.3 管理端按钮权限（费用管理）
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(10301, '费用项目查询', 103, 1, '', NULL, '1', '0', 'F', '0', '0', 'com:fee:item:query', '#', 'admin', NOW()),
(10302, '费用项目新增', 103, 2, '', NULL, '1', '0', 'F', '0', '0', 'com:fee:item:add', '#', 'admin', NOW()),
(10303, '费用项目修改', 103, 3, '', NULL, '1', '0', 'F', '0', '0', 'com:fee:item:edit', '#', 'admin', NOW()),
(10304, '费用项目删除', 103, 4, '', NULL, '1', '0', 'F', '0', '0', 'com:fee:item:remove', '#', 'admin', NOW()),
(10305, '账单查询', 103, 5, '', NULL, '1', '0', 'F', '0', '0', 'com:fee:bill:list', '#', 'admin', NOW()),
(10306, '账单详情', 103, 6, '', NULL, '1', '0', 'F', '0', '0', 'com:fee:bill:query', '#', 'admin', NOW()),
(10307, '账单新增', 103, 7, '', NULL, '1', '0', 'F', '0', '0', 'com:fee:bill:add', '#', 'admin', NOW()),
(10308, '账单删除', 103, 8, '', NULL, '1', '0', 'F', '0', '0', 'com:fee:bill:remove', '#', 'admin', NOW()),
(10309, '缴费记录查询', 103, 9, '', NULL, '1', '0', 'F', '0', '0', 'com:fee:payment:list', '#', 'admin', NOW()),
(10310, '缴费新增', 103, 10, '', NULL, '1', '0', 'F', '0', '0', 'com:fee:payment:add', '#', 'admin', NOW()),
(10917, '催缴邮件', 103, 11, '', NULL, '1', '0', 'F', '0', '0', 'com:fee:remind', '#', 'admin', NOW()),
(10918, '模拟缴费', 103, 12, '', NULL, '1', '0', 'F', '0', '0', 'com:fee:pay:mock', '#', 'admin', NOW()),
(10922, '批量生成', 103, 13, '', NULL, '1', '0', 'F', '0', '0', 'com:fee:bill:batch', '#', 'admin', NOW()),
(10923, '费用统计权限', 103, 14, '', NULL, '1', '0', 'F', '0', '0', 'com:fee:statistics', '#', 'admin', NOW());

-- 2.4 管理端按钮权限（停车管理）
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(10401, '车位查询', 104, 1, '', NULL, '1', '0', 'F', '0', '0', 'com:parking:spot:list', '#', 'admin', NOW()),
(10402, '车位新增', 104, 2, '', NULL, '1', '0', 'F', '0', '0', 'com:parking:spot:add', '#', 'admin', NOW()),
(10403, '车位修改', 104, 3, '', NULL, '1', '0', 'F', '0', '0', 'com:parking:spot:edit', '#', 'admin', NOW()),
(10404, '车位删除', 104, 4, '', NULL, '1', '0', 'F', '0', '0', 'com:parking:spot:remove', '#', 'admin', NOW()),
(10405, '车辆查询', 104, 5, '', NULL, '1', '0', 'F', '0', '0', 'com:parking:vehicle:list', '#', 'admin', NOW()),
(10406, '车辆详情', 104, 6, '', NULL, '1', '0', 'F', '0', '0', 'com:parking:vehicle:query', '#', 'admin', NOW()),
(10407, '车辆新增', 104, 7, '', NULL, '1', '0', 'F', '0', '0', 'com:parking:vehicle:add', '#', 'admin', NOW()),
(10408, '车辆修改', 104, 8, '', NULL, '1', '0', 'F', '0', '0', 'com:parking:vehicle:edit', '#', 'admin', NOW()),
(10409, '车辆删除', 104, 9, '', NULL, '1', '0', 'F', '0', '0', 'com:parking:vehicle:remove', '#', 'admin', NOW()),
(10410, '停车记录查询', 104, 10, '', NULL, '1', '0', 'F', '0', '0', 'com:parking:record:list', '#', 'admin', NOW()),
(10411, '停车记录新增', 104, 11, '', NULL, '1', '0', 'F', '0', '0', 'com:parking:record:add', '#', 'admin', NOW()),
(10919, '出场登记', 104, 12, '', NULL, '1', '0', 'F', '0', '0', 'com:parking:record:exit', '#', 'admin', NOW()),
(10924, '月卡续费', 104, 13, '', NULL, '1', '0', 'F', '0', '0', 'com:parking:monthly:renew', '#', 'admin', NOW());

-- ==================== 3. admin 角色(1) 权限分配 ====================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE menu_id >= 10300 AND menu_id <= 10999;
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 10929), (1, 10930);
-- 删除 admin 旧的 103/104 权限（让其用新菜单）
DELETE FROM sys_role_menu WHERE role_id = 1 AND menu_id IN (103, 104);

-- ==================== 4. 社区管理员角色(2) — zhang 权限分配 ====================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 10301), (2, 10302), (2, 10303), (2, 10304);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 10305), (2, 10306), (2, 10307), (2, 10308);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 10309), (2, 10310);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 10917), (2, 10918), (2, 10922), (2, 10923);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 10401), (2, 10402), (2, 10403), (2, 10404);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 10405), (2, 10406), (2, 10407), (2, 10408), (2, 10409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 10410), (2, 10411), (2, 10919), (2, 10924);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 10929), (2, 10930);
-- 删除 zhang 旧的 103/104 权限
DELETE FROM sys_role_menu WHERE role_id = 2 AND menu_id IN (103, 104);

-- ==================== 5. 居民角色(3) — resident 权限分配 ====================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (3, 103), (3, 104);  -- 顶级菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (3, 10915), (3, 10920), (3, 10916);  -- 居民子菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (3, 10309), (3, 10918), (3, 10924);  -- 查看、模拟缴费、月卡续费
