package com.zsc.module.repair.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.repair.domain.ComRepairRecord;
import com.zsc.module.repair.mapper.ComRepairRecordMapper;
import com.zsc.module.repair.service.IComRepairRecordService;
import org.springframework.stereotype.Service;

@Service
public class ComRepairRecordServiceImpl extends ServiceImpl<ComRepairRecordMapper, ComRepairRecord> implements IComRepairRecordService {
}
