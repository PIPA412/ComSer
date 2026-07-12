-- ============================================================
-- 社区服务管理系统 (ComSer) - 完整初始化脚本
-- Database: com_ser
-- 基于若依 (RuoYi) 快速开发框架
-- 说明：已整合 V1.0~V1.5 所有迁移，直接执行即可完成建库
-- ============================================================

CREATE DATABASE IF NOT EXISTS `com_ser` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `com_ser`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 第一部分：DDL —— 所有表结构
-- ============================================================

-- ----------------------------
-- 1. Quartz 定时任务表
-- ----------------------------

DROP TABLE IF EXISTS `QRTZ_JOB_DETAILS`;
CREATE TABLE `QRTZ_JOB_DETAILS` (
  `sched_name` varchar(120) NOT NULL COMMENT '调度名称',
  `job_name` varchar(200) NOT NULL COMMENT '任务名称',
  `job_group` varchar(200) NOT NULL COMMENT '任务组名',
  `description` varchar(250) DEFAULT NULL COMMENT '相关介绍',
  `job_class_name` varchar(250) NOT NULL COMMENT '执行任务类名称',
  `is_durable` varchar(1) NOT NULL COMMENT '是否持久化',
  `is_nonconcurrent` varchar(1) NOT NULL COMMENT '是否并发',
  `is_update_data` varchar(1) NOT NULL COMMENT '是否更新数据',
  `requests_recovery` varchar(1) NOT NULL COMMENT '是否接受恢复执行',
  `job_data` blob COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`,`job_name`,`job_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务详细信息表';

DROP TABLE IF EXISTS `QRTZ_TRIGGERS`;
CREATE TABLE `QRTZ_TRIGGERS` (
  `sched_name` varchar(120) NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) NOT NULL COMMENT '触发器的名字',
  `trigger_group` varchar(200) NOT NULL COMMENT '触发器所属组的名字',
  `job_name` varchar(200) NOT NULL COMMENT 'qrtz_job_details表job_name的外键',
  `job_group` varchar(200) NOT NULL COMMENT 'qrtz_job_details表job_group的外键',
  `description` varchar(250) DEFAULT NULL COMMENT '相关介绍',
  `next_fire_time` bigint DEFAULT NULL COMMENT '上一次触发时间（毫秒）',
  `prev_fire_time` bigint DEFAULT NULL COMMENT '下一次触发时间（默认为-1表示不触发）',
  `priority` int DEFAULT NULL COMMENT '优先级',
  `trigger_state` varchar(16) NOT NULL COMMENT '触发器状态',
  `trigger_type` varchar(8) NOT NULL COMMENT '触发器的类型',
  `start_time` bigint NOT NULL COMMENT '开始时间',
  `end_time` bigint DEFAULT NULL COMMENT '结束时间',
  `calendar_name` varchar(200) DEFAULT NULL COMMENT '日程表名称',
  `misfire_instr` smallint DEFAULT NULL COMMENT '补偿执行的策略',
  `job_data` blob COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  KEY `sched_name` (`sched_name`,`job_name`,`job_group`),
  CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `QRTZ_JOB_DETAILS` (`sched_name`, `job_name`, `job_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='触发器详细信息表';

DROP TABLE IF EXISTS `QRTZ_BLOB_TRIGGERS`;
CREATE TABLE `QRTZ_BLOB_TRIGGERS` (
  `sched_name` varchar(120) NOT NULL,
  `trigger_name` varchar(200) NOT NULL,
  `trigger_group` varchar(200) NOT NULL,
  `blob_data` blob,
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Blob类型的触发器表';

DROP TABLE IF EXISTS `QRTZ_CALENDARS`;
CREATE TABLE `QRTZ_CALENDARS` (
  `sched_name` varchar(120) NOT NULL,
  `calendar_name` varchar(200) NOT NULL,
  `calendar` blob NOT NULL,
  PRIMARY KEY (`sched_name`,`calendar_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='日历信息表';

DROP TABLE IF EXISTS `QRTZ_CRON_TRIGGERS`;
CREATE TABLE `QRTZ_CRON_TRIGGERS` (
  `sched_name` varchar(120) NOT NULL,
  `trigger_name` varchar(200) NOT NULL,
  `trigger_group` varchar(200) NOT NULL,
  `cron_expression` varchar(200) NOT NULL,
  `time_zone_id` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Cron类型的触发器表';

DROP TABLE IF EXISTS `QRTZ_FIRED_TRIGGERS`;
CREATE TABLE `QRTZ_FIRED_TRIGGERS` (
  `sched_name` varchar(120) NOT NULL,
  `entry_id` varchar(95) NOT NULL,
  `trigger_name` varchar(200) NOT NULL,
  `trigger_group` varchar(200) NOT NULL,
  `instance_name` varchar(200) NOT NULL,
  `fired_time` bigint NOT NULL,
  `sched_time` bigint NOT NULL,
  `priority` int NOT NULL,
  `state` varchar(16) NOT NULL,
  `job_name` varchar(200) DEFAULT NULL,
  `job_group` varchar(200) DEFAULT NULL,
  `is_nonconcurrent` varchar(1) DEFAULT NULL,
  `requests_recovery` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`sched_name`,`entry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='已触发的触发器表';

DROP TABLE IF EXISTS `QRTZ_LOCKS`;
CREATE TABLE `QRTZ_LOCKS` (
  `sched_name` varchar(120) NOT NULL,
  `lock_name` varchar(40) NOT NULL,
  PRIMARY KEY (`sched_name`,`lock_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='存储的悲观锁信息表';

DROP TABLE IF EXISTS `QRTZ_PAUSED_TRIGGER_GRPS`;
CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS` (
  `sched_name` varchar(120) NOT NULL,
  `trigger_group` varchar(200) NOT NULL,
  PRIMARY KEY (`sched_name`,`trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='暂停的触发器表';

DROP TABLE IF EXISTS `QRTZ_SCHEDULER_STATE`;
CREATE TABLE `QRTZ_SCHEDULER_STATE` (
  `sched_name` varchar(120) NOT NULL,
  `instance_name` varchar(200) NOT NULL,
  `last_checkin_time` bigint NOT NULL,
  `checkin_interval` bigint NOT NULL,
  PRIMARY KEY (`sched_name`,`instance_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调度器状态表';

DROP TABLE IF EXISTS `QRTZ_SIMPLE_TRIGGERS`;
CREATE TABLE `QRTZ_SIMPLE_TRIGGERS` (
  `sched_name` varchar(120) NOT NULL,
  `trigger_name` varchar(200) NOT NULL,
  `trigger_group` varchar(200) NOT NULL,
  `repeat_count` bigint NOT NULL,
  `repeat_interval` bigint NOT NULL,
  `times_triggered` bigint NOT NULL,
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='简单触发器的信息表';

DROP TABLE IF EXISTS `QRTZ_SIMPROP_TRIGGERS`;
CREATE TABLE `QRTZ_SIMPROP_TRIGGERS` (
  `sched_name` varchar(120) NOT NULL,
  `trigger_name` varchar(200) NOT NULL,
  `trigger_group` varchar(200) NOT NULL,
  `str_prop_1` varchar(512) DEFAULT NULL,
  `str_prop_2` varchar(512) DEFAULT NULL,
  `str_prop_3` varchar(512) DEFAULT NULL,
  `int_prop_1` int DEFAULT NULL,
  `int_prop_2` int DEFAULT NULL,
  `long_prop_1` bigint DEFAULT NULL,
  `long_prop_2` bigint DEFAULT NULL,
  `dec_prop_1` decimal(13,4) DEFAULT NULL,
  `dec_prop_2` decimal(13,4) DEFAULT NULL,
  `bool_prop_1` varchar(1) DEFAULT NULL,
  `bool_prop_2` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='同步机制的行锁表';

-- ----------------------------
-- 2. 代码生成表
-- ----------------------------

DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table` (
  `table_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `tpl_web_type` varchar(30) DEFAULT '' COMMENT '前端模板类型',
  `package_name` varchar(100) DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` char(1) DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) DEFAULT '/' COMMENT '生成路径',
  `options` varchar(1000) DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='代码生成业务表';

DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column` (
  `column_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) DEFAULT 'EQ' COMMENT '查询方式',
  `html_type` varchar(200) DEFAULT NULL COMMENT '显示类型',
  `dict_type` varchar(200) DEFAULT '' COMMENT '字典类型',
  `sort` int DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='代码生成业务表字段';

-- ----------------------------
-- 3. 系统管理表
-- ----------------------------

DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='参数配置表';

DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint DEFAULT 0 COMMENT '父部门id',
  `ancestors` varchar(50) DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) DEFAULT '' COMMENT '部门名称',
  `order_num` int DEFAULT 0 COMMENT '显示顺序',
  `leader` varchar(20) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `status` char(1) DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) DEFAULT NULL COMMENT '样式属性',
  `list_class` varchar(100) DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';

DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`),
  UNIQUE KEY `dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job` (
  `job_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` char(1) DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`job_id`,`job_name`,`job_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='定时任务调度表';

DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log` (
  `job_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) DEFAULT NULL COMMENT '日志信息',
  `status` char(1) DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) DEFAULT '' COMMENT '异常信息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='定时任务调度日志表';

DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor` (
  `info_id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) DEFAULT '' COMMENT '操作系统',
  `status` char(1) DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) DEFAULT '' COMMENT '提示消息',
  `login_time` datetime DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统访问记录';

DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `parent_id` bigint DEFAULT 0 COMMENT '父菜单ID',
  `order_num` int DEFAULT 0 COMMENT '显示顺序',
  `path` varchar(200) DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) DEFAULT NULL COMMENT '路由参数',
  `route_name` varchar(50) DEFAULT '' COMMENT '路由名称',
  `is_frame` int DEFAULT 1 COMMENT '是否为外链（0是 1否）',
  `is_cache` int DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单权限表';

DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
  `notice_id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) NOT NULL COMMENT '公告标题',
  `notice_type` char(1) NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob COMMENT '公告内容',
  `status` char(1) DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知公告表';

DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
  `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) DEFAULT '' COMMENT '模块标题',
  `business_type` int DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(100) DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) DEFAULT '' COMMENT '请求方式',
  `operator_type` int DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) DEFAULT '' COMMENT '返回参数',
  `status` int DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint DEFAULT 0 COMMENT '消耗时间（毫秒）',
  PRIMARY KEY (`oper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志记录';

DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post` (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) NOT NULL COMMENT '岗位名称',
  `post_sort` int NOT NULL COMMENT '显示顺序',
  `status` char(1) NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位信息表';

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) NOT NULL COMMENT '角色权限字符串',
  `role_sort` int NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) DEFAULT '1' COMMENT '数据范围（1全部数据权限 2自定数据权限 3本部门数据权限 4本部门及以下数据权限 5仅本人数据权限）',
  `menu_check_strictly` tinyint(1) DEFAULT '1' COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) DEFAULT '1' COMMENT '部门树选择项是否关联显示',
  `status` char(1) NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色信息表';

DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`,`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色和部门关联表';

DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色和菜单关联表';

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) NOT NULL COMMENT '用户昵称',
  `user_type` varchar(2) DEFAULT '00' COMMENT '用户类型（00系统用户）',
  `email` varchar(50) DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) DEFAULT '' COMMENT '手机号码',
  `sex` char(1) DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) DEFAULT '' COMMENT '密码',
  `status` char(1) DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` datetime DEFAULT NULL COMMENT '密码最后更新时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信息表';

DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`,`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户与岗位关联表';

DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户和角色关联表';

-- ----------------------------
-- 4. 社区服务业务表
-- ----------------------------

-- 楼栋信息
DROP TABLE IF EXISTS `com_building`;
CREATE TABLE `com_building` (
  `building_id`   bigint NOT NULL AUTO_INCREMENT COMMENT '楼栋ID',
  `building_name` varchar(100) DEFAULT ''  COMMENT '楼栋名称',
  `building_code` varchar(50)  DEFAULT ''  COMMENT '楼栋编号',
  `floor_count`   int          DEFAULT 0   COMMENT '楼层数',
  `address`       varchar(255) DEFAULT ''  COMMENT '楼栋地址',
  `status`        char(1)      DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by`     varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time`   datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`     varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time`   datetime     DEFAULT NULL COMMENT '更新时间',
  `remark`        varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`building_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='楼栋信息';

-- 单元信息
DROP TABLE IF EXISTS `com_unit`;
CREATE TABLE `com_unit` (
  `unit_id`     bigint NOT NULL AUTO_INCREMENT COMMENT '单元ID',
  `building_id` bigint       DEFAULT NULL COMMENT '所属楼栋ID',
  `unit_name`   varchar(100) DEFAULT ''  COMMENT '单元名称',
  `unit_code`   varchar(50)  DEFAULT ''  COMMENT '单元编号',
  `floor_count` int          DEFAULT 0   COMMENT '楼层数',
  `status`      char(1)      DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by`   varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`   varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time` datetime     DEFAULT NULL COMMENT '更新时间',
  `remark`      varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`unit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='单元信息';

-- 房屋信息
DROP TABLE IF EXISTS `com_room`;
CREATE TABLE `com_room` (
  `room_id`     bigint NOT NULL AUTO_INCREMENT COMMENT '房屋ID',
  `unit_id`     bigint         DEFAULT NULL COMMENT '所属单元ID',
  `room_number` varchar(20)    DEFAULT ''  COMMENT '房间号',
  `room_type`   varchar(20)    DEFAULT ''  COMMENT '房屋类型（住宅/商铺/办公）',
  `area`        decimal(10,2)  DEFAULT 0.00 COMMENT '建筑面积',
  `use_status`  varchar(20)    DEFAULT ''  COMMENT '使用状态（空置/自住/出租/未入住）',
  `status`      char(1)        DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by`   varchar(64)    DEFAULT ''  COMMENT '创建者',
  `create_time` datetime       DEFAULT NULL COMMENT '创建时间',
  `update_by`   varchar(64)    DEFAULT ''  COMMENT '更新者',
  `update_time` datetime       DEFAULT NULL COMMENT '更新时间',
  `remark`      varchar(500)   DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='房屋信息';

-- 业主/住户信息
DROP TABLE IF EXISTS `com_owner`;
CREATE TABLE `com_owner` (
  `owner_id`       bigint NOT NULL AUTO_INCREMENT COMMENT '业主ID',
  `owner_name`     varchar(50)  DEFAULT ''  COMMENT '姓名',
  `sex`            char(1)      DEFAULT '0' COMMENT '性别（0男 1女 2未知）',
  `id_card`        varchar(18)  DEFAULT ''  COMMENT '身份证号',
  `phone`          varchar(11)  DEFAULT ''  COMMENT '手机号',
  `backup_contact` varchar(100) DEFAULT ''  COMMENT '备用联系方式',
  `owner_type`     varchar(20)  DEFAULT ''  COMMENT '住户类型（业主/租户/家属）',
  `status`         char(1)      DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by`      varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time`    datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`      varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time`    datetime     DEFAULT NULL COMMENT '更新时间',
  `remark`         varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业主/住户信息';

-- 业主房屋关联
DROP TABLE IF EXISTS `com_owner_room`;
CREATE TABLE `com_owner_room` (
  `id`            bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `owner_id`      bigint       DEFAULT NULL COMMENT '业主ID',
  `room_id`       bigint       DEFAULT NULL COMMENT '房屋ID',
  `relation_type` varchar(20)  DEFAULT ''  COMMENT '关联类型（产权人/租户/家属）',
  `check_in_date` date         DEFAULT NULL COMMENT '入住日期',
  `check_out_date` date        DEFAULT NULL COMMENT '搬出日期',
  `is_current`    char(1)      DEFAULT '1' COMMENT '是否当前有效',
  `create_by`     varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time`   datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`     varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time`   datetime     DEFAULT NULL COMMENT '更新时间',
  `remark`        varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业主房屋关联';

-- 报修管理
DROP TABLE IF EXISTS `com_repair`;
CREATE TABLE `com_repair` (
  `repair_id`    bigint NOT NULL AUTO_INCREMENT COMMENT '报修ID',
  `repair_no`    varchar(32)  DEFAULT ''  COMMENT '报修编号',
  `user_id`      bigint       DEFAULT NULL COMMENT '报修人ID',
  `room_id`      bigint       DEFAULT NULL COMMENT '房屋ID',
  `repair_type`  varchar(50)  DEFAULT ''  COMMENT '维修类型',
  `urgency`      varchar(10)  DEFAULT ''  COMMENT '紧急程度（一般/紧急/非常紧急）',
  `description`  text         COMMENT '报修描述',
  `media_urls`   text         COMMENT '图片/视频地址（JSON数组）',
  `status`       varchar(20)  DEFAULT '待受理' COMMENT '状态',
  `assignee_id`  bigint       DEFAULT NULL COMMENT '受理人ID',
  `accept_time`  datetime     DEFAULT NULL COMMENT '受理时间',
  `finish_time`  datetime     DEFAULT NULL COMMENT '完成时间',
  `rating`       int          DEFAULT NULL COMMENT '评价（1-5星）',
  `feedback`     varchar(500) DEFAULT NULL COMMENT '评价内容',
  `create_by`    varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time`  datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`    varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time`  datetime     DEFAULT NULL COMMENT '更新时间',
  `remark`       varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`repair_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报修单';

DROP TABLE IF EXISTS `com_repair_record`;
CREATE TABLE `com_repair_record` (
  `record_id`    bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `repair_id`    bigint         DEFAULT NULL COMMENT '报修单ID',
  `worker_id`    bigint         DEFAULT NULL COMMENT '维修人员ID',
  `action_type`  varchar(20)    DEFAULT ''  COMMENT '操作类型（受理/派单/到场/完工/评价）',
  `description`  varchar(500)   DEFAULT ''  COMMENT '操作描述',
  `proof_urls`   text           COMMENT '完工凭证图片（JSON数组）',
  `repair_fee`   decimal(10,2)  DEFAULT 0.00 COMMENT '维修费用',
  `create_by`    varchar(64)    DEFAULT ''  COMMENT '创建者',
  `create_time`  datetime       DEFAULT NULL COMMENT '创建时间',
  `update_by`    varchar(64)    DEFAULT ''  COMMENT '更新者',
  `update_time`  datetime       DEFAULT NULL COMMENT '更新时间',
  `remark`       varchar(500)   DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='维修记录';

-- 费用管理
DROP TABLE IF EXISTS `com_fee_item`;
CREATE TABLE `com_fee_item` (
  `item_id`       bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `item_name`     varchar(100)  DEFAULT ''  COMMENT '费用名称',
  `charge_type`   varchar(20)   DEFAULT ''  COMMENT '计费方式（固定/计量/分摊）',
  `unit_price`    decimal(10,2) DEFAULT 0.00 COMMENT '单价',
  `billing_cycle` varchar(10)   DEFAULT ''  COMMENT '计费周期（月/季/年）',
  `status`        char(1)       DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by`     varchar(64)   DEFAULT ''  COMMENT '创建者',
  `create_time`   datetime      DEFAULT NULL COMMENT '创建时间',
  `update_by`     varchar(64)   DEFAULT ''  COMMENT '更新者',
  `update_time`   datetime      DEFAULT NULL COMMENT '更新时间',
  `remark`        varchar(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='费用项目';

DROP TABLE IF EXISTS `com_fee_bill`;
CREATE TABLE `com_fee_bill` (
  `bill_id`     bigint NOT NULL AUTO_INCREMENT COMMENT '账单ID',
  `bill_no`     varchar(32)   DEFAULT ''  COMMENT '账单编号',
  `room_id`     bigint        DEFAULT NULL COMMENT '房屋ID',
  `item_id`     bigint        DEFAULT NULL COMMENT '费用项目ID',
  `amount`      decimal(10,2) DEFAULT 0.00 COMMENT '应收金额',
  `paid_amount` decimal(10,2) DEFAULT 0.00 COMMENT '实收金额',
  `bill_period` varchar(20)   DEFAULT ''  COMMENT '账单周期',
  `due_date`    date          DEFAULT NULL COMMENT '截止日期',
  `status`      varchar(10)   DEFAULT '未缴' COMMENT '状态（未缴/已缴/逾期）',
  `pay_time`    datetime      DEFAULT NULL COMMENT '支付时间',
  `pay_method`  varchar(20)   DEFAULT ''  COMMENT '支付方式',
  `create_by`   varchar(64)   DEFAULT ''  COMMENT '创建者',
  `create_time` datetime      DEFAULT NULL COMMENT '创建时间',
  `update_by`   varchar(64)   DEFAULT ''  COMMENT '更新者',
  `update_time` datetime      DEFAULT NULL COMMENT '更新时间',
  `remark`      varchar(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`bill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='费用账单';

DROP TABLE IF EXISTS `com_fee_payment`;
CREATE TABLE `com_fee_payment` (
  `payment_id`     bigint NOT NULL AUTO_INCREMENT COMMENT '缴费ID',
  `bill_id`        bigint         DEFAULT NULL COMMENT '账单ID',
  `amount`         decimal(10,2)  DEFAULT 0.00 COMMENT '缴费金额',
  `pay_method`     varchar(20)    DEFAULT ''  COMMENT '支付方式',
  `transaction_no` varchar(64)    DEFAULT ''  COMMENT '交易流水号',
  `pay_time`       datetime       DEFAULT NULL COMMENT '支付时间',
  `status`         varchar(10)    DEFAULT '成功' COMMENT '支付状态',
  `create_by`      varchar(64)    DEFAULT ''  COMMENT '创建者',
  `create_time`    datetime       DEFAULT NULL COMMENT '创建时间',
  `update_by`      varchar(64)    DEFAULT ''  COMMENT '更新者',
  `update_time`    datetime       DEFAULT NULL COMMENT '更新时间',
  `remark`         varchar(500)   DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`payment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='缴费记录';

-- 停车管理
DROP TABLE IF EXISTS `com_parking_spot`;
CREATE TABLE `com_parking_spot` (
  `spot_id`     bigint NOT NULL AUTO_INCREMENT COMMENT '车位ID',
  `spot_code`   varchar(50)   DEFAULT ''  COMMENT '车位编号',
  `location`    varchar(255)  DEFAULT ''  COMMENT '车位位置',
  `spot_type`   varchar(10)   DEFAULT ''  COMMENT '车位类型（固定/临时）',
  `status`      varchar(10)   DEFAULT '空闲' COMMENT '状态（空闲/已占用）',
  `monthly_fee` decimal(10,2) DEFAULT 0.00 COMMENT '月租费',
  `create_by`   varchar(64)   DEFAULT ''  COMMENT '创建者',
  `create_time` datetime      DEFAULT NULL COMMENT '创建时间',
  `update_by`   varchar(64)   DEFAULT ''  COMMENT '更新者',
  `update_time` datetime      DEFAULT NULL COMMENT '更新时间',
  `remark`      varchar(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`spot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='车位信息';

DROP TABLE IF EXISTS `com_vehicle`;
CREATE TABLE `com_vehicle` (
  `vehicle_id`   bigint NOT NULL AUTO_INCREMENT COMMENT '车辆ID',
  `owner_id`     bigint       DEFAULT NULL COMMENT '车主ID',
  `plate_number` varchar(10)  DEFAULT ''  COMMENT '车牌号',
  `brand`        varchar(50)  DEFAULT ''  COMMENT '车辆品牌',
  `color`        varchar(20)  DEFAULT ''  COMMENT '车辆颜色',
  `vehicle_type` varchar(20)  DEFAULT ''  COMMENT '车辆类型（小型车/大型车/新能源）',
  `spot_id`      bigint       DEFAULT NULL COMMENT '绑定车位ID',
  `status`       char(1)      DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by`    varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time`  datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`    varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time`  datetime     DEFAULT NULL COMMENT '更新时间',
  `remark`       varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`vehicle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='车辆信息';

DROP TABLE IF EXISTS `com_parking_record`;
CREATE TABLE `com_parking_record` (
  `record_id`   bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `vehicle_id`  bigint         DEFAULT NULL COMMENT '车辆ID',
  `spot_id`     bigint         DEFAULT NULL COMMENT '车位ID',
  `entry_time`  datetime       DEFAULT NULL COMMENT '入场时间',
  `exit_time`   datetime       DEFAULT NULL COMMENT '出场时间',
  `fee`         decimal(10,2)  DEFAULT 0.00 COMMENT '停车费用',
  `pay_status`  varchar(10)    DEFAULT ''  COMMENT '支付状态',
  `create_by`   varchar(64)    DEFAULT ''  COMMENT '创建者',
  `create_time` datetime       DEFAULT NULL COMMENT '创建时间',
  `update_by`   varchar(64)    DEFAULT ''  COMMENT '更新者',
  `update_time` datetime       DEFAULT NULL COMMENT '更新时间',
  `remark`      varchar(500)   DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='停车记录';

-- 访客管理
DROP TABLE IF EXISTS `com_visitor`;
CREATE TABLE `com_visitor` (
  `visitor_id`    bigint NOT NULL AUTO_INCREMENT COMMENT '访客ID',
  `inviter_id`    bigint       DEFAULT NULL COMMENT '邀请人ID',
  `visitor_name`  varchar(50)  DEFAULT ''  COMMENT '访客姓名',
  `visitor_phone` varchar(11)  DEFAULT ''  COMMENT '访客手机号',
  `room_id`       bigint       DEFAULT NULL COMMENT '被访房屋ID',
  `expected_time` datetime     DEFAULT NULL COMMENT '预计来访时间',
  `reason`        varchar(200) DEFAULT ''  COMMENT '来访事由',
  `qr_code`       varchar(255) DEFAULT ''  COMMENT '通行二维码',
  `status`        varchar(20)  DEFAULT '待审批' COMMENT '状态',
  `arrival_time`  datetime     DEFAULT NULL COMMENT '实际到达时间',
  `leave_time`    datetime     DEFAULT NULL COMMENT '签离时间',
  `create_by`     varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time`   datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`     varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time`   datetime     DEFAULT NULL COMMENT '更新时间',
  `remark`        varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`visitor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='访客信息';

DROP TABLE IF EXISTS `com_visitor_record`;
CREATE TABLE `com_visitor_record` (
  `record_id`   bigint       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `visitor_id`  bigint       DEFAULT NULL COMMENT '访客ID',
  `pass_type`   varchar(10)  DEFAULT ''  COMMENT '通行类型（入园/出园）',
  `pass_time`   datetime     DEFAULT NULL COMMENT '通行时间',
  `gate_device` varchar(50)  DEFAULT ''  COMMENT '门禁设备',
  `create_by`   varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`   varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time` datetime     DEFAULT NULL COMMENT '更新时间',
  `remark`      varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='访客通行记录';

-- 社区信息发布
DROP TABLE IF EXISTS `com_announcement`;
CREATE TABLE `com_announcement` (
  `announcement_id`  bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title`            varchar(200)  DEFAULT ''  COMMENT '标题',
  `content`          longtext      COMMENT '内容（富文本）',
  `category`         varchar(50)   DEFAULT ''  COMMENT '分类',
  `target_scope`     varchar(20)   DEFAULT ''  COMMENT '发布范围（全部/指定楼栋/指定人群）',
  `target_buildings` text          COMMENT '目标楼栋ID（JSON数组）',
  `target_groups`    varchar(100)  DEFAULT ''  COMMENT '目标人群（业主/租户）',
  `push_method`      varchar(100)  DEFAULT ''  COMMENT '发布方式',
  `expire_date`      datetime      DEFAULT NULL COMMENT '有效期截止',
  `status`           varchar(10)   DEFAULT '草稿' COMMENT '状态（草稿/已发布/已撤回）',
  `publish_time`     datetime      DEFAULT NULL COMMENT '发布时间',
  `create_by`        varchar(64)   DEFAULT ''  COMMENT '创建者',
  `create_time`      datetime      DEFAULT NULL COMMENT '创建时间',
  `update_by`        varchar(64)   DEFAULT ''  COMMENT '更新者',
  `update_time`      datetime      DEFAULT NULL COMMENT '更新时间',
  `remark`           varchar(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`announcement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区公告';

DROP TABLE IF EXISTS `com_announcement_read`;
CREATE TABLE `com_announcement_read` (
  `id`              bigint    NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `announcement_id` bigint    DEFAULT NULL COMMENT '公告ID',
  `user_id`         bigint    DEFAULT NULL COMMENT '用户ID',
  `read_time`       datetime  DEFAULT NULL COMMENT '阅读时间',
  `create_by`       varchar(64) DEFAULT ''  COMMENT '创建者',
  `create_time`     datetime  DEFAULT NULL COMMENT '创建时间',
  `update_by`       varchar(64) DEFAULT ''  COMMENT '更新者',
  `update_time`     datetime  DEFAULT NULL COMMENT '更新时间',
  `remark`          varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公告已读记录';

-- 投诉与建议管理
DROP TABLE IF EXISTS `com_complaint`;
CREATE TABLE `com_complaint` (
  `complaint_id` bigint NOT NULL AUTO_INCREMENT COMMENT '投诉ID',
  `type`         varchar(10)   DEFAULT ''  COMMENT '类型（投诉/建议）',
  `user_id`      bigint        DEFAULT NULL COMMENT '提交人ID',
  `title`        varchar(200)  DEFAULT ''  COMMENT '标题',
  `content`      text          COMMENT '内容',
  `images`       text          COMMENT '图片（JSON数组）',
  `status`       varchar(20)   DEFAULT '待受理' COMMENT '状态',
  `handler_id`   bigint        DEFAULT NULL COMMENT '处理人ID',
  `accept_time`  datetime      DEFAULT NULL COMMENT '受理时间',
  `finish_time`  datetime      DEFAULT NULL COMMENT '完成时间',
  `rating`       int           DEFAULT NULL COMMENT '满意度评价（1-5）',
  `create_by`    varchar(64)   DEFAULT ''  COMMENT '创建者',
  `create_time`  datetime      DEFAULT NULL COMMENT '创建时间',
  `update_by`    varchar(64)   DEFAULT ''  COMMENT '更新者',
  `update_time`  datetime      DEFAULT NULL COMMENT '更新时间',
  `remark`       varchar(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`complaint_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='投诉建议';

DROP TABLE IF EXISTS `com_complaint_feedback`;
CREATE TABLE `com_complaint_feedback` (
  `feedback_id`    bigint       NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `complaint_id`   bigint       DEFAULT NULL COMMENT '投诉ID',
  `handler_id`     bigint       DEFAULT NULL COMMENT '处理人ID',
  `description`    text         COMMENT '处理说明',
  `attachment_urls` text        COMMENT '附件图片（JSON数组）',
  `create_by`      varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time`    datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`      varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time`    datetime     DEFAULT NULL COMMENT '更新时间',
  `remark`         varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`feedback_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='投诉处理反馈';

-- 社区活动管理
DROP TABLE IF EXISTS `com_activity`;
CREATE TABLE `com_activity` (
  `activity_id`         bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `title`               varchar(200) DEFAULT '' COMMENT '活动标题',
  `content`             text         COMMENT '活动内容',
  `activity_time`       datetime     DEFAULT NULL COMMENT '活动时间',
  `location`            varchar(200) DEFAULT '' COMMENT '活动地点',
  `max_participants`    int          DEFAULT 0 COMMENT '报名人数上限',
  `actual_participants` int          DEFAULT 0 COMMENT '实际报名人数',
  `signup_deadline`     datetime     DEFAULT NULL COMMENT '报名截止时间',
  `activity_type`       varchar(50)  DEFAULT '' COMMENT '活动类型',
  `status`              varchar(20)  DEFAULT '草稿' COMMENT '状态',
  `review`              text         COMMENT '活动回顾',
  `photos`              text         COMMENT '活动照片（JSON数组）',
  `create_by`           varchar(64)  DEFAULT '' COMMENT '创建者',
  `create_time`         datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`           varchar(64)  DEFAULT '' COMMENT '更新者',
  `update_time`         datetime     DEFAULT NULL COMMENT '更新时间',
  `remark`              varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区活动';

DROP TABLE IF EXISTS `com_activity_signup`;
CREATE TABLE `com_activity_signup` (
  `signup_id`     bigint      NOT NULL AUTO_INCREMENT COMMENT '报名ID',
  `activity_id`   bigint      DEFAULT NULL COMMENT '活动ID',
  `user_id`       bigint      DEFAULT NULL COMMENT '报名人ID',
  `status`        varchar(10) DEFAULT '已报名' COMMENT '报名状态（已报名/已取消）',
  `signin_time`   datetime    DEFAULT NULL COMMENT '签到时间',
  `signin_method` varchar(10) DEFAULT '' COMMENT '签到方式（扫码/手动）',
  `create_by`     varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time`   datetime    DEFAULT NULL COMMENT '创建时间',
  `update_by`     varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time`   datetime    DEFAULT NULL COMMENT '更新时间',
  `remark`        varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`signup_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动报名';

-- 便民服务管理
DROP TABLE IF EXISTS `com_service_provider`;
CREATE TABLE `com_service_provider` (
  `provider_id`   bigint       NOT NULL AUTO_INCREMENT COMMENT '服务商ID',
  `provider_name` varchar(100) DEFAULT ''  COMMENT '服务商名称',
  `service_type`  varchar(50)  DEFAULT ''  COMMENT '服务类型',
  `contact_name`  varchar(50)  DEFAULT ''  COMMENT '联系人',
  `contact_phone` varchar(11)  DEFAULT ''  COMMENT '联系电话',
  `address`       varchar(255) DEFAULT ''  COMMENT '地址',
  `description`   varchar(500) DEFAULT ''  COMMENT '描述',
  `status`        char(1)      DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by`     varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time`   datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`     varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time`   datetime     DEFAULT NULL COMMENT '更新时间',
  `remark`        varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务商';

DROP TABLE IF EXISTS `com_service_item`;
CREATE TABLE `com_service_item` (
  `item_id`       bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `provider_id`   bigint         DEFAULT NULL COMMENT '服务商ID',
  `item_name`     varchar(100)   DEFAULT ''  COMMENT '服务名称',
  `price`         decimal(10,2)  DEFAULT 0.00 COMMENT '服务价格',
  `duration`      int            DEFAULT 0 COMMENT '服务时长（分钟）',
  `booking_method` varchar(20)   DEFAULT ''  COMMENT '预约方式',
  `status`        char(1)        DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by`     varchar(64)    DEFAULT ''  COMMENT '创建者',
  `create_time`   datetime       DEFAULT NULL COMMENT '创建时间',
  `update_by`     varchar(64)    DEFAULT ''  COMMENT '更新者',
  `update_time`   datetime       DEFAULT NULL COMMENT '更新时间',
  `remark`        varchar(500)   DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务项目';

DROP TABLE IF EXISTS `com_service_order`;
CREATE TABLE `com_service_order` (
  `order_id`     bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no`     varchar(32)   DEFAULT ''  COMMENT '订单编号',
  `user_id`      bigint        DEFAULT NULL COMMENT '用户ID',
  `item_id`      bigint        DEFAULT NULL COMMENT '服务项目ID',
  `booking_time` datetime      DEFAULT NULL COMMENT '预约时间',
  `amount`       decimal(10,2) DEFAULT 0.00 COMMENT '订单金额',
  `status`       varchar(20)   DEFAULT '待接单' COMMENT '状态',
  `rating`       int           DEFAULT NULL COMMENT '服务评价（1-5星）',
  `review`       varchar(500)  DEFAULT ''  COMMENT '评价内容',
  `create_by`    varchar(64)   DEFAULT ''  COMMENT '创建者',
  `create_time`  datetime      DEFAULT NULL COMMENT '创建时间',
  `update_by`    varchar(64)   DEFAULT ''  COMMENT '更新者',
  `update_time`  datetime      DEFAULT NULL COMMENT '更新时间',
  `remark`       varchar(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务订单';


-- ============================================================
-- 第二部分：DML —— 所有数据
-- ============================================================

-- ----------------------------
-- 1. 参数配置
-- ----------------------------
INSERT INTO `sys_config` VALUES
(1,'主框架页-默认皮肤样式名称','sys.index.skinName','skin-blue','Y','admin','2026-03-06 01:54:37','',NULL,'蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow'),
(2,'用户管理-账号初始密码','sys.user.initPassword','123456','Y','admin','2026-03-06 01:54:38','',NULL,'初始化密码 123456'),
(3,'主框架页-侧边栏主题','sys.index.sideTheme','theme-dark','Y','admin','2026-03-06 01:54:38','',NULL,'深色主题theme-dark，浅色主题theme-light'),
(4,'账号自助-是否开启用户注册功能','sys.account.registerUser','true','Y','admin','2026-03-06 01:54:38','',NULL,'是否开启注册用户功能（true开启，false关闭）'),
(5,'用户管理-密码最大错误次数','sys.account.maxRetryCount','5','Y','admin','2026-06-14 13:33:15','admin','2026-06-14 13:33:20','密码最大错误次数，超过后锁定10分钟'),
(6,'用户管理-锁定时间(分)','sys.account.lockTime','10','Y','admin','2026-06-14 13:33:52','admin','2026-06-14 13:33:56','用户锁定时间，默认10分钟');

-- ----------------------------
-- 2. 部门数据
-- ----------------------------
INSERT INTO `sys_dept` VALUES
(100,0,'0','社区服务中心',0,'管理员','15888888888','admin@comser.cn','0','0','admin','2026-03-06 01:54:37','',NULL),
(101,100,'0,100','物业管理部门',1,'张经理','15888888881','zhang@comser.cn','0','0','admin','2026-03-06 01:54:37','',NULL),
(102,100,'0,100','客户服务部门',2,'李主管','15888888882','li@comser.cn','0','0','admin','2026-03-06 01:54:37','',NULL),
(103,100,'0,100','工程维修部门',3,'王工','15888888883','wang@comser.cn','0','0','admin','2026-03-06 01:54:37','',NULL),
(104,100,'0,100','安保管理部门',4,'赵队长','15888888884','zhao@comser.cn','0','0','admin','2026-03-06 01:54:37','',NULL),
(105,100,'0,100','社区活动部门',5,'陈主任','15888888885','chen@comser.cn','0','0','admin','2026-03-06 01:54:37','',NULL);

-- ----------------------------
-- 3. 字典类型
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES
(1,'用户性别','sys_user_sex','0','admin','2026-03-06 01:54:38','',NULL,'用户性别列表'),
(2,'菜单状态','sys_show_hide','0','admin','2026-03-06 01:54:38','',NULL,'菜单状态列表'),
(3,'系统开关','sys_normal_disable','0','admin','2026-03-06 01:54:38','',NULL,'系统开关列表'),
(4,'任务状态','sys_job_status','0','admin','2026-03-06 01:54:38','',NULL,'任务状态列表'),
(5,'任务分组','sys_job_group','0','admin','2026-03-06 01:54:38','',NULL,'任务分组列表'),
(6,'系统是否','sys_yes_no','0','admin','2026-03-06 01:54:38','',NULL,'系统是否列表'),
(7,'通知类型','sys_notice_type','0','admin','2026-03-06 01:54:38','',NULL,'通知类型列表'),
(8,'通知状态','sys_notice_status','0','admin','2026-03-06 01:54:38','',NULL,'通知状态列表'),
(9,'操作类型','sys_oper_type','0','admin','2026-03-06 01:54:38','',NULL,'操作类型列表'),
(10,'系统状态','sys_common_status','0','admin','2026-03-06 01:54:38','',NULL,'登录状态列表');

-- ----------------------------
-- 4. 字典数据
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES
(1,1,'男','0','sys_user_sex','','','Y','0','admin','2026-03-06 01:54:38','',NULL,'性别男'),
(2,2,'女','1','sys_user_sex','','','N','0','admin','2026-03-06 01:54:38','',NULL,'性别女'),
(3,3,'未知','2','sys_user_sex','','','N','0','admin','2026-03-06 01:54:38','',NULL,'性别未知'),
(4,1,'显示','0','sys_show_hide','','primary','Y','0','admin','2026-03-06 01:54:38','',NULL,'显示菜单'),
(5,2,'隐藏','1','sys_show_hide','','danger','N','0','admin','2026-03-06 01:54:38','',NULL,'隐藏菜单'),
(6,1,'正常','0','sys_normal_disable','','primary','Y','0','admin','2026-03-06 01:54:38','',NULL,'正常状态'),
(7,2,'停用','1','sys_normal_disable','','danger','N','0','admin','2026-03-06 01:54:38','',NULL,'停用状态'),
(8,1,'正常','0','sys_job_status','','primary','Y','0','admin','2026-03-06 01:54:38','',NULL,'正常状态'),
(9,2,'暂停','1','sys_job_status','','danger','N','0','admin','2026-03-06 01:54:38','',NULL,'停用状态'),
(10,1,'默认','DEFAULT','sys_job_group','','','Y','0','admin','2026-03-06 01:54:38','',NULL,'默认分组'),
(11,2,'系统','SYSTEM','sys_job_group','','','N','0','admin','2026-03-06 01:54:38','',NULL,'系统分组'),
(12,1,'是','Y','sys_yes_no','','primary','Y','0','admin','2026-03-06 01:54:38','',NULL,'系统默认是'),
(13,2,'否','N','sys_yes_no','','danger','N','0','admin','2026-03-06 01:54:38','',NULL,'系统默认否'),
(14,1,'通知','1','sys_notice_type','','warning','Y','0','admin','2026-03-06 01:54:38','',NULL,'通知'),
(15,2,'公告','2','sys_notice_type','','success','N','0','admin','2026-03-06 01:54:38','',NULL,'公告'),
(16,1,'正常','0','sys_notice_status','','primary','Y','0','admin','2026-03-06 01:54:38','',NULL,'正常状态'),
(17,2,'关闭','1','sys_notice_status','','danger','N','0','admin','2026-03-06 01:54:38','',NULL,'关闭状态'),
(18,99,'其他','0','sys_oper_type','','info','N','0','admin','2026-03-06 01:54:38','',NULL,'其他操作'),
(19,1,'新增','1','sys_oper_type','','info','N','0','admin','2026-03-06 01:54:38','',NULL,'新增操作'),
(20,2,'修改','2','sys_oper_type','','info','N','0','admin','2026-03-06 01:54:38','',NULL,'修改操作'),
(21,3,'删除','3','sys_oper_type','','danger','N','0','admin','2026-03-06 01:54:38','',NULL,'删除操作'),
(22,4,'授权','4','sys_oper_type','','primary','N','0','admin','2026-03-06 01:54:38','',NULL,'授权操作'),
(23,5,'导出','5','sys_oper_type','','warning','N','0','admin','2026-03-06 01:54:38','',NULL,'导出操作'),
(24,6,'导入','6','sys_oper_type','','warning','N','0','admin','2026-03-06 01:54:38','',NULL,'导入操作'),
(25,7,'强退','7','sys_oper_type','','danger','N','0','admin','2026-03-06 01:54:38','',NULL,'强退操作'),
(26,8,'生成代码','8','sys_oper_type','','warning','N','0','admin','2026-03-06 01:54:38','',NULL,'生成操作'),
(27,9,'清空数据','9','sys_oper_type','','danger','N','0','admin','2026-03-06 01:54:38','',NULL,'清空操作'),
(28,1,'成功','0','sys_common_status','','primary','Y','0','admin','2026-03-06 01:54:38','',NULL,'正常状态'),
(29,2,'失败','1','sys_common_status','','danger','N','0','admin','2026-03-06 01:54:38','',NULL,'停用状态');

-- ----------------------------
-- 5. 定时任务
-- ----------------------------
INSERT INTO `sys_job` VALUES
(1,'系统默认（无参）','DEFAULT','ryTask.ryNoParams','0/10 * * * * ?','3','1','1','admin','2026-03-06 01:54:38','',NULL,''),
(2,'系统默认（有参）','DEFAULT','ryTask.ryParams(''ry'')','0/15 * * * * ?','3','1','1','admin','2026-03-06 01:54:38','',NULL,''),
(3,'系统默认（多参）','DEFAULT','ryTask.ryMultipleParams(''ry'', true, 2000L, 316.50D, 100)','0/20 * * * * ?','3','1','1','admin','2026-03-06 01:54:38','',NULL,'');

-- ----------------------------
-- 6. 系统通知
-- ----------------------------
INSERT INTO `sys_notice` VALUES
(1,'温馨提醒：社区服务系统已上线','2',_binary '欢迎使用社区服务管理系统，如有问题请联系管理员。','0','admin','2026-03-06 01:54:38','',NULL,'管理员'),
(2,'维护通知：系统将于凌晨进行升级维护','1',_binary '维护内容：系统性能优化','0','admin','2026-03-06 01:54:38','',NULL,'管理员');

-- ----------------------------
-- 7. 岗位信息
-- ----------------------------
INSERT INTO `sys_post` VALUES
(1,'admin','系统管理员',1,'0','admin','2026-03-06 01:54:37','',NULL,''),
(2,'property','物业管理员',2,'0','admin','2026-03-06 01:54:37','',NULL,''),
(3,'service','客服专员',3,'0','admin','2026-03-06 01:54:37','',NULL,''),
(4,'repair','维修工程师',4,'0','admin','2026-03-06 01:54:37','',NULL,'');

-- ----------------------------
-- 8. 角色信息
-- ----------------------------
INSERT INTO `sys_role` VALUES
(1,'若依超管(备用)','admin',1,'1',1,1,'0','0','admin','2026-03-06 01:54:37','',NULL,'框架自带超管，日常不使用'),
(2,'社区管理员','admin_community',2,'2',1,1,'0','0','admin','2026-03-06 01:54:37','',NULL,'社区管理员，管理系统所有功能'),
(3,'社区居民','resident',3,'5',1,1,'0','0','admin','2026-03-06 01:54:37','',NULL,'社区居民，仅使用居民端功能');

-- ----------------------------
-- 9. 角色-部门关联
-- ----------------------------
INSERT INTO `sys_role_dept` VALUES (2,100),(2,101),(2,102);

-- ----------------------------
-- 10. 用户信息（密码均为: admin123）
-- ----------------------------
INSERT INTO `sys_user` VALUES
(1,100,'admin','若依超管(备用)','00','admin@comser.cn','15888888888','0','','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','0','0','127.0.0.1','2026-03-06 01:54:52',NULL,'admin','2026-03-06 01:54:37',NULL,NULL,'若依超管，日常不使用'),
(2,101,'zhang','张经理','00','zhang@comser.cn','15888888881','0','','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','0','0',NULL,NULL,NULL,'admin','2026-03-06 01:54:37',NULL,NULL,'社区管理员'),
(100,102,'resident','李明','00','resident@comser.cn','13900000001','0','','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','0','0',NULL,NULL,NULL,'admin','2026-03-06 01:54:37',NULL,NULL,'社区居民');

-- ----------------------------
-- 11. 用户-岗位关联
-- ----------------------------
INSERT INTO `sys_user_post` VALUES (1,1),(2,2),(100,4);

-- ----------------------------
-- 12. 用户-角色关联
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1,1),(2,2),(100,3);

-- ----------------------------
-- 13. 系统访问记录
-- ----------------------------
INSERT INTO `sys_logininfor` VALUES (1,'admin','127.0.0.1','内网IP','Chrome','Windows 10','0','登录成功','2026-03-06 01:54:52');

-- ----------------------------
-- 14. 菜单权限（最终形态：已应用所有 V1.2~V1.5 变更）
--     顶层菜单共 10 个：用户管理 + 9 个社区服务模块
--     - 删除了原若依的 系统管理/角色/菜单/部门/岗位/字典/通知公告
--     - 权限前缀从 system:user:* 修正为 admin:user:*
--     - 组件路径从 system/user/index 修正为 admin/user/index
--     - 所有社区服务模块提升为顶级菜单
-- ----------------------------

-- 14.1 用户管理（顶级菜单，order_num=0）
INSERT INTO `sys_menu` VALUES
(2, '用户管理', 0, 0, 'system/user', 'admin/user/index', NULL, '', 1, 0, 'C', '0', '0', 'admin:user:list',      'user',    'admin', NOW(), '', NULL, ''),
(3, '用户查询', 2, 1, '',     NULL,                NULL, '', 1, 0, 'F', '0', '0', 'admin:user:query',    '#',       'admin', NOW(), '', NULL, ''),
(4, '用户新增', 2, 2, '',     NULL,                NULL, '', 1, 0, 'F', '0', '0', 'admin:user:add',      '#',       'admin', NOW(), '', NULL, ''),
(5, '用户修改', 2, 3, '',     NULL,                NULL, '', 1, 0, 'F', '0', '0', 'admin:user:edit',     '#',       'admin', NOW(), '', NULL, ''),
(6, '用户删除', 2, 4, '',     NULL,                NULL, '', 1, 0, 'F', '0', '0', 'admin:user:remove',   '#',       'admin', NOW(), '', NULL, ''),
(7, '用户导出', 2, 5, '',     NULL,                NULL, '', 1, 0, 'F', '0', '0', 'admin:user:export',   '#',       'admin', NOW(), '', NULL, ''),
(14,'重置密码', 2, 6, '',     NULL,                NULL, '', 1, 0, 'F', '0', '0', 'admin:user:resetPwd', '#',       'admin', NOW(), '', NULL, '');

-- 14.2 社区服务模块（顶级菜单，order_num=1~9）
INSERT INTO `sys_menu` VALUES
(101,'房产管理',  0, 1, 'property',     'com/property/building',    NULL, '', 1, 0, 'C', '0', '0', 'com:property:building:list',        'tree',      'admin', NOW(), '', NULL, ''),
(102,'报修管理',  0, 2, 'repair',       'com/repair/index',         NULL, '', 1, 0, 'C', '0', '0', 'com:repair:list',                   'tool',      'admin', NOW(), '', NULL, ''),
(103,'费用管理',  0, 3, 'fee',          'com/fee/index',            NULL, '', 1, 0, 'C', '0', '0', 'com:fee:item:list',                 'money',     'admin', NOW(), '', NULL, ''),
(104,'停车管理',  0, 4, 'parking',      'com/parking/index',        NULL, '', 1, 0, 'C', '0', '0', 'com:parking:spot:list',             'cascader',  'admin', NOW(), '', NULL, ''),
(105,'访客管理',  0, 5, 'visitor',      'com/visitor/index',        NULL, '', 1, 0, 'C', '0', '0', 'com:visitor:list',                  'people',    'admin', NOW(), '', NULL, ''),
(106,'信息发布',  0, 6, 'announcement', 'com/announcement/index',   NULL, '', 1, 0, 'C', '0', '0', 'com:announcement:list',             'message',   'admin', NOW(), '', NULL, ''),
(107,'投诉建议',  0, 7, 'complaint',    'com/complaint/index',      NULL, '', 1, 0, 'C', '0', '0', 'com:complaint:list',                'edit',      'admin', NOW(), '', NULL, ''),
(108,'社区活动',  0, 8, 'activity',     'com/activity/index',       NULL, '', 1, 0, 'C', '0', '0', 'com:activity:list',                 'example',   'admin', NOW(), '', NULL, ''),
(109,'便民服务',  0, 9, 'convenience',  'com/convenience/index',    NULL, '', 1, 0, 'C', '0', '0', 'com:convenience:provider:list',     'shopping',  'admin', NOW(), '', NULL, '');

-- 14.3 房产管理 - 按钮级权限 (parent: menu_id=101)
INSERT INTO `sys_menu` VALUES
(10101,'楼栋查询', 101, 1,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:building:query',          '#', 'admin', NOW(), '', NULL, ''),
(10102,'楼栋新增', 101, 2,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:building:add',            '#', 'admin', NOW(), '', NULL, ''),
(10103,'楼栋修改', 101, 3,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:building:edit',           '#', 'admin', NOW(), '', NULL, ''),
(10104,'楼栋删除', 101, 4,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:building:remove',         '#', 'admin', NOW(), '', NULL, ''),
(10105,'单元查询', 101, 5,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:unit:query',              '#', 'admin', NOW(), '', NULL, ''),
(10106,'单元新增', 101, 6,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:unit:add',                '#', 'admin', NOW(), '', NULL, ''),
(10107,'单元修改', 101, 7,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:unit:edit',               '#', 'admin', NOW(), '', NULL, ''),
(10108,'单元删除', 101, 8,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:unit:remove',             '#', 'admin', NOW(), '', NULL, ''),
(10109,'房屋查询', 101, 9,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:room:query',              '#', 'admin', NOW(), '', NULL, ''),
(10110,'房屋新增', 101, 10, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:room:add',                '#', 'admin', NOW(), '', NULL, ''),
(10111,'房屋修改', 101, 11, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:room:edit',               '#', 'admin', NOW(), '', NULL, ''),
(10112,'房屋删除', 101, 12, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:room:remove',             '#', 'admin', NOW(), '', NULL, ''),
(10113,'居民查询', 101, 13, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:owner:list',              '#', 'admin', NOW(), '', NULL, ''),
(10114,'居民新增', 101, 14, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:owner:add',               '#', 'admin', NOW(), '', NULL, ''),
(10115,'居民修改', 101, 15, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:owner:edit',              '#', 'admin', NOW(), '', NULL, ''),
(10116,'居民删除', 101, 16, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:owner:remove',            '#', 'admin', NOW(), '', NULL, ''),
(10117,'关联查询', 101, 17, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:ownerroom:list',          '#', 'admin', NOW(), '', NULL, ''),
(10118,'关联新增', 101, 18, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:ownerroom:add',           '#', 'admin', NOW(), '', NULL, ''),
(10119,'关联修改', 101, 19, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:ownerroom:edit',          '#', 'admin', NOW(), '', NULL, ''),
(10120,'关联删除', 101, 20, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:ownerroom:remove',        '#', 'admin', NOW(), '', NULL, ''),
(10121,'居民导出', 101, 21, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:owner:export',            '#', 'admin', NOW(), '', NULL, ''),
(10122,'居民导入', 101, 22, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:property:owner:import',            '#', 'admin', NOW(), '', NULL, '');

-- 14.4 报修管理 - 按钮级权限 (parent: menu_id=102)
INSERT INTO `sys_menu` VALUES
(10201,'报修查询',     102, 1,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:repair:query',         '#', 'admin', NOW(), '', NULL, ''),
(10202,'报修新增',     102, 2,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:repair:add',           '#', 'admin', NOW(), '', NULL, ''),
(10203,'报修修改',     102, 3,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:repair:edit',          '#', 'admin', NOW(), '', NULL, ''),
(10204,'报修删除',     102, 4,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:repair:remove',        '#', 'admin', NOW(), '', NULL, ''),
(10205,'报修受理',     102, 5,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:repair:assign',        '#', 'admin', NOW(), '', NULL, ''),
(10206,'报修完工',     102, 6,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:repair:finish',        '#', 'admin', NOW(), '', NULL, ''),
(10207,'报修评价',     102, 7,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:repair:rate',          '#', 'admin', NOW(), '', NULL, ''),
(10208,'报修取消',     102, 8,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:repair:cancel',        '#', 'admin', NOW(), '', NULL, ''),
(10209,'维修记录查询', 102, 9,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:repair:record:list',   '#', 'admin', NOW(), '', NULL, ''),
(10210,'维修记录新增', 102, 10, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:repair:record:add',    '#', 'admin', NOW(), '', NULL, '');

-- 14.5 费用管理 - 按钮级权限 (parent: menu_id=103)
INSERT INTO `sys_menu` VALUES
(10301,'费用项目查询', 103, 1,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:fee:item:query',     '#', 'admin', NOW(), '', NULL, ''),
(10302,'费用项目新增', 103, 2,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:fee:item:add',       '#', 'admin', NOW(), '', NULL, ''),
(10303,'费用项目修改', 103, 3,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:fee:item:edit',      '#', 'admin', NOW(), '', NULL, ''),
(10304,'费用项目删除', 103, 4,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:fee:item:remove',    '#', 'admin', NOW(), '', NULL, ''),
(10305,'账单查询',     103, 5,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:fee:bill:list',      '#', 'admin', NOW(), '', NULL, ''),
(10306,'账单详情',     103, 6,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:fee:bill:query',     '#', 'admin', NOW(), '', NULL, ''),
(10307,'账单新增',     103, 7,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:fee:bill:add',       '#', 'admin', NOW(), '', NULL, ''),
(10308,'账单删除',     103, 8,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:fee:bill:remove',    '#', 'admin', NOW(), '', NULL, ''),
(10309,'缴费记录查询', 103, 9,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:fee:payment:list',   '#', 'admin', NOW(), '', NULL, ''),
(10310,'缴费新增',     103, 10, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:fee:payment:add',    '#', 'admin', NOW(), '', NULL, '');

-- 14.6 停车管理 - 按钮级权限 (parent: menu_id=104)
INSERT INTO `sys_menu` VALUES
(10401,'车位查询',     104, 1,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:parking:spot:query',    '#', 'admin', NOW(), '', NULL, ''),
(10402,'车位新增',     104, 2,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:parking:spot:add',      '#', 'admin', NOW(), '', NULL, ''),
(10403,'车位修改',     104, 3,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:parking:spot:edit',     '#', 'admin', NOW(), '', NULL, ''),
(10404,'车位删除',     104, 4,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:parking:spot:remove',   '#', 'admin', NOW(), '', NULL, ''),
(10405,'车辆查询',     104, 5,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:parking:vehicle:list',  '#', 'admin', NOW(), '', NULL, ''),
(10406,'车辆详情',     104, 6,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:parking:vehicle:query', '#', 'admin', NOW(), '', NULL, ''),
(10407,'车辆新增',     104, 7,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:parking:vehicle:add',   '#', 'admin', NOW(), '', NULL, ''),
(10408,'车辆修改',     104, 8,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:parking:vehicle:edit',  '#', 'admin', NOW(), '', NULL, ''),
(10409,'车辆删除',     104, 9,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:parking:vehicle:remove','#', 'admin', NOW(), '', NULL, ''),
(10410,'停车记录查询', 104, 10, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:parking:record:list',   '#', 'admin', NOW(), '', NULL, ''),
(10411,'停车记录新增', 104, 11, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:parking:record:add',    '#', 'admin', NOW(), '', NULL, '');

-- 14.7 访客管理 - 按钮级权限 (parent: menu_id=105)
INSERT INTO `sys_menu` VALUES
(10501,'访客查询',     105, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:visitor:query',        '#', 'admin', NOW(), '', NULL, ''),
(10502,'访客新增',     105, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:visitor:add',          '#', 'admin', NOW(), '', NULL, ''),
(10503,'访客审批',     105, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:visitor:approve',      '#', 'admin', NOW(), '', NULL, ''),
(10504,'访客签离',     105, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:visitor:checkout',     '#', 'admin', NOW(), '', NULL, ''),
(10505,'访客删除',     105, 5, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:visitor:remove',       '#', 'admin', NOW(), '', NULL, ''),
(10506,'通行记录查询', 105, 6, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:visitor:record:list',  '#', 'admin', NOW(), '', NULL, '');

-- 14.8 信息发布 - 按钮级权限 (parent: menu_id=106)
INSERT INTO `sys_menu` VALUES
(10601,'公告查询',     106, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:announcement:query',      '#', 'admin', NOW(), '', NULL, ''),
(10602,'公告新增',     106, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:announcement:add',        '#', 'admin', NOW(), '', NULL, ''),
(10603,'公告修改',     106, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:announcement:edit',       '#', 'admin', NOW(), '', NULL, ''),
(10604,'公告发布',     106, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:announcement:publish',     '#', 'admin', NOW(), '', NULL, ''),
(10605,'公告删除',     106, 5, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:announcement:remove',      '#', 'admin', NOW(), '', NULL, ''),
(10606,'已读记录查询', 106, 6, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:announcement:read:list',   '#', 'admin', NOW(), '', NULL, '');

-- 14.9 投诉建议 - 按钮级权限 (parent: menu_id=107)
INSERT INTO `sys_menu` VALUES
(10701,'投诉查询', 107, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:complaint:query',         '#', 'admin', NOW(), '', NULL, ''),
(10702,'投诉新增', 107, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:complaint:add',           '#', 'admin', NOW(), '', NULL, ''),
(10703,'投诉受理', 107, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:complaint:accept',        '#', 'admin', NOW(), '', NULL, ''),
(10704,'投诉完成', 107, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:complaint:finish',        '#', 'admin', NOW(), '', NULL, ''),
(10705,'投诉删除', 107, 5, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:complaint:remove',        '#', 'admin', NOW(), '', NULL, ''),
(10706,'反馈查询', 107, 6, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:complaint:feedback:list', '#', 'admin', NOW(), '', NULL, ''),
(10707,'反馈新增', 107, 7, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:complaint:feedback:add',  '#', 'admin', NOW(), '', NULL, '');

-- 14.10 社区活动 - 按钮级权限 (parent: menu_id=108)
INSERT INTO `sys_menu` VALUES
(10801,'活动查询', 108, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:activity:query',        '#', 'admin', NOW(), '', NULL, ''),
(10802,'活动新增', 108, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:activity:add',          '#', 'admin', NOW(), '', NULL, ''),
(10803,'活动修改', 108, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:activity:edit',         '#', 'admin', NOW(), '', NULL, ''),
(10804,'活动删除', 108, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:activity:remove',       '#', 'admin', NOW(), '', NULL, ''),
(10805,'报名查询', 108, 5, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:activity:signup:list',  '#', 'admin', NOW(), '', NULL, ''),
(10806,'报名添加', 108, 6, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:activity:signup:add',   '#', 'admin', NOW(), '', NULL, ''),
(10807,'签到',     108, 7, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:activity:signin',       '#', 'admin', NOW(), '', NULL, '');

-- 14.11 便民服务 - 按钮级权限 (parent: menu_id=109)
INSERT INTO `sys_menu` VALUES
(10901,'服务商查询',   109, 1,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:convenience:provider:query', '#', 'admin', NOW(), '', NULL, ''),
(10902,'服务商新增',   109, 2,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:convenience:provider:add',   '#', 'admin', NOW(), '', NULL, ''),
(10903,'服务商修改',   109, 3,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:convenience:provider:edit',  '#', 'admin', NOW(), '', NULL, ''),
(10904,'服务商删除',   109, 4,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:convenience:provider:remove','#', 'admin', NOW(), '', NULL, ''),
(10905,'服务项目查询', 109, 5,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:convenience:item:list',      '#', 'admin', NOW(), '', NULL, ''),
(10906,'服务项目详情', 109, 6,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:convenience:item:query',     '#', 'admin', NOW(), '', NULL, ''),
(10907,'服务项目新增', 109, 7,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:convenience:item:add',       '#', 'admin', NOW(), '', NULL, ''),
(10908,'服务项目修改', 109, 8,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:convenience:item:edit',      '#', 'admin', NOW(), '', NULL, ''),
(10909,'服务项目删除', 109, 9,  '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:convenience:item:remove',    '#', 'admin', NOW(), '', NULL, ''),
(10910,'订单查询',     109, 10, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:convenience:order:list',     '#', 'admin', NOW(), '', NULL, ''),
(10911,'订单详情',     109, 11, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:convenience:order:query',    '#', 'admin', NOW(), '', NULL, ''),
(10912,'订单新增',     109, 12, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'com:convenience:order:add',      '#', 'admin', NOW(), '', NULL, '');

-- ----------------------------
-- 15. 角色-菜单关联
--     - 若依超管(role_id=1): 所有菜单
--     - 社区管理员(role_id=2): 用户管理 + 所有社区服务模块
--     - 社区居民(role_id=3): 仅社区服务模块
-- ----------------------------
INSERT INTO `sys_role_menu` SELECT 1, menu_id FROM `sys_menu`;
INSERT INTO `sys_role_menu` SELECT 2, menu_id FROM `sys_menu` WHERE (menu_id <= 7 OR menu_id = 14) OR menu_id >= 101;
INSERT INTO `sys_role_menu` SELECT 3, menu_id FROM `sys_menu` WHERE menu_id >= 101;

SET FOREIGN_KEY_CHECKS = 1;
