package com.zsc.module.property.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 单元信息 com_unit
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_unit")
public class ComUnit extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long unitId;

    /** 所属楼栋ID */
    @NotNull(message = "所属楼栋不能为空")
    private Long buildingId;

    /** 单元名称 */
    @NotBlank(message = "单元名称不能为空")
    @Size(max = 100, message = "单元名称不能超过100个字符")
    private String unitName;

    /** 单元编号 */
    @Size(max = 50, message = "单元编号不能超过50个字符")
    private String unitCode;

    /** 楼层数 */
    @Min(value = 1, message = "楼层数至少为1")
    private Integer floorCount;

    /** 状态（0正常 1停用） */
    @Pattern(regexp = "[01]", message = "状态值无效")
    private String status;
}
