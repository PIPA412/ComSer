package com.zsc.module.convenience.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * 服务订单 com_service_order
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_service_order")
public class ComServiceOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 服务项目ID */
    private Long itemId;

    /** 联系人 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 预约时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bookingTime;

    /** 订单金额 */
    private BigDecimal amount;

    /** 状态（待接单/已接单/已完成/已取消） */
    private String status;

    /** 服务商名称（非数据库字段，用于前端展示） */
    @TableField(exist = false)
    private String providerName;

    /** 服务名称（非数据库字段，用于前端展示） */
    @TableField(exist = false)
    private String itemName;

    @TableField(exist = false)
    private Map<String, Object> params;

    /**
     * 搜索条件（非数据库字段）
     */
    @TableField(exist = false)
    private String searchValue;
}
