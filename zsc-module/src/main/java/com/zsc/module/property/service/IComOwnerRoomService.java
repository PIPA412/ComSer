package com.zsc.module.property.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsc.module.property.domain.ComOwnerRoom;

import java.util.Date;
import java.util.List;

/**
 * 居民房屋居住登记关联 Service
 *
 * @author zsc
 */
public interface IComOwnerRoomService extends IService<ComOwnerRoom> {

    /**
     * 绑定居民与房屋关系
     */
    boolean bindOwnerRoom(ComOwnerRoom ownerRoom);

    /**
     * 根据居民ID查询当前关联房屋
     */
    List<ComOwnerRoom> getRoomsByOwnerId(Long ownerId);

    /**
     * 根据房屋ID查询当前关联居民
     */
    List<ComOwnerRoom> getOwnersByRoomId(Long roomId);

    /**
     * 根据居民ID查询所有关联房屋（含已搬离）
     */
    List<ComOwnerRoom> getAllRoomsByOwnerId(Long ownerId);

    /**
     * 检查门牌号是否已有户主在住
     *
     * @param roomId 房屋ID
     * @return true-已有户主在住, false-无户主
     */
    boolean hasHouseholder(Long roomId);

    /**
     * 检查门牌号是否已有户主在住（排除指定关联记录）
     *
     * @param roomId          房屋ID
     * @param excludeOwnerRoomId 排除的关联记录ID
     * @return true-已有其他户主在住
     */
    boolean hasHouseholderExclude(Long roomId, Long excludeOwnerRoomId);

    /**
     * 获取门牌号当前的户主关联记录
     *
     * @param roomId 房屋ID
     * @return 户主关联记录，无则返回null
     */
    ComOwnerRoom getHouseholderByRoomId(Long roomId);

    /**
     * 搬离登记
     *
     * @param ownerRoomId  居住登记关联ID
     * @param checkOutDate 搬离日期
     * @param operator     操作人
     */
    void checkOut(Long ownerRoomId, Date checkOutDate, String operator);

    /**
     * 身份变更
     *
     * @param ownerRoomId 居住登记关联ID
     * @param newRoleType 新身份类型（户主/家属/租客）
     * @param operator    操作人
     */
    void changeRole(Long ownerRoomId, String newRoleType, String operator);

    /**
     * 检查房屋是否还有在住居民
     *
     * @param roomId 房屋ID
     * @return true-有在住居民
     */
    boolean hasCurrentResidents(Long roomId);
}
