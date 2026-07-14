package com.zsc.module.property.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.page.TableDataInfo;
import com.zsc.module.property.domain.*;
import com.zsc.module.property.domain.dto.OwnerCheckInDTO;
import com.zsc.module.property.service.*;
import com.zsc.common.utils.StringUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 房产与房屋档案管理 Controller
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

    /** 获取所有启用楼栋（下拉列表） */
    @PreAuthorize("@ss.hasPermi('com:property:building:list')")
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
        return toAjax(buildingService.save(building));
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

    @PreAuthorize("@ss.hasPermi('com:property:unit:list')")
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
    @PreAuthorize("@ss.hasPermi('com:property:unit:list')")
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

    @PreAuthorize("@ss.hasPermi('com:property:room:list')")
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
    @PreAuthorize("@ss.hasPermi('com:property:room:list')")
    @GetMapping("/room/byUnit/{unitId}")
    public AjaxResult roomByUnit(@PathVariable Long unitId) {
        List<ComRoom> list = roomService.lambdaQuery()
                .eq(ComRoom::getUnitId, unitId)
                .eq(ComRoom::getStatus, "0")
                .orderByAsc(ComRoom::getRoomNumber)
                .list();
        return success(list);
    }

    /** 房屋详情（含业主信息） */
    @PreAuthorize("@ss.hasPermi('com:property:room:query')")
    @GetMapping("/room/detail/{roomId}")
    public AjaxResult roomDetail(@PathVariable Long roomId) {
        ComRoom room = roomService.getById(roomId);
        if (room == null) {
            return error("房屋不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("room", room);
        // 查询关联业主
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

    @PreAuthorize("@ss.hasPermi('com:property:room:add')")
    @PostMapping("/room")
    public AjaxResult roomAdd(@RequestBody @Valid ComRoom room) {
        room.setCreateBy(getUsername());
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

    // ==================== 业主管理 ====================
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

    /** 业主详情（含关联房屋） */
    @PreAuthorize("@ss.hasPermi('com:property:owner:query')")
    @GetMapping("/owner/detail/{ownerId}")
    public AjaxResult ownerDetail(@PathVariable Long ownerId) {
        ComOwner owner = ownerService.getById(ownerId);
        if (owner == null) {
            return error("业主不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("owner", owner);
        // 查询关联房屋
        List<ComOwnerRoom> ownerRooms = ownerRoomService.getRoomsByOwnerId(ownerId);
        List<Map<String, Object>> rooms = new ArrayList<>();
        for (ComOwnerRoom or : ownerRooms) {
            ComRoom room = roomService.getById(or.getRoomId());
            if (room != null) {
                // 查询单元和楼栋
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

    // ==================== 业主房屋关联 ====================
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

    /** 绑定业主与房屋 */
    @PreAuthorize("@ss.hasPermi('com:property:ownerroom:add')")
    @PostMapping("/ownerroom/bind")
    public AjaxResult ownerRoomBind(@RequestBody @Valid ComOwnerRoom ownerRoom) {
        ownerRoom.setCreateBy(getUsername());
        return toAjax(ownerRoomService.bindOwnerRoom(ownerRoom));
    }

    @PreAuthorize("@ss.hasPermi('com:property:ownerroom:add')")
    @PostMapping("/ownerroom")
    public AjaxResult ownerRoomAdd(@RequestBody @Valid ComOwnerRoom ownerRoom) {
        ownerRoom.setCreateBy(getUsername());
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

    /** 入住登记：同时创建住户信息并绑定房屋 */
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
        // 4. 创建住户
        ComOwner owner = new ComOwner();
        owner.setOwnerName(dto.getOwnerName());
        owner.setSex(StringUtils.defaultString(dto.getSex(), "0"));
        owner.setIdCard(dto.getIdCard());
        owner.setPhone(dto.getPhone());
        owner.setBackupContact(dto.getBackupContact());
        owner.setOwnerType(dto.getOwnerType());
        owner.setStatus("0");
        owner.setCreateBy(getUsername());
        ownerService.save(owner);

        // 5. 创建住户-房屋关联
        ComOwnerRoom ownerRoom = new ComOwnerRoom();
        ownerRoom.setOwnerId(owner.getOwnerId());
        ownerRoom.setRoomId(dto.getRoomId());
        ownerRoom.setRelationType(StringUtils.defaultIfBlank(dto.getRelationType(), dto.getOwnerType()));
        ownerRoom.setCheckInDate(dto.getCheckInDate() != null ? dto.getCheckInDate() : new java.util.Date());
        ownerRoom.setIsCurrent("1");
        ownerRoom.setCreateBy(getUsername());
        ownerRoomService.bindOwnerRoom(ownerRoom);

        // 6. 更新房屋使用状态（空置→自住）
        if (StringUtils.isBlank(room.getUseStatus()) || "空置".equals(room.getUseStatus())) {
            room.setUseStatus("自住");
            room.setUpdateBy(getUsername());
            roomService.updateById(room);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("ownerId", owner.getOwnerId());
        result.put("roomId", dto.getRoomId());
        return success(result);
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
            // 查询单元
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
                // 查询房屋
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
        // 基础数量（4次count，不可合并）
        stats.put("buildingCount", buildingService.count());
        stats.put("unitCount", unitService.count());
        stats.put("roomCount", roomService.count());
        stats.put("ownerCount", ownerService.count());

        // 房屋使用状态统计（1次list + stream聚合，替代3次count）
        List<ComRoom> allRooms = roomService.list();
        long vacantCount = 0, ownerOccupiedCount = 0, rentCount = 0;
        for (ComRoom r : allRooms) {
            if ("空置".equals(r.getUseStatus())) vacantCount++;
            else if ("自住".equals(r.getUseStatus())) ownerOccupiedCount++;
            else if ("出租".equals(r.getUseStatus())) rentCount++;
        }
        stats.put("vacantCount", vacantCount);
        stats.put("ownerOccupiedCount", ownerOccupiedCount);
        stats.put("rentCount", rentCount);

        // 住户类型统计（1次list + stream聚合，替代3次count）
        List<ComOwner> allOwners = ownerService.list();
        long ownerTypeCount = 0, tenantCount = 0, familyCount = 0;
        for (ComOwner o : allOwners) {
            if ("业主".equals(o.getOwnerType())) ownerTypeCount++;
            else if ("租户".equals(o.getOwnerType())) tenantCount++;
            else if ("家属".equals(o.getOwnerType())) familyCount++;
        }
        stats.put("ownerTypeCount", ownerTypeCount);
        stats.put("tenantCount", tenantCount);
        stats.put("familyCount", familyCount);
        return success(stats);
    }
}
