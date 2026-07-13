package com.zsc.module.announcement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.announcement.domain.ComAnnouncement;
import com.zsc.module.announcement.mapper.ComAnnouncementMapper;
import com.zsc.module.announcement.service.IComAnnouncementService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ComAnnouncementServiceImpl extends ServiceImpl<ComAnnouncementMapper, ComAnnouncement> implements IComAnnouncementService {

    @Override
    public String validate(ComAnnouncement announcement) {
        // 校验有效期不能早于当前时间
        if (announcement.getExpireDate() != null && announcement.getExpireDate().before(new Date())) {
            return "有效期不能早于当前时间";
        }
        // 校验指定楼栋时必须填写目标楼栋
        if ("指定楼栋".equals(announcement.getTargetScope())
                && (announcement.getTargetBuildings() == null || announcement.getTargetBuildings().isEmpty())) {
            return "选择指定楼栋时必须选择目标楼栋";
        }
        // 校验指定人群时必须填写目标人群
        if ("指定人群".equals(announcement.getTargetScope())
                && (announcement.getTargetGroups() == null || announcement.getTargetGroups().isEmpty())) {
            return "选择指定人群时必须选择目标人群";
        }
        return null;
    }
}
