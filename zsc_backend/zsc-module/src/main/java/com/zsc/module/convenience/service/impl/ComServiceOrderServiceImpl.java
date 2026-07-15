package com.zsc.module.convenience.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.convenience.domain.ComServiceOrder;
import com.zsc.module.convenience.mapper.ComServiceOrderMapper;
import com.zsc.module.convenience.service.IComServiceOrderService;
import org.springframework.stereotype.Service;

@Service
public class ComServiceOrderServiceImpl extends ServiceImpl<ComServiceOrderMapper, ComServiceOrder> implements IComServiceOrderService {
}
