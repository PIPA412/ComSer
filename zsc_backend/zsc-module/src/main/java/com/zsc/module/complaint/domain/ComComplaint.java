package com.zsc.module.complaint.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * 投诉建议 com_complaint
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_complaint")
public class ComComplaint extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long complaintId;

    /** 类型（投诉/建议） */
    private String type;

    /** 分类（安全/卫生/噪音/设施/其他） */
    private String category;

    /** 紧急程度（普通/紧急） */
    private String urgency;

    /** 提交人ID */
    private Long userId;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 图片（JSON数组） */
    private String images;

    /** 状态（待受理/处理中/已完成） */
    private String status;

    /** 处理人ID */
    private Long handlerId;

    /** 受理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date acceptTime;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    /** 满意度评价（1-5） */
    private Integer rating;
}
