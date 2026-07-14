package com.zsc.module.dashboard.controller;

import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.module.dashboard.service.IDashboardService;
import com.zsc.module.dashboard.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 数据驾驶舱 Controller
 *
 * @author zsc
 */
@RestController
@RequestMapping("/com/dashboard")
public class DashboardController extends BaseController {

    @Autowired
    private IDashboardService dashboardService;

    /** 获取所有面板数据（一次性返回） */
    @PreAuthorize("@ss.hasPermi('com:dashboard:view')")
    @GetMapping("/all")
    public AjaxResult getAll(@RequestParam(required = false) Long buildingId,
                             @RequestParam(required = false, defaultValue = "12months") String timeRange) {
        DashboardVO vo = new DashboardVO();
        vo.setPopulation(dashboardService.getPopulationPanel(buildingId, timeRange));
        vo.setRoom(dashboardService.getRoomPanel(buildingId, timeRange));
        vo.setRepair(dashboardService.getRepairPanel(buildingId, timeRange));
        vo.setFee(dashboardService.getFeePanel(buildingId, timeRange));
        vo.setComplaint(dashboardService.getComplaintPanel(buildingId, timeRange));
        vo.setActivity(dashboardService.getActivityPanel(buildingId, timeRange));
        vo.setBuildingOptions(dashboardService.getBuildingOptions());
        return success(vo);
    }

    @PreAuthorize("@ss.hasPermi('com:dashboard:view')")
    @GetMapping("/population")
    public AjaxResult getPopulation(@RequestParam(required = false) Long buildingId,
                                    @RequestParam(required = false, defaultValue = "12months") String timeRange) {
        return success(dashboardService.getPopulationPanel(buildingId, timeRange));
    }

    @PreAuthorize("@ss.hasPermi('com:dashboard:view')")
    @GetMapping("/room")
    public AjaxResult getRoom(@RequestParam(required = false) Long buildingId,
                              @RequestParam(required = false, defaultValue = "12months") String timeRange) {
        return success(dashboardService.getRoomPanel(buildingId, timeRange));
    }

    @PreAuthorize("@ss.hasPermi('com:dashboard:view')")
    @GetMapping("/repair")
    public AjaxResult getRepair(@RequestParam(required = false) Long buildingId,
                                @RequestParam(required = false, defaultValue = "12months") String timeRange) {
        return success(dashboardService.getRepairPanel(buildingId, timeRange));
    }

    @PreAuthorize("@ss.hasPermi('com:dashboard:view')")
    @GetMapping("/fee")
    public AjaxResult getFee(@RequestParam(required = false) Long buildingId,
                             @RequestParam(required = false, defaultValue = "12months") String timeRange) {
        return success(dashboardService.getFeePanel(buildingId, timeRange));
    }

    @PreAuthorize("@ss.hasPermi('com:dashboard:view')")
    @GetMapping("/complaint")
    public AjaxResult getComplaint(@RequestParam(required = false) Long buildingId,
                                   @RequestParam(required = false, defaultValue = "12months") String timeRange) {
        return success(dashboardService.getComplaintPanel(buildingId, timeRange));
    }

    @PreAuthorize("@ss.hasPermi('com:dashboard:view')")
    @GetMapping("/activity")
    public AjaxResult getActivity(@RequestParam(required = false) Long buildingId,
                                  @RequestParam(required = false, defaultValue = "12months") String timeRange) {
        return success(dashboardService.getActivityPanel(buildingId, timeRange));
    }

    @PreAuthorize("@ss.hasPermi('com:dashboard:view')")
    @GetMapping("/buildings")
    public AjaxResult getBuildings() {
        return success(dashboardService.getBuildingOptions());
    }
}
