package com.zsc.module.property.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsc.module.property.domain.ComOwnerRoom;

import java.util.List;

public interface IComOwnerRoomService extends IService<ComOwnerRoom> {

    /**
     * 绑定业主与房屋关系
     */
    boolean bindOwnerRoom(ComOwnerRoom ownerRoom);

    /**
     * 根据业主ID查询关联房屋
     */
    List<ComOwnerRoom> getRoomsByOwnerId(Long ownerId);

    /**
     * 根据房屋ID查询关联业主
     */
    List<ComOwnerRoom> getOwnersByRoomId(Long roomId);
}