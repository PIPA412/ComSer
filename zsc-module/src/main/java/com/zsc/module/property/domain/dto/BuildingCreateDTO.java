package com.zsc.module.property.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 楼栋创建 DTO（含单元和门牌号信息）
 *
 * @author zsc
 */
@Data
public class BuildingCreateDTO {

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

    /** 状态 */
    private String status;

    /** 备注 */
    private String remark;

    /** 单元名称（默认"1单元"） */
    private String unitName;

    /** 门牌号列表，逗号或换行分隔，如 "101,102,103" 或 "101\n102\n103" */
    @NotBlank(message = "门牌号不能为空，请输入门牌号列表")
    private String roomNumbers;
}
