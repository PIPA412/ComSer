package com.zsc.module.parking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.parking.domain.ComParkingSpot;
import com.zsc.module.parking.mapper.ComParkingSpotMapper;
import com.zsc.module.parking.service.IComParkingSpotService;
import org.springframework.stereotype.Service;

@Service
public class ComParkingSpotServiceImpl extends ServiceImpl<ComParkingSpotMapper, ComParkingSpot> implements IComParkingSpotService {
}
