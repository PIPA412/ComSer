package com.zsc.module.property.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 身份变更 DTO
 *
 * @author zsc
 */
@Data
public class ChangeRoleDTO {

    /** 居住登记关联ID */
    @NotNull(message = "居住登记ID不能为空")
    private Long ownerRoomId;

    /** 新身份类型（户主/家属/租客） */
    @NotBlank(message = "新身份类型不能为空")
    private String newRoleType;
}
