package com.zsc.module.property.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 房屋信息 com_room
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_room")
public class ComRoom extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long roomId;

    /** 所属单元ID */
    @NotNull(message = "所属单元不能为空")
    private Long unitId;

    /** 房间号 */
    @NotBlank(message = "房间号不能为空")
    @Size(max = 20, message = "房间号不能超过20个字符")
    private String roomNumber;

    /** 房屋类型（住宅/商铺/办公） */
    @Size(max = 20, message = "房屋类型不能超过20个字符")
    private String roomType;

    /** 建筑面积 */
    @DecimalMin(value = "0.01", message = "面积必须大于0")
    private BigDecimal area;

    /** 使用状态（空置/自住/出租/未入住） */
    @Size(max = 20, message = "使用状态不能超过20个字符")
    private String useStatus;

    /** 状态（0正常 1停用） */
    @Pattern(regexp = "[01]", message = "状态值无效")
    private String status;
}
