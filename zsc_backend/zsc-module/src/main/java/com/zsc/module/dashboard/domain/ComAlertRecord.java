package com.zsc.module.dashboard.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预警记录 com_alert_record
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_alert_record")
public class ComAlertRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long recordId;

    /** 关联规则ID */
    private Long ruleId;

    /** 规则名称（冗余） */
    private String ruleName;

    /** 指标名称 */
    private String metricName;

    /** 触发值 */
    private java.math.BigDecimal triggerValue;

    /** 阈值 */
    private java.math.BigDecimal threshold;

    /** 触发时间 */
    private java.util.Date triggerTime;

    /** 处置状态（待处理/已处理/已忽略） */
    private String handleStatus;

    /** 处置人 */
    private String handleBy;

    /** 处置时间 */
    private java.util.Date handleTime;

    /** 处置备注 */
    private String handleRemark;
}
