package com.zsc.module.property.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 楼栋信息 com_building
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_building")
public class ComBuilding extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long buildingId;

    /** 楼栋名称 */
    @NotBlank(message = "楼栋名称不能为空")
    @Size(max = 100, message = "楼栋名称不能超过100个字符")
    private String buildingName;

    /** 楼栋编号 */
    @Size(max = 50, message = "楼栋编号不能超过50个字符")
    private String buildingCode;

    /** 楼层数 */
    @Min(value = 1, message = "楼层数至少为1")
    private Integer floorCount;

    /** 楼栋地址 */
    @Size(max = 255, message = "地址不能超过255个字符")
    private String address;

    /** 状态（0正常 1停用） */
    @Pattern(regexp = "[01]", message = "状态值无效")
    private String status;
}
