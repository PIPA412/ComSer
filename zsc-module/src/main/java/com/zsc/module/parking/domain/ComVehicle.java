package com.zsc.module.parking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 车辆信息 com_vehicle
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_vehicle")
public class ComVehicle extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long vehicleId;

    /** 车主ID */
    private Long ownerId;

    /** 车牌号 */
    private String plateNumber;

    /** 车辆品牌 */
    private String brand;

    /** 车辆颜色 */
    private String color;

    /** 车辆类型（小型车/大型车/新能源） */
    private String vehicleType;

    /** 绑定车位ID */
    private Long spotId;

    /** 状态（0正常 1停用） */
    private String status;
}
