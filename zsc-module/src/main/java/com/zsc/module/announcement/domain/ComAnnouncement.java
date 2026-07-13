package com.zsc.module.announcement.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 社区公告 com_announcement
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_announcement")
public class ComAnnouncement extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long announcementId;

    /** 标题 */
    private String title;

    /** 内容（富文本） */
    private String content;

    /** 分类（物业通知/社区新闻/政策法规/温馨提示/活动募集） */
    private String category;

    /** 发布范围（全部/指定楼栋/指定人群） */
    private String targetScope;

    /** 目标楼栋ID（JSON数组） */
    private String targetBuildings;

    /** 目标人群（业主/租户，逗号分隔） */
    private String targetGroups;

    /** 发布方式（APP推送/短信/微信） */
    private String pushMethod;

    /** 有效期截止 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireDate;

    /** 状态（草稿/已发布/已撤回） */
    private String status;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishTime;
}
