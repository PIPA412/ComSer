package com.zsc.module.announcement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.announcement.domain.ComAnnouncementRead;
import com.zsc.module.announcement.mapper.ComAnnouncementReadMapper;
import com.zsc.module.announcement.service.IComAnnouncementReadService;
import org.springframework.stereotype.Service;

@Service
public class ComAnnouncementReadServiceImpl extends ServiceImpl<ComAnnouncementReadMapper, ComAnnouncementRead> implements IComAnnouncementReadService {
}
