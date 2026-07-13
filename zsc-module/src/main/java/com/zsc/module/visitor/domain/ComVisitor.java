package com.zsc.module.visitor.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 访客信息 com_visitor
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_visitor")
public class ComVisitor extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long visitorId;

    /** 邀请人ID */
    private Long inviterId;

    /** 访客姓名 */
    private String visitorName;

    /** 访客手机号 */
    private String visitorPhone;

    /** 被访房屋ID */
    private Long roomId;

    /** 预计来访时间 */
    private Date expectedTime;

    /** 来访事由 */
    private String reason;

    /** 通行二维码 */
    @com.baomidou.mybatisplus.annotation.TableField("qr_code")
    private String qrCode;

    /** 状态（待审批/已通过/已拒绝/已签离） */
    private String status;

    /** 实际到达时间 */
    private Date arrivalTime;

    /** 签离时间 */
    private Date leaveTime;
}
