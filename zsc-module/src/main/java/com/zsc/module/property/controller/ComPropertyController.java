package com.zsc.module.property.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.page.TableDataInfo;
import com.zsc.module.property.domain.*;
import com.zsc.module.property.domain.dto.BuildingCreateDTO;
import com.zsc.module.property.domain.dto.ChangeRoleDTO;
import com.zsc.module.property.domain.dto.CheckOutDTO;
import com.zsc.module.property.domain.dto.OwnerCheckInDTO;
import com.zsc.module.property.service.*;
import com.zsc.common.utils.StringUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 居民与房屋档案管理 Controller
 *
 * @author zsc
 */
@RestController
@RequestMapping("/com/property")
public class ComPropertyController extends BaseController {

    // ==================== 楼栋管理 ====================
    @Autowired
    private IComBuildingService buildingService;

    @PreAuthorize("@ss.hasPermi('com:property:building:list')")
    @GetMapping("/building/list")
    public TableDataInfo buildingList(ComBuilding building) {
        Page<ComBuilding> page = startPage();
        List<ComBuilding> list = buildingService.lambdaQuery()
                .like(building.getBuildingName() != null, ComBuilding::getBuildingName, building.getBuildingName())
                .eq(building.getStatus() != null, ComBuilding::getStatus, building.getStatus())
                .orderByAsc(ComBuilding::getBuildingCode)
                .page(page).getRecords();
        return getDataTable(list);
    }

    /** 获取所有启用楼栋（下拉列表，报修表单等需要，放宽权限） */
    @PreAuthorize("@ss.hasPermi('com:property:building:list') || @ss.hasPermi('com:property:building:query')")
    @GetMapping("/building/all")
    public AjaxResult buildingAll() {
        List<ComBuilding> list = buildingService.lambdaQuery()
                .eq(ComBuilding::getStatus, "0")
                .orderByAsc(ComBuilding::getBuildingCode)
                .list();
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('com:property:building:query')")
    @GetMapping("/building/{buildingId}")
    public AjaxResult buildingGet(@PathVariable Long buildingId) {
        return success(buildingService.getById(buildingId));
    }

    @PreAuthorize("@ss.hasPermi('com:property:building:add')")
    @PostMapping("/building")
    public AjaxResult buildingAdd(@RequestBody @Valid ComBuilding building) {
        building.setCreateBy(getUsername());
        building.setCreateTime(new Date());
        return toAjax(buildingService.save(building));
    }

    /** 一站式创建楼栋：同时创建默认单元和门牌号 */
    @PreAuthorize("@ss.hasPermi('com:property:building:add')")
    @PostMapping("/building/withRooms")
    public AjaxResult buildingCreateWithRooms(@RequestBody @Valid BuildingCreateDTO dto) {
        String username = getUsername();
        // 1. 保存楼栋
        ComBuilding building = new ComBuilding();
        building.setBuildingName(dto.getBuildingName());
        building.setBuildingCode(dto.getBuildingCode());
        building.setFloorCount(dto.getFloorCount());
        building.setAddress(dto.getAddress());
        building.setStatus(StringUtils.defaultString(dto.getStatus(), "0"));
        building.setRemark(dto.getRemark());
        building.setCreateBy(username);
        building.setCreateTime(new Date());
        buildingService.save(building);

        // 2. 创建默认单元
        ComUnit unit = new ComUnit();
        unit.setBuildingId(building.getBuildingId());
        unit.setUnitName(StringUtils.defaultIfBlank(dto.getUnitName(), "1单元"));
        unit.setUnitCode(unit.getUnitName());
        unit.setFloorCount(dto.getFloorCount() != null ? dto.getFloorCount() : 1);
        unit.setStatus("0");
        unit.setCreateBy(username);
        unit.setCreateTime(new Date());
        unitService.save(unit);

        // 3. 解析门牌号并批量创建（支持逗号/换行/中文逗号分隔）
        String[] roomNumbers = dto.getRoomNumbers().split("[,\n，]+");
        int created = 0;
        for (String rn : roomNumbers) {
            String trimmed = rn.trim();
            if (trimmed.isEmpty()) continue;
            ComRoom room = new ComRoom();
            room.setUnitId(unit.getUnitId());
            room.setRoomNumber(trimmed);
            room.setRoomType("住宅");
            room.setUseStatus("空置");
            room.setStatus("0");
            room.setCreateBy(username);
            room.setCreateTime(new Date());
            roomService.save(room);
            created++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("buildingId", building.getBuildingId());
        result.put("unitId", unit.getUnitId());
        result.put("roomCount", created);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('com:property:building:edit')")
    @PutMapping("/building")
    public AjaxResult buildingEdit(@RequestBody @Valid ComBuilding building) {
        building.setUpdateBy(getUsername());
        return toAjax(buildingService.updateById(building));
    }

    @PreAuthorize("@ss.hasPermi('com:property:building:remove')")
    @DeleteMapping("/building/{buildingIds}")
    public AjaxResult buildingRemove(@PathVariable Long[] buildingIds) {
        return toAjax(buildingService.removeByIds(java.util.Arrays.asList(buildingIds)));
    }

    // ==================== 单元管理 ====================
    @Autowired
    private IComUnitService unitService;

    @PreAuthorize("@ss.hasPermi('com:property:unit:query')")
    @GetMapping("/unit/list")
    public TableDataInfo unitList(ComUnit unit) {
        Page<ComUnit> page = startPage();
        List<ComUnit> list = unitService.lambdaQuery()
                .eq(unit.getBuildingId() != null, ComUnit::getBuildingId, unit.getBuildingId())
                .like(unit.getUnitName() != null, ComUnit::getUnitName, unit.getUnitName())
                .eq(unit.getStatus() != null, ComUnit::getStatus, unit.getStatus())
                .orderByAsc(ComUnit::getUnitCode)
                .page(page).getRecords();
        return getDataTable(list);
    }

    /** 根据楼栋获取单元列表（下拉） */
    @PreAuthorize("@ss.hasPermi('com:property:unit:query')")
    @GetMapping("/unit/byBuilding/{buildingId}")
    public AjaxResult unitByBuilding(@PathVariable Long buildingId) {
        List<ComUnit> list = unitService.lambdaQuery()
                .eq(ComUnit::getBuildingId, buildingId)
                .eq(ComUnit::getStatus, "0")
                .orderByAsc(ComUnit::getUnitCode)
                .list();
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('com:property:unit:query')")
    @GetMapping("/unit/{unitId}")
    public AjaxResult unitGet(@PathVariable Long unitId) {
        return success(unitService.getById(unitId));
    }

    @PreAuthorize("@ss.hasPermi('com:property:unit:add')")
    @PostMapping("/unit")
    public AjaxResult unitAdd(@RequestBody @Valid ComUnit unit) {
        unit.setCreateBy(getUsername());
        unit.setCreateTime(new Date());
        return toAjax(unitService.save(unit));
    }

    @PreAuthorize("@ss.hasPermi('com:property:unit:edit')")
    @PutMapping("/unit")
    public AjaxResult unitEdit(@RequestBody @Valid ComUnit unit) {
        unit.setUpdateBy(getUsername());
        return toAjax(unitService.updateById(unit));
    }

    @PreAuthorize("@ss.hasPermi('com:property:unit:remove')")
    @DeleteMapping("/unit/{unitIds}")
    public AjaxResult unitRemove(@PathVariable Long[] unitIds) {
        return toAjax(unitService.removeByIds(java.util.Arrays.asList(unitIds)));
    }

    // ==================== 房屋管理 ====================
    @Autowired
    private IComRoomService roomService;

    @PreAuthorize("@ss.hasPermi('com:property:room:query')")
    @GetMapping("/room/list")
    public TableDataInfo roomList(ComRoom room) {
        Page<ComRoom> page = startPage();
        List<ComRoom> list = roomService.lambdaQuery()
                .eq(room.getUnitId() != null, ComRoom::getUnitId, room.getUnitId())
                .like(room.getRoomNumber() != null, ComRoom::getRoomNumber, room.getRoomNumber())
                .eq(room.getRoomType() != null, ComRoom::getRoomType, room.getRoomType())
                .eq(room.getUseStatus() != null, ComRoom::getUseStatus, room.getUseStatus())
                .eq(room.getStatus() != null, ComRoom::getStatus, room.getStatus())
                .orderByAsc(ComRoom::getRoomNumber)
                .page(page).getRecords();
        return getDataTable(list);
    }

    /** 根据单元获取房屋列表（下拉） */
    @PreAuthorize("@ss.hasPermi('com:property:room:query')")
    @GetMapping("/room/byUnit/{unitId}")
    public AjaxResult roomByUnit(@PathVariable Long unitId) {
        List<ComRoom> list = roomService.lambdaQuery()
                .eq(ComRoom::getUnitId, unitId)
                .eq(ComRoom::getStatus, "0")
                .orderByAsc(ComRoom::getRoomNumber)
                .list();
        return success(list);
    }

    /** 根据楼栋获取门牌号列表（下拉，跳过单元层级） */
    @PreAuthorize("@ss.hasPermi('com:property:room:query')")
    @GetMapping("/room/byBuilding/{buildingId}")
    public AjaxResult roomByBuilding(@PathVariable Long buildingId) {
        // 查到该楼栋下所有单元ID
        List<Long> unitIds = unitService.lambdaQuery()
                .eq(ComUnit::getBuildingId, buildingId)
                .eq(ComUnit::getStatus, "0")
                .list()
                .stream()
                .map(ComUnit::getUnitId)
                .collect(java.util.stream.Collectors.toList());
        if (unitIds.isEmpty()) {
            return success(java.util.Collections.emptyList());
        }
        List<ComRoom> list = roomService.lambdaQuery()
                .in(ComRoom::getUnitId, unitIds)
                .eq(ComRoom::getStatus, "0")
                .orderByAsc(ComRoom::getRoomNumber)
                .list();
        return success(list);
    }

    /** 房屋详情（含居民信息） */
    @PreAuthorize("@ss.hasPermi('com:property:room:query')")
    @GetMapping("/room/detail/{roomId}")
    public AjaxResult roomDetail(@PathVariable Long roomId) {
        ComRoom room = roomService.getById(roomId);
        if (room == null) {
            return error("房屋不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("room", room);
        // 查询关联居民
        List<ComOwnerRoom> ownerRooms = ownerRoomService.getOwnersByRoomId(roomId);
        List<Map<String, Object>> owners = new ArrayList<>();
        for (ComOwnerRoom or : ownerRooms) {
            ComOwner owner = ownerService.getById(or.getOwnerId());
            if (owner != null) {
                Map<String, Object> ownerInfo = new HashMap<>();
                ownerInfo.put("owner", owner);
                ownerInfo.put("relation", or);
                owners.add(ownerInfo);
            }
        }
        result.put("owners", owners);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('com:property:room:query')")
    @GetMapping("/room/{roomId}")
    public AjaxResult roomGet(@PathVariable Long roomId) {
        return success(roomService.getById(roomId));
    }

    /** 查询门牌号当前在住居民 */
    @PreAuthorize("@ss.hasPermi('com:property:room:query')")
    @GetMapping("/room/{roomId}/residents")
    public AjaxResult roomResidents(@PathVariable Long roomId) {
        List<ComOwnerRoom> relations = ownerRoomService.getOwnersByRoomId(roomId);
        List<Map<String, Object>> residents = new ArrayList<>();
        for (ComOwnerRoom or : relations) {
            ComOwner owner = ownerService.getById(or.getOwnerId());
            if (owner != null) {
                Map<String, Object> info = new HashMap<>();
                info.put("ownerName", owner.getOwnerName());
                info.put("phone", owner.getPhone());
                info.put("ownerType", or.getRelationType());
                info.put("checkInDate", or.getCheckInDate());
                residents.add(info);
            }
        }
        return success(residents);
    }

    @PreAuthorize("@ss.hasPermi('com:property:room:add')")
    @PostMapping("/room")
    public AjaxResult roomAdd(@RequestBody @Valid ComRoom room) {
        room.setCreateBy(getUsername());
        room.setCreateTime(new Date());
        return toAjax(roomService.save(room));
    }

    @PreAuthorize("@ss.hasPermi('com:property:room:edit')")
    @PutMapping("/room")
    public AjaxResult roomEdit(@RequestBody @Valid ComRoom room) {
        room.setUpdateBy(getUsername());
        return toAjax(roomService.updateById(room));
    }

    @PreAuthorize("@ss.hasPermi('com:property:room:remove')")
    @DeleteMapping("/room/{roomIds}")
    public AjaxResult roomRemove(@PathVariable Long[] roomIds) {
        return toAjax(roomService.removeByIds(java.util.Arrays.asList(roomIds)));
    }

    // ==================== 居民管理 ====================
    @Autowired
    private IComOwnerService ownerService;

    @PreAuthorize("@ss.hasPermi('com:property:owner:list')")
    @GetMapping("/owner/list")
    public TableDataInfo ownerList(ComOwner owner) {
        Page<ComOwner> page = startPage();
        List<ComOwner> list = ownerService.lambdaQuery()
                .like(owner.getOwnerName() != null, ComOwner::getOwnerName, owner.getOwnerName())
                .eq(owner.getPhone() != null, ComOwner::getPhone, owner.getPhone())
                .eq(owner.getOwnerType() != null, ComOwner::getOwnerType, owner.getOwnerType())
                .eq(owner.getStatus() != null, ComOwner::getStatus, owner.getStatus())
                .orderByDesc(ComOwner::getCreateTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    /** 居民详情（含关联房屋及同住人） */
    @PreAuthorize("@ss.hasPermi('com:property:owner:query')")
    @GetMapping("/owner/detail/{ownerId}")
    public AjaxResult ownerDetail(@PathVariable Long ownerId) {
        ComOwner owner = ownerService.getById(ownerId);
        if (owner == null) {
            return error("居民不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("owner", owner);
        // 查询关联房屋（含已搬离）
        List<ComOwnerRoom> ownerRooms = ownerRoomService.getAllRoomsByOwnerId(ownerId);
        List<Map<String, Object>> rooms = new ArrayList<>();
        for (ComOwnerRoom or : ownerRooms) {
            ComRoom room = roomService.getById(or.getRoomId());
            if (room != null) {
                ComUnit unit = unitService.getById(room.getUnitId());
                ComBuilding building = null;
                if (unit != null) {
                    building = buildingService.getById(unit.getBuildingId());
                }
                Map<String, Object> roomInfo = new HashMap<>();
                roomInfo.put("room", room);
                roomInfo.put("unit", unit);
                roomInfo.put("building", building);
                roomInfo.put("relation", or);

                // 查询该房屋所有在住居民（同住人）
                List<ComOwnerRoom> coResidents = ownerRoomService.getOwnersByRoomId(room.getRoomId());
                List<Map<String, Object>> residents = new ArrayList<>();
                for (ComOwnerRoom cr : coResidents) {
                    ComOwner coOwner = ownerService.getById(cr.getOwnerId());
                    if (coOwner != null) {
                        Map<String, Object> r = new HashMap<>();
                        r.put("ownerId", coOwner.getOwnerId());
                        r.put("ownerName", coOwner.getOwnerName());
                        r.put("phone", coOwner.getPhone());
                        r.put("relationType", cr.getRelationType());
                        r.put("checkInDate", cr.getCheckInDate());
                        r.put("ownerRoomId", cr.getId());
                        r.put("isCurrent", cr.getIsCurrent());
                        residents.add(r);
                    }
                }
                roomInfo.put("residents", residents);
                rooms.add(roomInfo);
            }
        }
        result.put("rooms", rooms);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('com:property:owner:query')")
    @GetMapping("/owner/{ownerId}")
    public AjaxResult ownerGet(@PathVariable Long ownerId) {
        return success(ownerService.getById(ownerId));
    }

    @PreAuthorize("@ss.hasPermi('com:property:owner:add')")
    @PostMapping("/owner")
    public AjaxResult ownerAdd(@RequestBody @Valid ComOwner owner) {
        owner.setCreateBy(getUsername());
        // 校验手机号唯一
        if (owner.getPhone() != null && !owner.getPhone().isEmpty()) {
            ComOwner exist = ownerService.getByPhone(owner.getPhone());
            if (exist != null) {
                return error("该手机号已被使用");
            }
        }
        return toAjax(ownerService.save(owner));
    }

    @PreAuthorize("@ss.hasPermi('com:property:owner:edit')")
    @PutMapping("/owner")
    public AjaxResult ownerEdit(@RequestBody ComOwner owner) {
        owner.setUpdateBy(getUsername());
        // 校验手机号唯一（排除自身）
        if (owner.getPhone() != null && !owner.getPhone().isEmpty()) {
            ComOwner exist = ownerService.getByPhone(owner.getPhone());
            if (exist != null && !exist.getOwnerId().equals(owner.getOwnerId())) {
                return error("该手机号已被使用");
            }
        }
        return toAjax(ownerService.updateById(owner));
    }

    @PreAuthorize("@ss.hasPermi('com:property:owner:remove')")
    @DeleteMapping("/owner/{ownerIds}")
    public AjaxResult ownerRemove(@PathVariable Long[] ownerIds) {
        return toAjax(ownerService.removeByIds(java.util.Arrays.asList(ownerIds)));
    }

    // ==================== 居民房屋关联 ====================
    @Autowired
    private IComOwnerRoomService ownerRoomService;

    @PreAuthorize("@ss.hasPermi('com:property:ownerroom:list')")
    @GetMapping("/ownerroom/list")
    public TableDataInfo ownerRoomList(ComOwnerRoom ownerRoom) {
        Page<ComOwnerRoom> page = startPage();
        List<ComOwnerRoom> list = ownerRoomService.lambdaQuery()
                .eq(ownerRoom.getOwnerId() != null, ComOwnerRoom::getOwnerId, ownerRoom.getOwnerId())
                .eq(ownerRoom.getRoomId() != null, ComOwnerRoom::getRoomId, ownerRoom.getRoomId())
                .eq(ownerRoom.getIsCurrent() != null, ComOwnerRoom::getIsCurrent, ownerRoom.getIsCurrent())
                .orderByDesc(ComOwnerRoom::getCreateTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    /** 绑定居民与房屋 */
    @PreAuthorize("@ss.hasPermi('com:property:ownerroom:add')")
    @PostMapping("/ownerroom/bind")
    public AjaxResult ownerRoomBind(@RequestBody @Valid ComOwnerRoom ownerRoom) {
        ownerRoom.setCreateBy(getUsername());
        ownerRoom.setCreateTime(new Date());
        return toAjax(ownerRoomService.bindOwnerRoom(ownerRoom));
    }

    @PreAuthorize("@ss.hasPermi('com:property:ownerroom:add')")
    @PostMapping("/ownerroom")
    public AjaxResult ownerRoomAdd(@RequestBody @Valid ComOwnerRoom ownerRoom) {
        ownerRoom.setCreateBy(getUsername());
        ownerRoom.setCreateTime(new Date());
        return toAjax(ownerRoomService.save(ownerRoom));
    }

    @PreAuthorize("@ss.hasPermi('com:property:ownerroom:edit')")
    @PutMapping("/ownerroom")
    public AjaxResult ownerRoomEdit(@RequestBody ComOwnerRoom ownerRoom) {
        ownerRoom.setUpdateBy(getUsername());
        return toAjax(ownerRoomService.updateById(ownerRoom));
    }

    @PreAuthorize("@ss.hasPermi('com:property:ownerroom:remove')")
    @DeleteMapping("/ownerroom/{ids}")
    public AjaxResult ownerRoomRemove(@PathVariable Long[] ids) {
        return toAjax(ownerRoomService.removeByIds(java.util.Arrays.asList(ids)));
    }

    // ==================== 入住登记（一站式） ====================

    /** 入住登记：同时创建居民信息并绑定房屋 */
    @PreAuthorize("@ss.hasPermi('com:property:owner:add')")
    @PostMapping("/owner/checkIn")
    public AjaxResult ownerCheckIn(@RequestBody @Valid OwnerCheckInDTO dto) {
        // 1. 校验手机号唯一
        ComOwner existPhone = ownerService.getByPhone(dto.getPhone());
        if (existPhone != null) {
            return error("该手机号已被使用");
        }
        // 2. 校验身份证号唯一（如果提供）
        if (StringUtils.hasText(dto.getIdCard())) {
            ComOwner existIdCard = ownerService.getByIdCard(dto.getIdCard());
            if (existIdCard != null) {
                return error("该身份证号已被使用");
            }
        }
        // 3. 校验房屋存在
        ComRoom room = roomService.getById(dto.getRoomId());
        if (room == null) {
            return error("房屋不存在");
        }

        // 4. 确定身份类型（默认与ownerType相同）
        String relationType = StringUtils.defaultIfBlank(dto.getRelationType(), dto.getOwnerType());

        // 5. 户主唯一性校验：如果是户主，检查该门牌号是否已有户主在住
        if ("户主".equals(relationType)) {
            ComOwnerRoom existingHouseholder = ownerRoomService.getHouseholderByRoomId(dto.getRoomId());
            if (existingHouseholder != null) {
                ComOwner householder = ownerService.getById(existingHouseholder.getOwnerId());
                String name = householder != null ? householder.getOwnerName() : "未知";
                return error("该门牌号已登记户主[" + name + "]，每户仅允许一位户主。如需变更，请先办理原户主身份变更或搬离");
            }
        }

        // 6. 创建居民
        ComOwner owner = new ComOwner();
        owner.setOwnerName(dto.getOwnerName());
        owner.setSex(StringUtils.defaultString(dto.getSex(), "0"));
        // 空字符串转为null，避免uk_id_card唯一索引冲突
        owner.setIdCard(StringUtils.hasText(dto.getIdCard()) ? dto.getIdCard() : null);
        owner.setPhone(dto.getPhone());
        owner.setBackupContact(dto.getBackupContact());
        owner.setOwnerType(relationType);
        owner.setStatus("0");
        owner.setCreateBy(getUsername());
        owner.setCreateTime(new Date());
        ownerService.save(owner);

        // 7. 创建居民-房屋关联
        ComOwnerRoom ownerRoom = new ComOwnerRoom();
        ownerRoom.setOwnerId(owner.getOwnerId());
        ownerRoom.setRoomId(dto.getRoomId());
        ownerRoom.setRelationType(relationType);
        ownerRoom.setCheckInDate(dto.getCheckInDate() != null ? dto.getCheckInDate() : new Date());
        ownerRoom.setIsCurrent("1");
        ownerRoom.setCreateBy(getUsername());
        ownerRoom.setCreateTime(new Date());
        // 使用 save() 而非 bindOwnerRoom()，允许同一房屋有多人同住
        ownerRoomService.save(ownerRoom);

        // 8. 更新房屋使用状态
        if (StringUtils.isBlank(room.getUseStatus()) || "空置".equals(room.getUseStatus())) {
            room.setUseStatus("自住");
            room.setUpdateBy(getUsername());
            roomService.updateById(room);
        }

        // 9. 写入变更记录
        changeLogService.saveChangeLog(
                ownerRoom.getId(), owner.getOwnerId(), dto.getRoomId(),
                "登记入住", null,
                buildOwnerRoomJson(owner, room, relationType, ownerRoom.getCheckInDate()),
                getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("ownerId", owner.getOwnerId());
        result.put("roomId", dto.getRoomId());
        result.put("ownerRoomId", ownerRoom.getId());
        return success(result);
    }

    // ==================== 搬离登记 ====================
    @Autowired
    private IComResidenceChangeLogService changeLogService;

    @PreAuthorize("@ss.hasPermi('com:property:owner:checkOut')")
    @PostMapping("/owner/checkOut")
    public AjaxResult ownerCheckOut(@RequestBody @Valid CheckOutDTO dto) {
        try {
            ownerRoomService.checkOut(dto.getOwnerRoomId(), dto.getCheckOutDate(), getUsername());
            return success("搬离登记成功");
        } catch (com.zsc.common.exception.ServiceException e) {
            return error(e.getMessage());
        }
    }

    // ==================== 身份变更 ====================
    @PreAuthorize("@ss.hasPermi('com:property:owner:changeRole')")
    @PutMapping("/owner/changeRole")
    public AjaxResult ownerChangeRole(@RequestBody @Valid ChangeRoleDTO dto) {
        try {
            ownerRoomService.changeRole(dto.getOwnerRoomId(), dto.getNewRoleType(), getUsername());
            return success("身份变更成功");
        } catch (com.zsc.common.exception.ServiceException e) {
            return error(e.getMessage());
        }
    }

    // ==================== 变更记录查询 ====================
    @PreAuthorize("@ss.hasPermi('com:property:changelog:list')")
    @GetMapping("/changeLog/byOwner/{ownerId}")
    public AjaxResult changeLogByOwner(@PathVariable Long ownerId) {
        return success(changeLogService.getByOwnerId(ownerId));
    }

    @PreAuthorize("@ss.hasPermi('com:property:changelog:list')")
    @GetMapping("/changeLog/byRoom/{roomId}")
    public AjaxResult changeLogByRoom(@PathVariable Long roomId) {
        return success(changeLogService.getByRoomId(roomId));
    }

    @PreAuthorize("@ss.hasPermi('com:property:changelog:list')")
    @GetMapping("/changeLog/list")
    public TableDataInfo changeLogList(ComResidenceChangeLog changeLog) {
        Page<ComResidenceChangeLog> page = startPage();
        List<ComResidenceChangeLog> list = changeLogService.lambdaQuery()
                .eq(changeLog.getOwnerId() != null, ComResidenceChangeLog::getOwnerId, changeLog.getOwnerId())
                .eq(changeLog.getRoomId() != null, ComResidenceChangeLog::getRoomId, changeLog.getRoomId())
                .eq(changeLog.getChangeType() != null && !changeLog.getChangeType().isEmpty(),
                        ComResidenceChangeLog::getChangeType, changeLog.getChangeType())
                .orderByDesc(ComResidenceChangeLog::getCreateTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    // ==================== 档案概览 ====================

    /** 获取楼栋-单元-房屋树形结构 */
    @PreAuthorize("@ss.hasPermi('com:property:building:list')")
    @GetMapping("/tree")
    public AjaxResult propertyTree() {
        List<ComBuilding> buildings = buildingService.lambdaQuery()
                .eq(ComBuilding::getStatus, "0")
                .orderByAsc(ComBuilding::getBuildingCode)
                .list();
        List<Map<String, Object>> tree = new ArrayList<>();
        for (ComBuilding building : buildings) {
            Map<String, Object> bNode = new HashMap<>();
            bNode.put("id", building.getBuildingId());
            bNode.put("label", building.getBuildingName());
            bNode.put("type", "building");
            bNode.put("data", building);
            List<ComUnit> units = unitService.lambdaQuery()
                    .eq(ComUnit::getBuildingId, building.getBuildingId())
                    .eq(ComUnit::getStatus, "0")
                    .orderByAsc(ComUnit::getUnitCode)
                    .list();
            List<Map<String, Object>> uChildren = new ArrayList<>();
            for (ComUnit unit : units) {
                Map<String, Object> uNode = new HashMap<>();
                uNode.put("id", unit.getUnitId());
                uNode.put("label", unit.getUnitName());
                uNode.put("type", "unit");
                uNode.put("data", unit);
                List<ComRoom> rooms = roomService.lambdaQuery()
                        .eq(ComRoom::getUnitId, unit.getUnitId())
                        .eq(ComRoom::getStatus, "0")
                        .orderByAsc(ComRoom::getRoomNumber)
                        .list();
                List<Map<String, Object>> rChildren = new ArrayList<>();
                for (ComRoom room : rooms) {
                    Map<String, Object> rNode = new HashMap<>();
                    rNode.put("id", room.getRoomId());
                    rNode.put("label", room.getRoomNumber());
                    rNode.put("type", "room");
                    rNode.put("data", room);
                    rChildren.add(rNode);
                }
                uNode.put("children", rChildren);
                uChildren.add(uNode);
            }
            bNode.put("children", uChildren);
            tree.add(bNode);
        }
        return success(tree);
    }

    /** 档案统计概览 */
    @PreAuthorize("@ss.hasPermi('com:property:building:list')")
    @GetMapping("/statistics")
    public AjaxResult statistics() {
        Map<String, Object> stats = new HashMap<>();

        // 基础数量
        stats.put("buildingCount", buildingService.count());
        stats.put("unitCount", unitService.count());
        long totalRoomCount = roomService.count();
        stats.put("roomCount", totalRoomCount);
        stats.put("ownerCount", ownerService.count());

        // 人口统计：按居民自身的ownerType统计
        List<ComOwner> allOwners = ownerService.list();
        long householderCount = 0, familyCount = 0, tenantCount = 0;
        for (ComOwner o : allOwners) {
            if ("户主".equals(o.getOwnerType())) householderCount++;
            else if ("家属".equals(o.getOwnerType())) familyCount++;
            else if ("租客".equals(o.getOwnerType())) tenantCount++;
        }
        stats.put("totalPopulation", allOwners.size());
        stats.put("householderCount", householderCount);
        stats.put("familyCount", familyCount);
        stats.put("tenantCount", tenantCount);

        // 房屋统计：基于当前在住关联
        List<ComOwnerRoom> allCurrentRelations = ownerRoomService.lambdaQuery()
                .eq(ComOwnerRoom::getIsCurrent, "1")
                .list();
        // 已入住门牌号（去重）
        Set<Long> occupiedRoomIds = allCurrentRelations.stream()
                .map(ComOwnerRoom::getRoomId)
                .collect(Collectors.toSet());
        long occupiedCount = occupiedRoomIds.size();
        long vacantCount = totalRoomCount - occupiedCount;
        String occupancyRate = totalRoomCount > 0
                ? String.format("%.1f%%", occupiedCount * 100.0 / totalRoomCount)
                : "0.0%";

        stats.put("totalRoomCount", totalRoomCount);
        stats.put("occupiedCount", occupiedCount);
        stats.put("vacantCount", vacantCount);
        stats.put("occupancyRate", occupancyRate);

        // 房屋使用状态统计
        List<ComRoom> allRooms = roomService.list();
        long statusVacant = 0, statusOwnerOccupied = 0, statusRent = 0;
        for (ComRoom r : allRooms) {
            if ("空置".equals(r.getUseStatus())) statusVacant++;
            else if ("自住".equals(r.getUseStatus())) statusOwnerOccupied++;
            else if ("出租".equals(r.getUseStatus())) statusRent++;
        }
        stats.put("useStatusVacant", statusVacant);
        stats.put("useStatusOwnerOccupied", statusOwnerOccupied);
        stats.put("useStatusRent", statusRent);

        return success(stats);
    }

    /** 按楼栋查看入住率排行 */
    @PreAuthorize("@ss.hasPermi('com:property:building:list')")
    @GetMapping("/statistics/occupancyRate")
    public AjaxResult occupancyRateByBuilding() {
        List<ComBuilding> buildings = buildingService.lambdaQuery()
                .eq(ComBuilding::getStatus, "0")
                .list();

        // 获取所有当前在住的关联记录
        List<ComOwnerRoom> allCurrentRelations = ownerRoomService.lambdaQuery()
                .eq(ComOwnerRoom::getIsCurrent, "1")
                .list();
        Set<Long> occupiedRoomIds = allCurrentRelations.stream()
                .map(ComOwnerRoom::getRoomId)
                .collect(Collectors.toSet());

        // 获取所有门牌号（用于按楼栋分组）
        List<ComRoom> allRooms = roomService.list();

        List<Map<String, Object>> ranking = new ArrayList<>();
        for (ComBuilding building : buildings) {
            // 找到该楼栋下所有单元
            List<ComUnit> units = unitService.lambdaQuery()
                    .eq(ComUnit::getBuildingId, building.getBuildingId())
                    .list();
            Set<Long> unitIds = units.stream()
                    .map(ComUnit::getUnitId)
                    .collect(Collectors.toSet());

            // 该楼栋下所有门牌
            List<ComRoom> buildingRooms = allRooms.stream()
                    .filter(r -> unitIds.contains(r.getUnitId()))
                    .collect(Collectors.toList());
            long buildingTotalRooms = buildingRooms.size();

            // 该楼栋下已入住门牌
            long buildingOccupiedRooms = buildingRooms.stream()
                    .filter(r -> occupiedRoomIds.contains(r.getRoomId()))
                    .count();

            double rate = buildingTotalRooms > 0
                    ? buildingOccupiedRooms * 100.0 / buildingTotalRooms
                    : 0.0;

            Map<String, Object> item = new HashMap<>();
            item.put("buildingId", building.getBuildingId());
            item.put("buildingName", building.getBuildingName());
            item.put("totalRooms", buildingTotalRooms);
            item.put("occupiedRooms", buildingOccupiedRooms);
            item.put("vacantRooms", buildingTotalRooms - buildingOccupiedRooms);
            item.put("occupancyRate", String.format("%.1f%%", rate));
            item.put("rateValue", rate);
            ranking.add(item);
        }

        // 按入住率降序排列
        ranking.sort((a, b) -> Double.compare((Double) b.get("rateValue"), (Double) a.get("rateValue")));

        return success(ranking);
    }

    // ==================== 辅助方法 ====================

    /**
     * 构建居民-房屋关联JSON（用于变更记录）
     */
    private String buildOwnerRoomJson(ComOwner owner, ComRoom room, String relationType, Date checkInDate) {
        return "{"
                + "\"ownerName\":\"" + escapeJson(owner.getOwnerName()) + "\","
                + "\"roomNumber\":\"" + escapeJson(room.getRoomNumber()) + "\","
                + "\"relationType\":\"" + escapeJson(relationType) + "\","
                + "\"checkInDate\":\"" + (checkInDate != null ? checkInDate.toString() : "") + "\""
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
