package com.zsc.module.announcement.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 公告已读记录 com_announcement_read
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_announcement_read")
public class ComAnnouncementRead extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /** 公告ID */
    private Long announcementId;

    /** 用户ID */
    private Long userId;

    /** 阅读时间 */
    private Date readTime;
}
