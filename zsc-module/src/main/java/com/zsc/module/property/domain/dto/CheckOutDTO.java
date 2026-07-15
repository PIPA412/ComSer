package com.zsc.module.property.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * 搬离登记 DTO
 *
 * @author zsc
 */
@Data
public class CheckOutDTO {

    /** 居住登记关联ID（com_owner_room.id） */
    @NotNull(message = "居住登记ID不能为空")
    private Long ownerRoomId;

    /** 搬离日期（默认当天） */
    private Date checkOutDate;
}
