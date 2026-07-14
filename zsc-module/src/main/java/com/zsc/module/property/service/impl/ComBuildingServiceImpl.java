package com.zsc.module.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.property.domain.ComBuilding;
import com.zsc.module.property.domain.ComRoom;
import com.zsc.module.property.domain.ComUnit;
import com.zsc.module.property.mapper.ComBuildingMapper;
import com.zsc.module.property.mapper.ComRoomMapper;
import com.zsc.module.property.mapper.ComUnitMapper;
import com.zsc.module.property.service.IComBuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
public class ComBuildingServiceImpl extends ServiceImpl<ComBuildingMapper, ComBuilding> implements IComBuildingService {

    @Autowired
    private ComUnitMapper unitMapper;

    @Autowired
    private ComRoomMapper roomMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<?> list) {
        for (Object id : list) {
            Long buildingId = (Long) id;
            // 找到该楼栋下所有单元
            var units = unitMapper.selectList(
                    new LambdaQueryWrapper<ComUnit>().eq(ComUnit::getBuildingId, buildingId));
            for (ComUnit unit : units) {
                // 删除单元下所有房屋
                roomMapper.delete(new LambdaQueryWrapper<ComRoom>().eq(ComRoom::getUnitId, unit.getUnitId()));
            }
            // 删除所有单元
            unitMapper.delete(new LambdaQueryWrapper<ComUnit>().eq(ComUnit::getBuildingId, buildingId));
        }
        return super.removeByIds(list);
    }
}
