package com.zsc.module.announcement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.announcement.domain.ComAnnouncement;
import com.zsc.module.announcement.mapper.ComAnnouncementMapper;
import com.zsc.module.announcement.service.IComAnnouncementService;
import org.springframework.stereotype.Service;

@Service
public class ComAnnouncementServiceImpl extends ServiceImpl<ComAnnouncementMapper, ComAnnouncement> implements IComAnnouncementService {
}
