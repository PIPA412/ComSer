package com.zsc.module.fee.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 费用项目 com_fee_item
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_fee_item")
public class ComFeeItem extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long itemId;

    /** 费用名称 */
    private String itemName;

    /** 计费方式（固定/计量/分摊） */
    private String chargeType;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 计费周期（月/季/年） */
    private String billingCycle;

    /** 状态（0正常 1停用） */
    private String status;
}
