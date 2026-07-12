package com.zsc.module.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.property.domain.ComOwnerRoom;
import com.zsc.module.property.mapper.ComOwnerRoomMapper;
import com.zsc.module.property.service.IComOwnerRoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ComOwnerRoomServiceImpl extends ServiceImpl<ComOwnerRoomMapper, ComOwnerRoom> implements IComOwnerRoomService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bindOwnerRoom(ComOwnerRoom ownerRoom) {
        // 如果绑定为当前有效，先将该房屋的其他关联设为非当前
        if ("1".equals(ownerRoom.getIsCurrent())) {
            LambdaQueryWrapper<ComOwnerRoom> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ComOwnerRoom::getRoomId, ownerRoom.getRoomId())
                   .eq(ComOwnerRoom::getIsCurrent, "1");
            List<ComOwnerRoom> existList = list(wrapper);
            for (ComOwnerRoom exist : existList) {
                exist.setIsCurrent("0");
                exist.setCheckOutDate(new Date());
                updateById(exist);
            }
        }
        // 默认当前有效
        if (ownerRoom.getIsCurrent() == null) {
            ownerRoom.setIsCurrent("1");
        }
        return save(ownerRoom);
    }

    @Override
    public List<ComOwnerRoom> getRoomsByOwnerId(Long ownerId) {
        LambdaQueryWrapper<ComOwnerRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComOwnerRoom::getOwnerId, ownerId)
               .eq(ComOwnerRoom::getIsCurrent, "1");
        return list(wrapper);
    }

    @Override
    public List<ComOwnerRoom> getOwnersByRoomId(Long roomId) {
        LambdaQueryWrapper<ComOwnerRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComOwnerRoom::getRoomId, roomId)
               .eq(ComOwnerRoom::getIsCurrent, "1");
        return list(wrapper);
    }
}
