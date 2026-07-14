package com.zsc.module.activity.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 活动报名 com_activity_signup
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_activity_signup")
public class ComActivitySignup extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long signupId;

    /** 活动ID */
    private Long activityId;

    /** 报名人ID */
    private Long userId;

    /** 报名状态（待审核/已通过/已拒绝/已报名） */
    private String status;

    /** 拒绝原因 */
    private String rejectReason;

    /** 考勤状态（已签到/已缺席） */
    private String attendStatus;

    /** 签到时间 */
    private Date signinTime;

    /** 签到方式（扫码/手动） */
    private String signinMethod;
}
