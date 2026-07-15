package com.zsc.module.property.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsc.module.property.domain.ComResidenceChangeLog;

import java.util.List;

/**
 * 居住变更记录 Service
 *
 * @author zsc
 */
public interface IComResidenceChangeLogService extends IService<ComResidenceChangeLog> {

    /**
     * 根据居民ID查询变更记录
     */
    List<ComResidenceChangeLog> getByOwnerId(Long ownerId);

    /**
     * 根据房屋ID查询变更记录
     */
    List<ComResidenceChangeLog> getByRoomId(Long roomId);

    /**
     * 写入变更记录
     *
     * @param ownerRoomId  居住登记关联ID
     * @param ownerId      居民ID
     * @param roomId       房屋ID
     * @param changeType   变更类型
     * @param beforeContent 变更前内容（JSON）
     * @param afterContent  变更后内容（JSON）
     * @param createBy     操作人
     */
    void saveChangeLog(Long ownerRoomId, Long ownerId, Long roomId,
                       String changeType, String beforeContent, String afterContent, String createBy);
}
