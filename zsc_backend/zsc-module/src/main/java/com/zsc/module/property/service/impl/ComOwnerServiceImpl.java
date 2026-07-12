package com.zsc.module.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.common.exception.ServiceException;
import com.zsc.module.property.domain.ComOwner;
import com.zsc.module.property.domain.ComOwnerRoom;
import com.zsc.module.property.mapper.ComOwnerMapper;
import com.zsc.module.property.mapper.ComOwnerRoomMapper;
import com.zsc.module.property.service.IComOwnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ComOwnerServiceImpl extends ServiceImpl<ComOwnerMapper, ComOwner> implements IComOwnerService {

    @Autowired
    private ComOwnerRoomMapper ownerRoomMapper;

    @Override
    public ComOwner getByPhone(String phone) {
        LambdaQueryWrapper<ComOwner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComOwner::getPhone, phone);
        return getOne(wrapper);
    }

    @Override
    public ComOwner getByIdCard(String idCard) {
        LambdaQueryWrapper<ComOwner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComOwner::getIdCard, idCard);
        return getOne(wrapper);
    }

    @Override
    public boolean removeByIds(Collection<?> list) {
        for (Object id : list) {
            Long ownerId = (Long) id;
            Long count = ownerRoomMapper.selectCount(
                    new LambdaQueryWrapper<ComOwnerRoom>().eq(ComOwnerRoom::getOwnerId, ownerId));
            if (count > 0) {
                throw new ServiceException("该住户已关联房屋，请先解绑房屋关系");
            }
        }
        return super.removeByIds(list);
    }
}
