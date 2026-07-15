package com.zsc.module.convenience.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.convenience.domain.ComServiceOrder;
import com.zsc.module.convenience.mapper.ComServiceOrderMapper;
import com.zsc.module.convenience.service.IComServiceOrderService;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ComServiceOrderServiceImpl extends ServiceImpl<ComServiceOrderMapper, ComServiceOrder> implements IComServiceOrderService {

    /**
     * 生成订单编号：FW + yyyyMMdd + 4位流水
     */
    private String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String datePart = sdf.format(new Date());
        // 取当前最大订单号的后4位，自增1
        long count = baseMapper.selectCount(null);
        String seq = String.format("%04d", (count % 10000) + 1);
        return "FW" + datePart + seq;
    }

    @Override
    public boolean save(ComServiceOrder entity) {
        if (entity.getOrderNo() == null) {
            entity.setOrderNo(generateOrderNo());
        }
        if (entity.getStatus() == null) {
            entity.setStatus("待接单");
        }
        return super.save(entity);
    }
}
