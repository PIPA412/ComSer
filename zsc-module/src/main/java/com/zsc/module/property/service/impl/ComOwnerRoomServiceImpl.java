package com.zsc.module.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.common.exception.ServiceException;
import com.zsc.module.fee.domain.ComFeeBill;
import com.zsc.module.fee.service.IComFeeBillService;
import com.zsc.module.property.domain.ComOwner;
import com.zsc.module.property.domain.ComOwnerRoom;
import com.zsc.module.property.domain.ComRoom;
import com.zsc.module.property.mapper.ComOwnerRoomMapper;
import com.zsc.module.property.service.IComOwnerService;
import com.zsc.module.property.service.IComOwnerRoomService;
import com.zsc.module.property.service.IComResidenceChangeLogService;
import com.zsc.module.property.service.IComRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 居民房屋居住登记关联 Service 实现
 *
 * @author zsc
 */
@Service
public class ComOwnerRoomServiceImpl extends ServiceImpl<ComOwnerRoomMapper, ComOwnerRoom> implements IComOwnerRoomService {

    @Autowired
    private IComOwnerService ownerService;

    @Autowired
    private IComRoomService roomService;

    @Autowired
    private IComFeeBillService feeBillService;

    @Autowired
    private IComResidenceChangeLogService changeLogService;

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

    @Override
    public List<ComOwnerRoom> getAllRoomsByOwnerId(Long ownerId) {
        LambdaQueryWrapper<ComOwnerRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComOwnerRoom::getOwnerId, ownerId)
               .orderByDesc(ComOwnerRoom::getIsCurrent)
               .orderByDesc(ComOwnerRoom::getCreateTime);
        return list(wrapper);
    }

    @Override
    public boolean hasHouseholder(Long roomId) {
        LambdaQueryWrapper<ComOwnerRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComOwnerRoom::getRoomId, roomId)
               .eq(ComOwnerRoom::getRelationType, "户主")
               .eq(ComOwnerRoom::getIsCurrent, "1");
        return count(wrapper) > 0;
    }

    @Override
    public boolean hasHouseholderExclude(Long roomId, Long excludeOwnerRoomId) {
        LambdaQueryWrapper<ComOwnerRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComOwnerRoom::getRoomId, roomId)
               .eq(ComOwnerRoom::getRelationType, "户主")
               .eq(ComOwnerRoom::getIsCurrent, "1")
               .ne(ComOwnerRoom::getId, excludeOwnerRoomId);
        return count(wrapper) > 0;
    }

    @Override
    public ComOwnerRoom getHouseholderByRoomId(Long roomId) {
        LambdaQueryWrapper<ComOwnerRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComOwnerRoom::getRoomId, roomId)
               .eq(ComOwnerRoom::getRelationType, "户主")
               .eq(ComOwnerRoom::getIsCurrent, "1");
        return getOne(wrapper, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOut(Long ownerRoomId, Date checkOutDate, String operator) {
        // 1. 查询居住登记记录
        ComOwnerRoom ownerRoom = getById(ownerRoomId);
        if (ownerRoom == null) {
            throw new ServiceException("居住登记记录不存在");
        }
        if (!"1".equals(ownerRoom.getIsCurrent())) {
            throw new ServiceException("该记录已搬离，请勿重复操作");
        }

        // 2. 费用结算检查：查询该居民名下所有关联房屋的未结清费用
        List<ComOwnerRoom> allRooms = getAllRoomsByOwnerId(ownerRoom.getOwnerId());
        List<Long> roomIds = allRooms.stream()
                .filter(r -> "1".equals(r.getIsCurrent()))
                .map(ComOwnerRoom::getRoomId)
                .collect(Collectors.toList());
        // 加入当前要搬离的房屋
        if (!roomIds.contains(ownerRoom.getRoomId())) {
            roomIds.add(ownerRoom.getRoomId());
        }

        if (!roomIds.isEmpty()) {
            List<ComFeeBill> unpaidBills = feeBillService.lambdaQuery()
                    .in(ComFeeBill::getRoomId, roomIds)
                    .ne(ComFeeBill::getStatus, "已缴")
                    .list();
            if (!unpaidBills.isEmpty()) {
                StringBuilder sb = new StringBuilder("该居民名下存在未结清费用：\n");
                for (ComFeeBill bill : unpaidBills) {
                    sb.append("账单编号：").append(bill.getBillNo())
                      .append("，金额：").append(bill.getAmount())
                      .append("元，状态：").append(bill.getStatus()).append("\n");
                }
                throw new ServiceException(sb.toString());
            }
        }

        // 3. 记录变更前内容
        String beforeContent = buildOwnerRoomJson(ownerRoom);

        // 4. 执行搬离
        Date actualCheckOutDate = checkOutDate != null ? checkOutDate : new Date();
        ownerRoom.setCheckOutDate(actualCheckOutDate);
        ownerRoom.setIsCurrent("0");
        ownerRoom.setUpdateBy(operator);
        ownerRoom.setUpdateTime(new Date());
        updateById(ownerRoom);

        // 4b. 搬离后检查该居民是否还有其他在住房屋，没有则删除居民
        List<ComOwnerRoom> remainingRooms = lambdaQuery()
                .eq(ComOwnerRoom::getOwnerId, ownerRoom.getOwnerId())
                .eq(ComOwnerRoom::getIsCurrent, "1")
                .list();
        if (remainingRooms.isEmpty()) {
            ownerService.removeById(ownerRoom.getOwnerId());
        }

        // 5. 检查该房屋是否还有在住居民，如无则更新房屋状态
        if (!hasCurrentResidents(ownerRoom.getRoomId())) {
            ComRoom room = roomService.getById(ownerRoom.getRoomId());
            if (room != null) {
                room.setUseStatus("空置");
                room.setUpdateBy(operator);
                roomService.updateById(room);
            }
        }

        // 6. 写入变更记录
        String afterContent = buildOwnerRoomJson(ownerRoom);
        changeLogService.saveChangeLog(
                ownerRoomId, ownerRoom.getOwnerId(), ownerRoom.getRoomId(),
                "搬离", beforeContent, afterContent, operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeRole(Long ownerRoomId, String newRoleType, String operator) {
        // 1. 查询居住登记记录
        ComOwnerRoom ownerRoom = getById(ownerRoomId);
        if (ownerRoom == null) {
            throw new ServiceException("居住登记记录不存在");
        }
        if (!"1".equals(ownerRoom.getIsCurrent())) {
            throw new ServiceException("该记录已搬离，无法变更身份");
        }

        // 2. 校验新身份类型合法性
        if (!"户主".equals(newRoleType) && !"家属".equals(newRoleType) && !"租客".equals(newRoleType)) {
            throw new ServiceException("无效的身份类型，允许值：户主、家属、租客");
        }

        String oldRoleType = ownerRoom.getRelationType();
        // 如果身份未变化，直接返回
        if (newRoleType.equals(oldRoleType)) {
            return;
        }

        // 3. 如果变更为"户主"，检查是否已有户主
        if ("户主".equals(newRoleType) && hasHouseholderExclude(ownerRoom.getRoomId(), ownerRoomId)) {
            ComOwnerRoom existingHouseholder = getHouseholderByRoomId(ownerRoom.getRoomId());
            String householderName = "";
            if (existingHouseholder != null) {
                ComOwner owner = ownerService.getById(existingHouseholder.getOwnerId());
                if (owner != null) {
                    householderName = owner.getOwnerName();
                }
            }
            throw new ServiceException("该门牌号已登记户主[" + householderName + "]，请先办理原户主身份变更或搬离");
        }

        // 4. 记录变更前内容
        String beforeContent = buildOwnerRoomJson(ownerRoom);

        // 5. 执行身份变更
        ownerRoom.setRelationType(newRoleType);
        ownerRoom.setUpdateBy(operator);
        ownerRoom.setUpdateTime(new Date());
        updateById(ownerRoom);

        // 6. 同步更新居民表中的住户类型
        ComOwner owner = ownerService.getById(ownerRoom.getOwnerId());
        if (owner != null) {
            owner.setOwnerType(newRoleType);
            owner.setUpdateBy(operator);
            ownerService.updateById(owner);
        }

        // 7. 写入变更记录
        String afterContent = buildOwnerRoomJson(ownerRoom);
        changeLogService.saveChangeLog(
                ownerRoomId, ownerRoom.getOwnerId(), ownerRoom.getRoomId(),
                "身份变更", beforeContent, afterContent, operator);
    }

    @Override
    public boolean hasCurrentResidents(Long roomId) {
        LambdaQueryWrapper<ComOwnerRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComOwnerRoom::getRoomId, roomId)
               .eq(ComOwnerRoom::getIsCurrent, "1");
        return count(wrapper) > 0;
    }

    /**
     * 构建关联记录的简要JSON描述（用于变更记录）
     */
    private String buildOwnerRoomJson(ComOwnerRoom or) {
        ComOwner owner = ownerService.getById(or.getOwnerId());
        ComRoom room = roomService.getById(or.getRoomId());
        String ownerName = owner != null ? owner.getOwnerName() : "未知";
        String roomNumber = room != null ? room.getRoomNumber() : "未知";
        return "{"
                + "\"ownerName\":\"" + escapeJson(ownerName) + "\","
                + "\"roomNumber\":\"" + escapeJson(roomNumber) + "\","
                + "\"relationType\":\"" + escapeJson(or.getRelationType()) + "\","
                + "\"checkInDate\":\"" + (or.getCheckInDate() != null ? or.getCheckInDate().toString() : "") + "\","
                + "\"checkOutDate\":\"" + (or.getCheckOutDate() != null ? or.getCheckOutDate().toString() : "") + "\","
                + "\"isCurrent\":\"" + or.getIsCurrent() + "\""
                + "}";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
