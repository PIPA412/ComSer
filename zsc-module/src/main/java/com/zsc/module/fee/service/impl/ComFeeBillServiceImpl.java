package com.zsc.module.fee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.fee.domain.ComFeeBill;
import com.zsc.module.fee.mapper.ComFeeBillMapper;
import com.zsc.module.fee.service.IComFeeBillService;
import org.springframework.stereotype.Service;

@Service
public class ComFeeBillServiceImpl extends ServiceImpl<ComFeeBillMapper, ComFeeBill> implements IComFeeBillService {
}
