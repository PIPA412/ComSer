package com.zsc.module.repair.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 维修记录 com_repair_record
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_repair_record")
public class ComRepairRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long recordId;

    /** 报修单ID */
    private Long repairId;

    /** 维修人员ID */
    private Long workerId;

    /** 操作类型（受理/派单/到场/完工/评价） */
    private String actionType;

    /** 操作描述 */
    private String description;

    /** 完工凭证图片 */
    private String proofUrls;

    /** 维修费用 */
    private java.math.BigDecimal repairFee;
}
