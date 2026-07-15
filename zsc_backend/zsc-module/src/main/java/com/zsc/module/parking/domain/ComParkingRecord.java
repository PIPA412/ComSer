package com.zsc.module.parking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 停车记录 com_parking_record
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_parking_record")
public class ComParkingRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long recordId;

    /** 车辆ID */
    private Long vehicleId;

    /** 车位ID */
    private Long spotId;

    /** 入场时间 */
    private Date entryTime;

    /** 出场时间 */
    private Date exitTime;

    /** 停车费用 */
    private java.math.BigDecimal fee;

    /** 支付状态 */
    private String payStatus;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String plateNumber;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String ownerName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String spotCode;
}
