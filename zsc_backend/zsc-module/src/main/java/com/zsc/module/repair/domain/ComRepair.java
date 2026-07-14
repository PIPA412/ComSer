package com.zsc.module.repair.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报修单 com_repair
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_repair")
public class ComRepair extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long repairId;

    /** 报修编号 */
    private String repairNo;

    /** 报修人ID */
    private Long userId;

    /** 房屋ID */
    private Long roomId;

    /** 维修类型 */
    private String repairType;

    /** 紧急程度（一般/紧急/非常紧急） */
    private String urgency;

    /** 报修描述 */
    private String description;

    /** 图片/视频地址（JSON数组） */
    private String mediaUrls;

    /** 状态（待受理/处理中/已完成/已取消） */
    private String status;

    /** 受理人ID */
    private Long assigneeId;

    /** 受理时间 */
    private java.util.Date acceptTime;

    /** 完成时间 */
    private java.util.Date finishTime;

    /** 评价（1-5星） */
    private Integer rating;

    /** 评价内容 */
    private String feedback;
}
