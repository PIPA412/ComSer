-- =============================================
-- 便民服务整合模块 - 数据库变更脚本
-- 数据库：com_ser
-- =============================================

-- 1. 服务商表（补充字段）
ALTER TABLE `com_service_provider`
  ADD COLUMN `settle_date` datetime DEFAULT NULL COMMENT '入驻时间' AFTER `description`;

-- 2. 服务项目表（补充字段）
ALTER TABLE `com_service_item`
  ADD COLUMN `service_detail` text DEFAULT NULL COMMENT '服务详情' AFTER `booking_method`;

-- 3. 预约订单表（补充字段）
ALTER TABLE `com_service_order`
  ADD COLUMN `contact_name` varchar(50) DEFAULT NULL COMMENT '联系人' AFTER `item_id`,
  ADD COLUMN `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话' AFTER `contact_name`;

-- 4. 预约订单表（删除旧的评价字段，已移至评价表）
-- 如果之前执行过旧脚本已有 contact_name/contact_phone 则跳过上面两行
-- ALTER TABLE `com_service_order` DROP COLUMN `rating`, DROP COLUMN `review`;

-- 5. 服务评价表（新建）
CREATE TABLE IF NOT EXISTS `com_service_review` (
  `review_id`   bigint       NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id`    bigint       DEFAULT NULL COMMENT '订单ID',
  `user_id`     bigint       DEFAULT NULL COMMENT '用户ID',
  `item_id`     bigint       DEFAULT NULL COMMENT '服务项目ID',
  `provider_id` bigint       DEFAULT NULL COMMENT '服务商ID',
  `rating`      tinyint      DEFAULT NULL COMMENT '评分（1-5星）',
  `content`     varchar(500) DEFAULT NULL COMMENT '评价内容',
  `create_by`   varchar(64)  DEFAULT '' COMMENT '创建者',
  `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`   varchar(64)  DEFAULT '' COMMENT '更新者',
  `update_time` datetime     DEFAULT NULL COMMENT '更新时间',
  `remark`      varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`review_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_item_id`  (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务评价表';
