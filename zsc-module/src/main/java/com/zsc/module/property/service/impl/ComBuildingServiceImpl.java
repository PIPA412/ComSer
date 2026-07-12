package com.zsc.module.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.common.exception.ServiceException;
import com.zsc.module.property.domain.ComBuilding;
import com.zsc.module.property.domain.ComUnit;
import com.zsc.module.property.mapper.ComBuildingMapper;
import com.zsc.module.property.mapper.ComUnitMapper;
import com.zsc.module.property.service.IComBuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ComBuildingServiceImpl extends ServiceImpl<ComBuildingMapper, ComBuilding> implements IComBuildingService {

    @Autowired
    private ComUnitMapper unitMapper;

    @Override
    public boolean removeByIds(Collection<?> list) {
        // 检查是否存在关联单元
        for (Object id : list) {
            Long buildingId = (Long) id;
            Long count = unitMapper.selectCount(
                    new LambdaQueryWrapper<ComUnit>().eq(ComUnit::getBuildingId, buildingId));
            if (count > 0) {
                throw new ServiceException("该楼栋下存在单元，请先删除单元");
            }
        }
        return super.removeByIds(list);
    }
}
