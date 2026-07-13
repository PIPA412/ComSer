package com.zsc.module.fee.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 缴费记录 com_fee_payment
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_fee_payment")
public class ComFeePayment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long paymentId;

    /** 账单ID */
    private Long billId;

    /** 缴费金额 */
    private BigDecimal amount;

    /** 支付方式 */
    private String payMethod;

    /** 交易流水号 */
    private String transactionNo;

    /** 支付时间 */
    private Date payTime;

    /** 支付状态 */
    private String status;
}
