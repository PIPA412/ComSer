package com.zsc.module.dashboard.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预警规则 com_alert_rule
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_alert_rule")
public class ComAlertRule extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long ruleId;

    /** 规则名称 */
    private String ruleName;

    /** 指标标识（collection_rate / complaint_monthly / repair_overdue_rate 等） */
    private String metricKey;

    /** 指标名称（展示用） */
    private String metricName;

    /** 比较方式（GT大于 / LT小于 / GTE / LTE） */
    private String compareType;

    /** 阈值 */
    private java.math.BigDecimal threshold;

    /** 是否启用（0启用 1停用） */
    private String status;
}
