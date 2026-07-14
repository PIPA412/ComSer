package com.zsc.module.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.common.exception.ServiceException;
import com.zsc.module.property.domain.ComRoom;
import com.zsc.module.property.domain.ComUnit;
import com.zsc.module.property.mapper.ComRoomMapper;
import com.zsc.module.property.mapper.ComUnitMapper;
import com.zsc.module.property.service.IComUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ComUnitServiceImpl extends ServiceImpl<ComUnitMapper, ComUnit> implements IComUnitService {

    @Autowired
    private ComRoomMapper roomMapper;

    @Override
    public boolean removeByIds(Collection<?> list) {
        for (Object id : list) {
            Long unitId = (Long) id;
            Long count = roomMapper.selectCount(
                    new LambdaQueryWrapper<ComRoom>().eq(ComRoom::getUnitId, unitId));
            if (count > 0) {
                throw new ServiceException("该单元下存在房屋，请先删除房屋");
            }
        }
        return super.removeByIds(list);
    }
}
