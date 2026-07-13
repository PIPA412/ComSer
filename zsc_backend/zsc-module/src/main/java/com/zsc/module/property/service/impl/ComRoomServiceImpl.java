package com.zsc.module.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.common.exception.ServiceException;
import com.zsc.module.property.domain.ComOwnerRoom;
import com.zsc.module.property.domain.ComRoom;
import com.zsc.module.property.mapper.ComOwnerRoomMapper;
import com.zsc.module.property.mapper.ComRoomMapper;
import com.zsc.module.property.service.IComRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ComRoomServiceImpl extends ServiceImpl<ComRoomMapper, ComRoom> implements IComRoomService {

    @Autowired
    private ComOwnerRoomMapper ownerRoomMapper;

    @Override
    public boolean removeByIds(Collection<?> list) {
        for (Object id : list) {
            Long roomId = (Long) id;
            Long count = ownerRoomMapper.selectCount(
                    new LambdaQueryWrapper<ComOwnerRoom>().eq(ComOwnerRoom::getRoomId, roomId));
            if (count > 0) {
                throw new ServiceException("该房屋已关联住户，请先解绑住户关系");
            }
        }
        return super.removeByIds(list);
    }
}
