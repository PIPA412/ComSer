package com.zsc.module.convenience.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

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

    /** 预约时间 */
    private Date bookingTime;

    /** 订单金额 */
    private BigDecimal amount;

    /** 状态（待接单/已接单/服务中/已完成/已取消） */
    private String status;

    /** 服务评价（1-5星） */
    private Integer rating;

    /** 评价内容 */
    private String review;
}
