package com.zsc.module.fee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsc.module.fee.domain.ComFeeBill;

import java.util.Map;

public interface IComFeeBillService extends IService<ComFeeBill> {
    Map<String, Object> getStatistics(String period);

}
