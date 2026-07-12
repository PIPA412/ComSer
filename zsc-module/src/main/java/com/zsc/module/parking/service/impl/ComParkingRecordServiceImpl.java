package com.zsc.module.parking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.parking.domain.ComParkingRecord;
import com.zsc.module.parking.mapper.ComParkingRecordMapper;
import com.zsc.module.parking.service.IComParkingRecordService;
import org.springframework.stereotype.Service;

@Service
public class ComParkingRecordServiceImpl extends ServiceImpl<ComParkingRecordMapper, ComParkingRecord> implements IComParkingRecordService {
}
