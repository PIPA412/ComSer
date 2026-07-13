package com.zsc.module.announcement.controller;

import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.module.property.domain.*;
import com.zsc.module.property.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 信息发布 - 推送范围查询接口
 *
 * 提供楼栋/单元层级结构及业主信息查询，用于公告精确推送范围选择。
 * 遵循开闭原则：新增独立 Controller，不修改现有房产管理或公告管理 Controller。
 *
 * @author zsc
 */
@RestController
@RequestMapping("/com/announcement/scope")
public class ComAnnouncementScopeController extends BaseController {

    @Autowired
    private IComBuildingService buildingService;

    @Autowired
    private IComUnitService unitService;

    @Autowired
    private IComRoomService roomService;

    @Autowired
    private IComOwnerService ownerService;

    @Autowired
    private IComOwnerRoomService ownerRoomService;

    /**
     * 获取可用于推送范围选择的楼栋-单元层级结构
     * 返回所有启用楼栋及其下启用单元，用于前端级联选择器
     */
    @PreAuthorize("@ss.hasAnyPermi('com:announcement:add,com:announcement:edit')")
    @GetMapping("/buildings")
    public AjaxResult getPushScopeBuildings() {
        List<ComBuilding> buildings = buildingService.lambdaQuery()
                .eq(ComBuilding::getStatus, "0")
                .orderByAsc(ComBuilding::getBuildingCode)
                .list();

        List<Map<String, Object>> result = new ArrayList<>();
        for (ComBuilding building : buildings) {
            Map<String, Object> bNode = new HashMap<>();
            bNode.put("buildingId", building.getBuildingId());
            bNode.put("buildingName", building.getBuildingName());
            bNode.put("buildingCode", building.getBuildingCode());
            bNode.put("floorCount", building.getFloorCount());
            bNode.put("address", building.getAddress());

            // 查询该楼栋下启用单元
            List<ComUnit> units = unitService.lambdaQuery()
                    .eq(ComUnit::getBuildingId, building.getBuildingId())
                    .eq(ComUnit::getStatus, "0")
                    .orderByAsc(ComUnit::getUnitCode)
                    .list();

            List<Map<String, Object>> unitList = new ArrayList<>();
            for (ComUnit unit : units) {
                Map<String, Object> uNode = new HashMap<>();
                uNode.put("unitId", unit.getUnitId());
                uNode.put("unitName", unit.getUnitName());
                uNode.put("unitCode", unit.getUnitCode());
                uNode.put("floorCount", unit.getFloorCount());
                unitList.add(uNode);
            }
            bNode.put("units", unitList);
            result.add(bNode);
        }
        return success(result);
    }

    /**
     * 根据楼栋ID查询可用于推送的目标业主/住户列表
     *
     * @param buildingIds 楼栋ID列表（逗号分隔，可选）
     * @param ownerType   住户类型过滤（业主/租户/家属，可选）
     * @return 业主列表及其关联的楼栋-单元-房间信息
     */
    @PreAuthorize("@ss.hasAnyPermi('com:announcement:add,com:announcement:edit')")
    @GetMapping("/owners")
    public AjaxResult getPushTargetOwners(
            @RequestParam(required = false) String buildingIds,
            @RequestParam(required = false) String ownerType) {

        // 解析楼栋ID列表
        Set<Long> buildingIdSet = null;
        if (buildingIds != null && !buildingIds.isEmpty()) {
            buildingIdSet = Arrays.stream(buildingIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toSet());
        }

        // 查询所有启用的业主/住户
        List<ComOwner> owners = ownerService.lambdaQuery()
                .eq(ComOwner::getStatus, "0")
                .eq(ownerType != null && !ownerType.isEmpty(), ComOwner::getOwnerType, ownerType)
                .orderByAsc(ComOwner::getOwnerName)
                .list();

        List<Map<String, Object>> result = new ArrayList<>();
        for (ComOwner owner : owners) {
            // 查询该业主关联的当前有效房屋
            List<ComOwnerRoom> ownerRooms = ownerRoomService.getRoomsByOwnerId(owner.getOwnerId());
            List<Map<String, Object>> roomInfos = new ArrayList<>();

            for (ComOwnerRoom or : ownerRooms) {
                if (!"1".equals(or.getIsCurrent())) continue;

                ComRoom room = roomService.getById(or.getRoomId());
                if (room == null || !"0".equals(room.getStatus())) continue;

                ComUnit unit = unitService.getById(room.getUnitId());
                if (unit == null || !"0".equals(unit.getStatus())) continue;

                // 如果指定了楼栋过滤，则只保留匹配的
                if (buildingIdSet != null && !buildingIdSet.contains(unit.getBuildingId())) continue;

                ComBuilding building = buildingService.getById(unit.getBuildingId());
                if (building == null || !"0".equals(building.getStatus())) continue;

                Map<String, Object> roomInfo = new HashMap<>();
                roomInfo.put("roomId", room.getRoomId());
                roomInfo.put("roomNumber", room.getRoomNumber());
                roomInfo.put("roomType", room.getRoomType());
                roomInfo.put("useStatus", room.getUseStatus());
                roomInfo.put("unitId", unit.getUnitId());
                roomInfo.put("unitName", unit.getUnitName());
                roomInfo.put("buildingId", building.getBuildingId());
                roomInfo.put("buildingName", building.getBuildingName());
                roomInfo.put("relationType", or.getRelationType());
                roomInfos.add(roomInfo);
            }

            if (!roomInfos.isEmpty()) {
                Map<String, Object> ownerInfo = new HashMap<>();
                ownerInfo.put("ownerId", owner.getOwnerId());
                ownerInfo.put("ownerName", owner.getOwnerName());
                ownerInfo.put("phone", owner.getPhone());
                ownerInfo.put("ownerType", owner.getOwnerType());
                ownerInfo.put("rooms", roomInfos);
                result.add(ownerInfo);
            }
        }
        return success(result);
    }

    /**
     * 获取推送范围统计概览
     * 返回各楼栋的住户数量统计，用于推送时评估受众规模
     */
    @PreAuthorize("@ss.hasAnyPermi('com:announcement:add,com:announcement:edit')")
    @GetMapping("/statistics")
    public AjaxResult getPushStatistics() {
        List<ComBuilding> buildings = buildingService.lambdaQuery()
                .eq(ComBuilding::getStatus, "0")
                .orderByAsc(ComBuilding::getBuildingCode)
                .list();

        List<Map<String, Object>> stats = new ArrayList<>();
        for (ComBuilding building : buildings) {
            // 查询该楼栋下的单元
            List<ComUnit> units = unitService.lambdaQuery()
                    .eq(ComUnit::getBuildingId, building.getBuildingId())
                    .eq(ComUnit::getStatus, "0")
                    .list();

            Set<Long> unitIds = units.stream().map(ComUnit::getUnitId).collect(Collectors.toSet());
            if (unitIds.isEmpty()) continue;

            // 查询单元下的房屋
            List<ComRoom> rooms = roomService.lambdaQuery()
                    .in(ComRoom::getUnitId, unitIds)
                    .eq(ComRoom::getStatus, "0")
                    .list();

            Set<Long> roomIds = rooms.stream().map(ComRoom::getRoomId).collect(Collectors.toSet());
            if (roomIds.isEmpty()) continue;

            // 查询关联的业主（去重计数）
            Set<Long> ownerIds = new HashSet<>();
            for (Long roomId : roomIds) {
                List<ComOwnerRoom> ownerRooms = ownerRoomService.getOwnersByRoomId(roomId);
                for (ComOwnerRoom or : ownerRooms) {
                    if ("1".equals(or.getIsCurrent())) {
                        ComOwner owner = ownerService.getById(or.getOwnerId());
                        if (owner != null && "0".equals(owner.getStatus())) {
                            ownerIds.add(owner.getOwnerId());
                        }
                    }
                }
            }

            Map<String, Object> buildingStats = new HashMap<>();
            buildingStats.put("buildingId", building.getBuildingId());
            buildingStats.put("buildingName", building.getBuildingName());
            buildingStats.put("unitCount", units.size());
            buildingStats.put("roomCount", rooms.size());
            buildingStats.put("ownerCount", ownerIds.size());
            stats.add(buildingStats);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("buildings", stats);
        result.put("totalBuildings", stats.size());
        result.put("totalOwners", stats.stream().mapToInt(s -> (int) s.get("ownerCount")).sum());
        return success(result);
    }
}
