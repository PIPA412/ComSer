package com.zsc.module.repair.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.page.TableDataInfo;
import com.zsc.common.utils.SecurityUtils;
import com.zsc.module.repair.domain.*;
import com.zsc.module.repair.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报修管理 Controller
 *
 * @author zsc
 */
@RestController
@RequestMapping("/com/repair")
public class ComRepairController extends BaseController {

    @Autowired
    private IComRepairService repairService;

    @Autowired
    private IComRepairRecordService repairRecordService;

    // ==================== 报修单 ====================
    @PreAuthorize("@ss.hasPermi('com:repair:list')")
    @GetMapping("/list")
    public TableDataInfo list(ComRepair repair) {
        Page<ComRepair> page = startPage();
        List<ComRepair> list = repairService.lambdaQuery()
                .like(repair.getRepairType() != null && !repair.getRepairType().isEmpty(), ComRepair::getRepairType, repair.getRepairType())
                .eq(repair.getStatus() != null && !repair.getStatus().isEmpty(), ComRepair::getStatus, repair.getStatus())
                .eq(repair.getUserId() != null, ComRepair::getUserId, repair.getUserId())
                .eq(repair.getUrgency() != null && !repair.getUrgency().isEmpty(), ComRepair::getUrgency, repair.getUrgency())
                .eq(repair.getAssigneeId() != null, ComRepair::getAssigneeId, repair.getAssigneeId())
                .like(repair.getRepairNo() != null && !repair.getRepairNo().isEmpty(), ComRepair::getRepairNo, repair.getRepairNo())
                .orderByDesc(ComRepair::getCreateTime)
                .page(page).getRecords();
        repairService.populateTimeoutInfo(list);
        return getDataTable(list);
    }

    /** 待处理列表 */
    @PreAuthorize("@ss.hasPermi('com:repair:list')")
    @GetMapping("/pending")
    public TableDataInfo pendingList() {
        Page<ComRepair> page = startPage();
        List<ComRepair> list = repairService.lambdaQuery()
                .eq(ComRepair::getStatus, "待处理")
                .orderByDesc(ComRepair::getCreateTime)
                .page(page).getRecords();
        repairService.populateTimeoutInfo(list);
        return getDataTable(list);
    }

    /** 处理中列表 */
    @PreAuthorize("@ss.hasPermi('com:repair:list')")
    @GetMapping("/processing")
    public TableDataInfo processingList() {
        Page<ComRepair> page = startPage();
        List<ComRepair> list = repairService.lambdaQuery()
                .eq(ComRepair::getStatus, "处理中")
                .orderByDesc(ComRepair::getCreateTime)
                .page(page).getRecords();
        repairService.populateTimeoutInfo(list);
        return getDataTable(list);
    }

    /** 我的报修（当前用户，与list相同的搜索逻辑） */
    @GetMapping("/myList")
    public TableDataInfo myList(ComRepair repair) {
        Long userId = getUserId();
        Page<ComRepair> page = startPage();
        List<ComRepair> list = repairService.lambdaQuery()
                .eq(ComRepair::getUserId, userId)
                .like(repair.getRepairType() != null && !repair.getRepairType().isEmpty(), ComRepair::getRepairType, repair.getRepairType())
                .eq(repair.getStatus() != null && !repair.getStatus().isEmpty(), ComRepair::getStatus, repair.getStatus())
                .eq(repair.getUrgency() != null && !repair.getUrgency().isEmpty(), ComRepair::getUrgency, repair.getUrgency())
                .orderByDesc(ComRepair::getCreateTime)
                .page(page).getRecords();
        repairService.populateTimeoutInfo(list);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:repair:query')")
    @GetMapping("/{repairId}")
    public AjaxResult getInfo(@PathVariable Long repairId) {
        ComRepair repair = repairService.getById(repairId);
        if (repair == null) {
            return error("报修单不存在");
        }
        // 非管理员只能查看自己的报修
        if (!SecurityUtils.hasRole("admin_community") && !SecurityUtils.hasRole("admin") && !repair.getUserId().equals(getUserId())) {
            return error("无权查看该报修单");
        }
        // 查询维修记录
        List<ComRepairRecord> records = repairRecordService.lambdaQuery()
                .eq(ComRepairRecord::getRepairId, repairId)
                .orderByDesc(ComRepairRecord::getCreateTime)
                .list();
        Map<String, Object> result = new HashMap<>();
        result.put("repair", repair);
        result.put("records", records);
        return success(result);
    }

    /** 提交报修 */
    @PreAuthorize("@ss.hasPermi('com:repair:add')")
    @PostMapping
    public AjaxResult add(@RequestBody ComRepair repair) {
        repair.setCreateBy(getUsername());
        // 绑定当前用户
        repair.setUserId(getUserId());
        // 生成报修编号
        repair.setRepairNo(repairService.generateRepairNo());
        // 默认状态
        if (repair.getStatus() == null || repair.getStatus().isEmpty()) {
            repair.setStatus("待处理");
        }
        repair.setCreateTime(new Date());
        return toAjax(repairService.save(repair));
    }

    @PreAuthorize("@ss.hasPermi('com:repair:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody ComRepair repair) {
        repair.setUpdateBy(getUsername());
        return toAjax(repairService.updateById(repair));
    }

    /** 管理员切换状态（待处理↔处理中↔已完成） */
    @PreAuthorize("@ss.hasPermi('com:repair:assign')")
    @PutMapping("/status/{repairId}")
    public AjaxResult updateStatus(@PathVariable Long repairId, @RequestBody Map<String, String> params) {
        String status = params.get("status");
        if (status == null || status.isEmpty()) {
            return error("状态不能为空");
        }
        if (!"待处理".equals(status) && !"处理中".equals(status) && !"已完成".equals(status)) {
            return error("无效的状态值");
        }
        ComRepair existing = repairService.getById(repairId);
        if (existing == null) {
            return error("报修单不存在");
        }
        if (status.equals(existing.getStatus())) {
            return error("状态未改变");
        }
        if ("已完成".equals(existing.getStatus()) || "已取消".equals(existing.getStatus())) {
            return error("已完成/已取消的工单不能修改状态");
        }
        return toAjax(repairService.updateStatus(repairId, status, getUsername()));
    }

    /** 评价 */
    @PreAuthorize("@ss.hasPermi('com:repair:rate')")
    @PutMapping("/rate")
    public AjaxResult rate(@RequestBody Map<String, Object> params) {
        Long repairId = Long.valueOf(params.get("repairId").toString());
        Integer rating = params.get("rating") != null ? Integer.valueOf(params.get("rating").toString()) : null;
        String feedback = params.get("feedback") != null ? params.get("feedback").toString() : null;
        if (repairId == null || rating == null) {
            return error("报修单ID和评价分数不能为空");
        }
        if (rating < 1 || rating > 5) {
            return error("评价分数为1-5星");
        }
        // 非管理员只能评价自己的报修
        if (!SecurityUtils.hasRole("admin_community") && !SecurityUtils.hasRole("admin")) {
            ComRepair repair = repairService.getById(repairId);
            if (repair == null || !repair.getUserId().equals(getUserId())) {
                return error("无权操作该报修单");
            }
        }
        return toAjax(repairService.rateRepair(repairId, rating, feedback, getUsername()));
    }

    /** 取消报修 */
    @PreAuthorize("@ss.hasPermi('com:repair:cancel')")
    @PutMapping("/cancel/{repairId}")
    public AjaxResult cancel(@PathVariable Long repairId) {
        // 非管理员只能取消自己的报修
        if (!SecurityUtils.hasRole("admin_community") && !SecurityUtils.hasRole("admin")) {
            ComRepair repair = repairService.getById(repairId);
            if (repair == null || !repair.getUserId().equals(getUserId())) {
                return error("无权操作该报修单");
            }
        }
        return toAjax(repairService.cancelRepair(repairId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('com:repair:remove')")
    @DeleteMapping("/{repairIds}")
    public AjaxResult remove(@PathVariable Long[] repairIds) {
        return toAjax(repairService.removeByIds(java.util.Arrays.asList(repairIds)));
    }

    // ==================== 维修记录 ====================
    @PreAuthorize("@ss.hasPermi('com:repair:record:list')")
    @GetMapping("/record/list")
    public TableDataInfo recordList(ComRepairRecord record) {
        Page<ComRepairRecord> page = startPage();
        List<ComRepairRecord> list = repairRecordService.lambdaQuery()
                .eq(record.getRepairId() != null, ComRepairRecord::getRepairId, record.getRepairId())
                .eq(record.getWorkerId() != null, ComRepairRecord::getWorkerId, record.getWorkerId())
                .eq(record.getActionType() != null, ComRepairRecord::getActionType, record.getActionType())
                .orderByDesc(ComRepairRecord::getCreateTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:repair:record:add')")
    @PostMapping("/record")
    public AjaxResult recordAdd(@RequestBody ComRepairRecord record) {
        record.setCreateBy(getUsername());
        return toAjax(repairRecordService.save(record));
    }

    /** 报修统计 */
    @PreAuthorize("@ss.hasPermi('com:repair:list')")
    @GetMapping("/statistics")
    public AjaxResult statistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", repairService.count());
        stats.put("pendingCount", repairService.lambdaQuery().eq(ComRepair::getStatus, "待处理").count());
        stats.put("processingCount", repairService.lambdaQuery().eq(ComRepair::getStatus, "处理中").count());
        stats.put("finishedCount", repairService.lambdaQuery().eq(ComRepair::getStatus, "已完成").count());
        // 已评价数 = 已完成且有评价的
        stats.put("ratedCount", repairService.lambdaQuery()
                .eq(ComRepair::getStatus, "已完成").isNotNull(ComRepair::getRating).count());
        stats.put("canceledCount", repairService.lambdaQuery().eq(ComRepair::getStatus, "已取消").count());
        // 按类型统计
        List<ComRepair> allRepairs = repairService.list();
        Map<String, Long> typeStats = new HashMap<>();
        for (ComRepair r : allRepairs) {
            String type = r.getRepairType() != null && !r.getRepairType().isEmpty() ? r.getRepairType() : "其他";
            typeStats.merge(type, 1L, Long::sum);
        }
        stats.put("typeStats", typeStats);
        return success(stats);
    }
}
