package com.zsc.module.parking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.parking.domain.ComVehicle;
import com.zsc.module.parking.mapper.ComVehicleMapper;
import com.zsc.module.parking.service.IComVehicleService;
import org.springframework.stereotype.Service;

@Service
public class ComVehicleServiceImpl extends ServiceImpl<ComVehicleMapper, ComVehicle> implements IComVehicleService {
}
