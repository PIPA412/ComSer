package com.zsc.module.property.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 居住变更记录 com_residence_change_log
 *
 * @author zsc
 */
@Data
@TableName("com_residence_change_log")
public class ComResidenceChangeLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long logId;

    /** 关联的居住登记ID */
    private Long ownerRoomId;

    /** 居民ID */
    private Long ownerId;

    /** 房屋ID */
    private Long roomId;

    /** 变更类型（登记入住/搬离/身份变更） */
    private String changeType;

    /** 变更前内容（JSON） */
    private String beforeContent;

    /** 变更后内容（JSON） */
    private String afterContent;

    /** 操作人 */
    private String createBy;

    /** 操作时间 */
    private Date createTime;
}
