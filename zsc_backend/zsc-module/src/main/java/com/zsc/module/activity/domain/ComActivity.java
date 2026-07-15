package com.zsc.module.activity.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 社区活动 com_activity
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_activity")
public class ComActivity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long activityId;

    /** 活动标题 */
    private String title;

    /** 活动内容 */
    private String content;

    /** 活动时间 */
    private Date activityTime;

    /** 活动地点 */
    private String location;

    /** 报名人数上限 */
    private Integer maxParticipants;

    /** 实际报名人数 */
    private Integer actualParticipants;

    /** 报名截止时间 */
    private Date signupDeadline;

    /** 活动类型 */
    private String activityType;

    /** 状态（草稿/报名中/进行中/已结束/已取消） */
    private String status;

    /** 活动回顾 */
    private String review;

    /** 活动照片（JSON数组） */
    private String photos;
}
