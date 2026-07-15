package com.zsc.module.parking.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.page.TableDataInfo;
import com.zsc.module.parking.domain.*;
import com.zsc.module.parking.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 停车管理 Controller
 *
 * @author zsc
 */
@RestController
@RequestMapping("/com/parking")
public class ComParkingController extends BaseController {

    // ==================== 车位管理 ====================
    @Autowired
    private IComParkingSpotService spotService;

    @PreAuthorize("@ss.hasPermi('com:parking:spot:list')")
    @GetMapping("/spot/list")
    public TableDataInfo spotList(ComParkingSpot spot) {
        Page<ComParkingSpot> page = startPage();
        List<ComParkingSpot> list = spotService.lambdaQuery()
                .like(spot.getSpotCode() != null, ComParkingSpot::getSpotCode, spot.getSpotCode())
                .eq(spot.getSpotType() != null, ComParkingSpot::getSpotType, spot.getSpotType())
                .eq(spot.getStatus() != null, ComParkingSpot::getStatus, spot.getStatus())
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:parking:spot:query')")
    @GetMapping("/spot/{spotId}")
    public AjaxResult spotGet(@PathVariable Long spotId) {
        return success(spotService.getById(spotId));
    }

    @PreAuthorize("@ss.hasPermi('com:parking:spot:add')")
    @PostMapping("/spot")
    public AjaxResult spotAdd(@RequestBody ComParkingSpot spot) {
        spot.setCreateBy(getUsername());
        return toAjax(spotService.save(spot));
    }

    @PreAuthorize("@ss.hasPermi('com:parking:spot:edit')")
    @PutMapping("/spot")
    public AjaxResult spotEdit(@RequestBody ComParkingSpot spot) {
        spot.setUpdateBy(getUsername());
        return toAjax(spotService.updateById(spot));
    }

    @PreAuthorize("@ss.hasPermi('com:parking:spot:remove')")
    @DeleteMapping("/spot/{spotIds}")
    public AjaxResult spotRemove(@PathVariable Long[] spotIds) {
        return toAjax(spotService.removeByIds(java.util.Arrays.asList(spotIds)));
    }

    // ==================== 车辆管理 ====================
    @Autowired
    private IComVehicleService vehicleService;

    @PreAuthorize("@ss.hasPermi('com:parking:vehicle:list')")
    @GetMapping("/vehicle/list")
    public TableDataInfo vehicleList(ComVehicle vehicle) {
        Page<ComVehicle> page = startPage();
        List<ComVehicle> list = vehicleService.lambdaQuery()
                .like(vehicle.getPlateNumber() != null, ComVehicle::getPlateNumber, vehicle.getPlateNumber())
                .eq(vehicle.getOwnerId() != null, ComVehicle::getOwnerId, vehicle.getOwnerId())
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:parking:vehicle:query')")
    @GetMapping("/vehicle/{vehicleId}")
    public AjaxResult vehicleGet(@PathVariable Long vehicleId) {
        return success(vehicleService.getById(vehicleId));
    }

    @PreAuthorize("@ss.hasPermi('com:parking:vehicle:add')")
    @PostMapping("/vehicle")
    public AjaxResult vehicleAdd(@RequestBody ComVehicle vehicle) {
        vehicle.setCreateBy(getUsername());
        return toAjax(vehicleService.save(vehicle));
    }

    @PreAuthorize("@ss.hasPermi('com:parking:vehicle:edit')")
    @PutMapping("/vehicle")
    public AjaxResult vehicleEdit(@RequestBody ComVehicle vehicle) {
        vehicle.setUpdateBy(getUsername());
        return toAjax(vehicleService.updateById(vehicle));
    }

    @PreAuthorize("@ss.hasPermi('com:parking:vehicle:remove')")
    @DeleteMapping("/vehicle/{vehicleIds}")
    public AjaxResult vehicleRemove(@PathVariable Long[] vehicleIds) {
        return toAjax(vehicleService.removeByIds(java.util.Arrays.asList(vehicleIds)));
    }

    // ==================== 停车记录 ====================
    @Autowired
    private IComParkingRecordService recordService;

    @PreAuthorize("@ss.hasPermi('com:parking:record:list')")
    @GetMapping("/record/list")
    public TableDataInfo recordList(ComParkingRecord record) {
        Page<ComParkingRecord> page = startPage();
        List<ComParkingRecord> list = recordService.lambdaQuery()
                .eq(record.getVehicleId() != null, ComParkingRecord::getVehicleId, record.getVehicleId())
                .orderByDesc(ComParkingRecord::getEntryTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:parking:record:add')")
    @PostMapping("/record")
    public AjaxResult recordAdd(@RequestBody ComParkingRecord record) {
        record.setCreateBy(getUsername());
        return toAjax(recordService.save(record));
    }
}
