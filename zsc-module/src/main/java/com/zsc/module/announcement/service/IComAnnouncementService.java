package com.zsc.module.announcement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsc.module.announcement.domain.ComAnnouncement;

public interface IComAnnouncementService extends IService<ComAnnouncement> {

    /**
     * 保存公告（含业务校验）
     *
     * @param announcement 公告实体
     * @param operator     操作人
     * @return 校验错误信息，null 表示校验通过
     */
    String validate(ComAnnouncement announcement);
}
