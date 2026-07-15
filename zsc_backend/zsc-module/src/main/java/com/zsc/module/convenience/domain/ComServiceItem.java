package com.zsc.module.convenience.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 服务项目 com_service_item
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_service_item")
public class ComServiceItem extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long itemId;

    /** 服务商ID */
    private Long providerId;

    /** 服务名称 */
    private String itemName;

    /** 服务价格 */
    private BigDecimal price;

    /** 服务时长（分钟） */
    private Integer duration;

    /** 预约方式（在线预约/电话预约） */
    private String bookingMethod;

    /** 状态（0正常 1停用） */
    private String status;
}
