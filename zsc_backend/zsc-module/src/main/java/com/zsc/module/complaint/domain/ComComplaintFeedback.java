package com.zsc.module.complaint.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 投诉处理反馈 com_complaint_feedback
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_complaint_feedback")
public class ComComplaintFeedback extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long feedbackId;

    /** 投诉ID */
    private Long complaintId;

    /** 处理人ID */
    private Long handlerId;

    /** 处理说明 */
    private String description;

    /** 附件图片 */
    private String attachmentUrls;
}
