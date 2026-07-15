package com.zsc.module.parking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 月卡申请 com_parking_monthly_apply
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_parking_monthly_apply")
public class ComParkingMonthlyApply extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long applyId;
    private Long ownerId;
    private Long vehicleId;
    private Long spotId;
    private Integer months;
    private BigDecimal amount;
    private String status;
    private java.util.Date approveTime;
    private String approveBy;

    /** 非数据库字段 */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String plateNumber;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String spotCode;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String ownerName;
}
