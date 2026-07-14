package com.zsc.module.property.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

/**
 * 住户入住登记 DTO
 * 一次性创建住户信息并绑定房屋
 *
 * @author zsc
 */
@Data
public class OwnerCheckInDTO {

    /** 姓名 */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名不能超过50个字符")
    private String ownerName;

    /** 性别（0男 1女 2未知） */
    @Pattern(regexp = "[012]", message = "性别值无效")
    private String sex;

    /** 身份证号 */
    @Pattern(regexp = "^\\d{17}[\\dXx]$", message = "身份证号格式不正确")
    private String idCard;

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 备用联系方式 */
    @Size(max = 100, message = "备用联系方式不能超过100个字符")
    private String backupContact;

    /** 住户类型（业主/租户/家属） */
    @NotBlank(message = "住户类型不能为空")
    private String ownerType;

    /** 绑定房屋ID */
    @NotNull(message = "房屋ID不能为空")
    private Long roomId;

    /** 关联类型（产权人/租户/家属），默认与ownerType相同 */
    private String relationType;

    /** 入住日期 */
    private Date checkInDate;
}
