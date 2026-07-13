package com.zsc.module.repair.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.page.TableDataInfo;
import com.zsc.module.repair.domain.*;
import com.zsc.module.repair.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
                .eq(repair.getRepairType() != null, ComRepair::getRepairType, repair.getRepairType())
                .eq(repair.getStatus() != null, ComRepair::getStatus, repair.getStatus())
                .eq(repair.getUserId() != null, ComRepair::getUserId, repair.getUserId())
                .eq(repair.getUrgency() != null, ComRepair::getUrgency, repair.getUrgency())
                .eq(repair.getAssigneeId() != null, ComRepair::getAssigneeId, repair.getAssigneeId())
                .like(repair.getRepairNo() != null, ComRepair::getRepairNo, repair.getRepairNo())
                .orderByDesc(ComRepair::getCreateTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    /** 待受理列表 */
    @PreAuthorize("@ss.hasPermi('com:repair:list')")
    @GetMapping("/pending")
    public TableDataInfo pendingList() {
        Page<ComRepair> page = startPage();
        List<ComRepair> list = repairService.lambdaQuery()
                .eq(ComRepair::getStatus, "待受理")
                .orderByDesc(ComRepair::getCreateTime)
                .page(page).getRecords();
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
        return getDataTable(list);
    }

    /** 我的报修（当前用户报修列表） */
    @GetMapping("/myList")
    public TableDataInfo myList() {
        Long userId = getUserId();
        Page<ComRepair> page = startPage();
        List<ComRepair> list = repairService.lambdaQuery()
                .eq(ComRepair::getUserId, userId)
                .orderByDesc(ComRepair::getCreateTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:repair:query')")
    @GetMapping("/{repairId}")
    public AjaxResult getInfo(@PathVariable Long repairId) {
        ComRepair repair = repairService.getById(repairId);
        if (repair == null) {
            return error("报修单不存在");
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
        // 生成报修编号
        repair.setRepairNo(repairService.generateRepairNo());
        // 默认状态
        if (repair.getStatus() == null) {
            repair.setStatus("待受理");
        }
        return toAjax(repairService.save(repair));
    }

    @PreAuthorize("@ss.hasPermi('com:repair:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody ComRepair repair) {
        repair.setUpdateBy(getUsername());
        return toAjax(repairService.updateById(repair));
    }

    /** 受理/派单 */
    @PreAuthorize("@ss.hasPermi('com:repair:assign')")
    @PutMapping("/assign")
    public AjaxResult assign(@RequestBody Map<String, Long> params) {
        Long repairId = params.get("repairId");
        Long assigneeId = params.get("assigneeId");
        if (repairId == null || assigneeId == null) {
            return error("报修单ID和受理人ID不能为空");
        }
        return toAjax(repairService.acceptRepair(repairId, assigneeId, getUsername()));
    }

    /** 完工 */
    @PreAuthorize("@ss.hasPermi('com:repair:finish')")
    @PutMapping("/finish")
    public AjaxResult finish(@RequestBody Map<String, Object> params) {
        Long repairId = Long.valueOf(params.get("repairId").toString());
        if (repairId == null) {
            return error("报修单ID不能为空");
        }
        // 可选：维修费用和完工说明
        if (params.containsKey("repairFee") || params.containsKey("description")) {
            ComRepairRecord record = new ComRepairRecord();
            record.setRepairId(repairId);
            if (params.containsKey("repairFee")) {
                record.setRepairFee(new java.math.BigDecimal(params.get("repairFee").toString()));
            }
            if (params.containsKey("description")) {
                record.setDescription(params.get("description").toString());
            }
            if (params.containsKey("proofUrls")) {
                record.setProofUrls(params.get("proofUrls").toString());
            }
            record.setActionType("完工");
            record.setCreateBy(getUsername());
            repairRecordService.save(record);
        }
        return toAjax(repairService.finishRepair(repairId, getUsername()));
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
        return toAjax(repairService.rateRepair(repairId, rating, feedback, getUsername()));
    }

    /** 取消报修 */
    @PreAuthorize("@ss.hasPermi('com:repair:cancel')")
    @PutMapping("/cancel/{repairId}")
    public AjaxResult cancel(@PathVariable Long repairId) {
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
        stats.put("pendingCount", repairService.lambdaQuery().eq(ComRepair::getStatus, "待受理").count());
        stats.put("processingCount", repairService.lambdaQuery().eq(ComRepair::getStatus, "处理中").count());
        stats.put("finishedCount", repairService.lambdaQuery().eq(ComRepair::getStatus, "已完成").count());
        stats.put("ratedCount", repairService.lambdaQuery().eq(ComRepair::getStatus, "已评价").count());
        stats.put("canceledCount", repairService.lambdaQuery().eq(ComRepair::getStatus, "已取消").count());
        // 按类型统计
        List<ComRepair> allRepairs = repairService.list();
        Map<String, Long> typeStats = new HashMap<>();
        for (ComRepair r : allRepairs) {
            String type = r.getRepairType() != null ? r.getRepairType() : "其他";
            typeStats.merge(type, 1L, Long::sum);
        }
        stats.put("typeStats", typeStats);
        return success(stats);
    }
}
