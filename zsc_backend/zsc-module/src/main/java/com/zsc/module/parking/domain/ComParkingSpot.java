package com.zsc.module.parking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 车位信息 com_parking_spot
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_parking_spot")
public class ComParkingSpot extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long spotId;

    /** 车位编号 */
    private String spotCode;

    /** 车位位置 */
    private String location;

    /** 车位类型（固定/临时） */
    private String spotType;

    /** 状态（空闲/已占用） */
    private String status;

    /** 月租费 */
    private java.math.BigDecimal monthlyFee;

    /** 小时计费（元/小时） */
    private java.math.BigDecimal hourlyFee;
}
