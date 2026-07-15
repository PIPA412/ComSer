package com.zsc.module.fee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.fee.domain.ComFeeBill;
import com.zsc.module.fee.mapper.ComFeeBillMapper;
import com.zsc.module.fee.service.IComFeeBillService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ComFeeBillServiceImpl extends ServiceImpl<ComFeeBillMapper, ComFeeBill> implements IComFeeBillService {
    @Override
    public Map<String, Object> getStatistics(String period) {
        Map<String, Object> result = new HashMap<>();
        // 若依框架可以通过 MyBatis Plus 配合 QueryWrapper 实现统计
        // 或者直接写 Mapper XML 查询
        // 这里先返回空统计
        result.put("totalBills", 0);
        result.put("paidAmount", 0);
        result.put("unpaidAmount", 0);
        result.put("collectionRate", "0%");
        return result;
    }

}
