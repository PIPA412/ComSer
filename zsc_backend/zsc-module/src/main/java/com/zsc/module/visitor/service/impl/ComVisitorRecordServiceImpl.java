package com.zsc.module.visitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.visitor.domain.ComVisitorRecord;
import com.zsc.module.visitor.mapper.ComVisitorRecordMapper;
import com.zsc.module.visitor.service.IComVisitorRecordService;
import org.springframework.stereotype.Service;

@Service
public class ComVisitorRecordServiceImpl extends ServiceImpl<ComVisitorRecordMapper, ComVisitorRecord> implements IComVisitorRecordService {
}
