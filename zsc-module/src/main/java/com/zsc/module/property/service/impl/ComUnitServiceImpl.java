package com.zsc.module.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.property.domain.ComRoom;
import com.zsc.module.property.domain.ComUnit;
import com.zsc.module.property.mapper.ComRoomMapper;
import com.zsc.module.property.mapper.ComUnitMapper;
import com.zsc.module.property.service.IComUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
public class ComUnitServiceImpl extends ServiceImpl<ComUnitMapper, ComUnit> implements IComUnitService {

    @Autowired
    private ComRoomMapper roomMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<?> list) {
        for (Object id : list) {
            Long unitId = (Long) id;
            // 级联删除该单元下所有房屋
            roomMapper.delete(new LambdaQueryWrapper<ComRoom>().eq(ComRoom::getUnitId, unitId));
        }
        return super.removeByIds(list);
    }
}
