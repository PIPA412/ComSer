package com.zsc.module.property.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 业主房屋关联 com_owner_room
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_owner_room")
public class ComOwnerRoom extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /** 业主ID */
    @NotNull(message = "业主ID不能为空")
    private Long ownerId;

    /** 房屋ID */
    @NotNull(message = "房屋ID不能为空")
    private Long roomId;

    /** 关联类型（产权人/租户/家属） */
    @Size(max = 20, message = "关联类型不能超过20个字符")
    private String relationType;

    /** 入住日期 */
    private Date checkInDate;

    /** 搬出日期 */
    private Date checkOutDate;

    /** 是否当前有效 */
    @Pattern(regexp = "[01]", message = "是否当前有效值无效")
    private String isCurrent;
}
