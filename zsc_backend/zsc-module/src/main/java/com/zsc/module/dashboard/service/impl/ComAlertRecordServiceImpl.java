package com.zsc.module.dashboard.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.dashboard.domain.ComAlertRecord;
import com.zsc.module.dashboard.mapper.ComAlertRecordMapper;
import com.zsc.module.dashboard.service.IComAlertRecordService;
import org.springframework.stereotype.Service;

@Service
public class ComAlertRecordServiceImpl extends ServiceImpl<ComAlertRecordMapper, ComAlertRecord> implements IComAlertRecordService {
}
