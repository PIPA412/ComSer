package com.zsc.module.property.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 居民信息 com_owner
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_owner")
public class ComOwner extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long ownerId;

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

    /** 住户类型（户主/家属/租客） */
    @NotBlank(message = "住户类型不能为空")
    @Size(max = 20, message = "住户类型不能超过20个字符")
    private String ownerType;

    /** 状态（0正常 1停用） */
    @Pattern(regexp = "[01]", message = "状态值无效")
    private String status;
}
