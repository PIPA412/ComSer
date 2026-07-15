package com.zsc.module.convenience.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 服务评价 com_service_review
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_service_review")
public class ComServiceReview extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long reviewId;

    /** 订单ID */
    private Long orderId;

    /** 用户ID */
    private Long userId;

    /** 服务项目ID */
    private Long itemId;

    /** 服务商ID */
    private Long providerId;

    /** 评分（1-5星） */
    private Integer rating;

    /** 评价内容 */
    private String content;

    /** 服务商名称（非数据库字段，用于前端展示） */
    @TableField(exist = false)
    private String providerName;

    /** 服务名称（非数据库字段，用于前端展示） */
    @TableField(exist = false)
    private String itemName;

    /** 订单编号（非数据库字段，用于前端展示） */
    @TableField(exist = false)
    private String orderNo;

    @TableField(exist = false)
    private Map<String, Object> params;

    /**
     * 搜索条件（非数据库字段）
     */
    @TableField(exist = false)
    private String searchValue;
}
