package com.zsc.module.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.property.domain.ComResidenceChangeLog;
import com.zsc.module.property.mapper.ComResidenceChangeLogMapper;
import com.zsc.module.property.service.IComResidenceChangeLogService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 居住变更记录 Service 实现
 *
 * @author zsc
 */
@Service
public class ComResidenceChangeLogServiceImpl
        extends ServiceImpl<ComResidenceChangeLogMapper, ComResidenceChangeLog>
        implements IComResidenceChangeLogService {

    @Override
    public List<ComResidenceChangeLog> getByOwnerId(Long ownerId) {
        LambdaQueryWrapper<ComResidenceChangeLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComResidenceChangeLog::getOwnerId, ownerId)
               .orderByDesc(ComResidenceChangeLog::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<ComResidenceChangeLog> getByRoomId(Long roomId) {
        LambdaQueryWrapper<ComResidenceChangeLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComResidenceChangeLog::getRoomId, roomId)
               .orderByDesc(ComResidenceChangeLog::getCreateTime);
        return list(wrapper);
    }

    @Override
    public void saveChangeLog(Long ownerRoomId, Long ownerId, Long roomId,
                              String changeType, String beforeContent, String afterContent, String createBy) {
        ComResidenceChangeLog log = new ComResidenceChangeLog();
        log.setOwnerRoomId(ownerRoomId);
        log.setOwnerId(ownerId);
        log.setRoomId(roomId);
        log.setChangeType(changeType);
        log.setBeforeContent(beforeContent);
        log.setAfterContent(afterContent);
        log.setCreateBy(createBy);
        log.setCreateTime(new Date());
        save(log);
    }
}
