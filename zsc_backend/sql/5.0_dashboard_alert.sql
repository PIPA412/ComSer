-- ============================================================
-- 社区服务管理系统 (ComSer) - 数据驾驶舱预警模块
-- Database: com_ser
-- 说明：新增预警规则和预警记录两张表
-- ============================================================

-- ----------------------------
-- 1. 预警规则表
-- ----------------------------
DROP TABLE IF EXISTS `com_alert_rule`;
CREATE TABLE `com_alert_rule` (
  `rule_id`      bigint NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `rule_name`    varchar(100)  DEFAULT ''  COMMENT '规则名称',
  `metric_key`   varchar(50)   DEFAULT ''  COMMENT '指标标识（collection_rate / complaint_monthly / repair_overdue_rate）',
  `metric_name`  varchar(100)  DEFAULT ''  COMMENT '指标名称（展示用）',
  `compare_type` varchar(10)   DEFAULT 'LT' COMMENT '比较方式（GT大于/LT小于/GTE大于等于/LTE小于等于）',
  `threshold`    decimal(10,2) DEFAULT 0.00 COMMENT '阈值',
  `status`       char(1)       DEFAULT '0' COMMENT '是否启用（0启用 1停用）',
  `create_by`    varchar(64)   DEFAULT ''  COMMENT '创建者',
  `create_time`  datetime      DEFAULT NULL COMMENT '创建时间',
  `update_by`    varchar(64)   DEFAULT ''  COMMENT '更新者',
  `update_time`  datetime      DEFAULT NULL COMMENT '更新时间',
  `remark`       varchar(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预警规则表';

-- ----------------------------
-- 2. 预警记录表
-- ----------------------------
DROP TABLE IF EXISTS `com_alert_record`;
CREATE TABLE `com_alert_record` (
  `record_id`     bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `rule_id`       bigint         DEFAULT NULL COMMENT '关联规则ID',
  `rule_name`     varchar(100)   DEFAULT ''  COMMENT '规则名称（冗余）',
  `metric_name`   varchar(100)   DEFAULT ''  COMMENT '指标名称',
  `trigger_value` decimal(10,2)  DEFAULT 0.00 COMMENT '触发值',
  `threshold`     decimal(10,2)  DEFAULT 0.00 COMMENT '阈值',
  `trigger_time`  datetime       DEFAULT NULL COMMENT '触发时间',
  `handle_status` varchar(20)    DEFAULT '待处理' COMMENT '处置状态（待处理/已处理/已忽略）',
  `handle_by`     varchar(64)    DEFAULT ''  COMMENT '处置人',
  `handle_time`   datetime       DEFAULT NULL COMMENT '处置时间',
  `handle_remark` varchar(500)   DEFAULT ''  COMMENT '处置备注',
  `create_by`     varchar(64)    DEFAULT ''  COMMENT '创建者',
  `create_time`   datetime       DEFAULT NULL COMMENT '创建时间',
  `update_by`     varchar(64)    DEFAULT ''  COMMENT '更新者',
  `update_time`   datetime       DEFAULT NULL COMMENT '更新时间',
  `remark`        varchar(500)   DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`record_id`),
  KEY `idx_rule_id` (`rule_id`),
  KEY `idx_handle_status` (`handle_status`),
  KEY `idx_trigger_time` (`trigger_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预警记录表';

-- ----------------------------
-- 3. 插入默认预警规则
-- ----------------------------
INSERT INTO `com_alert_rule` (`rule_name`, `metric_key`, `metric_name`, `compare_type`, `threshold`, `status`, `create_by`, `create_time`)
VALUES
('收缴率过低', 'collection_rate', '当月收缴率', 'LT', 80.00, '0', 'admin', NOW()),
('投诉量异常', 'complaint_monthly', '月度投诉量', 'GT', 20.00, '0', 'admin', NOW()),
('报修超时率过高', 'repair_overdue_rate', '报修超时率', 'GT', 10.00, '0', 'admin', NOW());
