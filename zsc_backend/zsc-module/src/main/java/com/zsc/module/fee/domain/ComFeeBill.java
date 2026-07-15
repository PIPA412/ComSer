package com.zsc.module.fee.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 费用账单 com_fee_bill
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_fee_bill")
public class ComFeeBill extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long billId;

    /** 账单编号 */
    private String billNo;

    /** 房屋ID */
    private Long roomId;

    /** 费用项目ID */
    private Long itemId;

    /** 应收金额 */
    private BigDecimal amount;

    /** 实收金额 */
    private BigDecimal paidAmount;

    /** 账单周期 */
    private String billPeriod;

    /** 截止日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dueDate;

    /** 状态（未缴/已缴/逾期） */
    private String status;

    /** 支付时间 */
    private Date payTime;

    /** 支付方式（微信/支付宝/银行转账/现金） */
    private String payMethod;

    /** 房间号（非数据库字段，关联查询填充） */
    @TableField(exist = false)
    private String roomNumber;

    /** 业主姓名（非数据库字段） */
    @TableField(exist = false)
    private String ownerName;

    /** 业主手机号（非数据库字段） */
    @TableField(exist = false)
    private String ownerPhone;

    /** 费用项目名称（非数据库字段） */
    @TableField(exist = false)
    private String itemName;
}
