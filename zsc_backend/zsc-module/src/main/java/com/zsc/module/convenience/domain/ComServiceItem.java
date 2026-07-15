package com.zsc.module.convenience.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Map;

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

    /** 预约方式（线上/电话） */
    private String bookingMethod;

    /** 服务详情 */
    private String serviceDetail;

    /** 状态（0正常 1停用） */
    private String status;

    @TableField(exist = false)
    private Map<String, Object> params;

    /**
     * 搜索条件（非数据库字段）
     */
    @TableField(exist = false)
    private String searchValue;
}
