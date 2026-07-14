package com.zsc.module.complaint.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.page.TableDataInfo;
import com.zsc.module.complaint.domain.*;
import com.zsc.module.complaint.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 投诉与建议管理 Controller
 *
 * @author zsc
 */
@RestController
@RequestMapping("/com/complaint")
public class ComComplaintController extends BaseController {

    @Autowired
    private IComComplaintService complaintService;

    @Autowired
    private IComComplaintFeedbackService feedbackService;

    // ==================== 投诉/建议 ====================
    @PreAuthorize("@ss.hasPermi('com:complaint:list')")
    @GetMapping("/list")
    public TableDataInfo list(ComComplaint complaint) {
        Page<ComComplaint> page = startPage();
        List<ComComplaint> list = complaintService.lambdaQuery()
                .eq(complaint.getType() != null, ComComplaint::getType, complaint.getType())
                .eq(complaint.getCategory() != null, ComComplaint::getCategory, complaint.getCategory())
                .eq(complaint.getUrgency() != null, ComComplaint::getUrgency, complaint.getUrgency())
                .eq(complaint.getStatus() != null, ComComplaint::getStatus, complaint.getStatus())
                .like(complaint.getTitle() != null, ComComplaint::getTitle, complaint.getTitle())
                .eq(complaint.getUserId() != null, ComComplaint::getUserId, complaint.getUserId())
                .orderByDesc(ComComplaint::getUrgency)
                .orderByDesc(ComComplaint::getCreateTime)
                .page(page).getRecords();
        // 计算预警
        list.forEach(this::calcWarn);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:complaint:query')")
    @GetMapping("/{complaintId}")
    public AjaxResult getInfo(@PathVariable Long complaintId) {
        return success(complaintService.getById(complaintId));
    }

    @PreAuthorize("@ss.hasPermi('com:complaint:add')")
    @PostMapping
    public AjaxResult add(@RequestBody ComComplaint complaint) {
        complaint.setCreateBy(getUsername());
        complaint.setUserId(getUserId());
        complaint.setCreateTime(new java.util.Date());
        complaint.setStatus("待受理");
        return toAjax(complaintService.save(complaint));
    }

    /** 查询当前用户的投诉/建议列表（居民端） */
    @GetMapping("/my")
    public TableDataInfo myList(ComComplaint complaint) {
        Page<ComComplaint> page = startPage();
        List<ComComplaint> list = complaintService.lambdaQuery()
                .eq(complaint.getType() != null, ComComplaint::getType, complaint.getType())
                .eq(ComComplaint::getUserId, getUserId())
                .orderByDesc(ComComplaint::getCreateTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    /** 受理 */
    @PreAuthorize("@ss.hasPermi('com:complaint:accept')")
    @PutMapping("/accept")
    public AjaxResult accept(@RequestBody ComComplaint complaint) {
        complaint.setUpdateBy(getUsername());
        complaint.setHandlerId(getUserId());
        complaint.setStatus("处理中");
        complaint.setAcceptTime(new java.util.Date());
        // 48小时处理时限
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 48);
        complaint.setDeadline(cal.getTime());
        return toAjax(complaintService.updateById(complaint));
    }

    /** 完成 */
    @PreAuthorize("@ss.hasPermi('com:complaint:finish')")
    @PutMapping("/finish")
    public AjaxResult finish(@RequestBody ComComplaint complaint) {
        complaint.setUpdateBy(getUsername());
        complaint.setStatus("已完成");
        complaint.setFinishTime(new java.util.Date());
        return toAjax(complaintService.updateById(complaint));
    }

    /** 满意度评价（居民端） */
    @PutMapping("/rate")
    public AjaxResult rate(@RequestBody ComComplaint complaint) {
        ComComplaint db = complaintService.getById(complaint.getComplaintId());
        if (db == null) return error("记录不存在");
        if (!"已完成".equals(db.getStatus())) return error("仅可评价已完成的投诉/建议");
        if (db.getRating() != null && db.getReopened() == null) return error("已评价，不可重复评价");
        db.setRating(complaint.getRating());
        db.setUpdateBy(getUsername());
        // 1-2分 → 重开工单，24h重新处理
        if (complaint.getRating() != null && complaint.getRating() <= 2) {
            db.setStatus("处理中");
            db.setReopened(1);
            db.setFinishTime(null);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, 24);
            db.setDeadline(cal.getTime());
        }
        return toAjax(complaintService.updateById(db));
    }

    @PreAuthorize("@ss.hasPermi('com:complaint:remove')")
    @DeleteMapping("/{complaintIds}")
    public AjaxResult remove(@PathVariable Long[] complaintIds) {
        return toAjax(complaintService.removeByIds(java.util.Arrays.asList(complaintIds)));
    }

    // ==================== 处理反馈 ====================
    @PreAuthorize("@ss.hasPermi('com:complaint:feedback:list')")
    @GetMapping("/feedback/list")
    public TableDataInfo feedbackList(ComComplaintFeedback feedback) {
        Page<ComComplaintFeedback> page = startPage();
        List<ComComplaintFeedback> list = feedbackService.lambdaQuery()
                .eq(feedback.getComplaintId() != null, ComComplaintFeedback::getComplaintId, feedback.getComplaintId())
                .orderByDesc(ComComplaintFeedback::getCreateTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:complaint:feedback:add')")
    @PostMapping("/feedback")
    public AjaxResult feedbackAdd(@RequestBody ComComplaintFeedback feedback) {
        feedback.setCreateBy(getUsername());
        feedback.setCreateTime(new java.util.Date());
        return toAjax(feedbackService.save(feedback));
    }

    // ==================== 超时统计 ====================
    @PreAuthorize("@ss.hasPermi('com:complaint:list')")
    @GetMapping("/statistics")
    public AjaxResult statistics() {
        List<ComComplaint> all = complaintService.lambdaQuery()
                .eq(ComComplaint::getStatus, "处理中")
                .list();
        Date now = new Date();
        long yellow = all.stream().filter(c -> c.getDeadline() != null && isYellow(c.getDeadline(), now)).count();
        long red = all.stream().filter(c -> c.getDeadline() != null && c.getDeadline().before(now)).count();

        // 按处理人统计
        Map<Long, Long> byHandler = all.stream()
                .filter(c -> c.getHandlerId() != null && c.getDeadline() != null && c.getDeadline().before(now))
                .collect(Collectors.groupingBy(ComComplaint::getHandlerId, Collectors.counting()));

        Map<String, Object> result = new HashMap<>();
        result.put("yellowCount", yellow);
        result.put("redCount", red);
        result.put("totalProcessing", all.size());
        result.put("overtimeByHandler", byHandler);
        return success(result);
    }

    // ==================== 数据统计 ====================
    @PreAuthorize("@ss.hasPermi('com:complaint:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard() {
        List<ComComplaint> all = complaintService.list();
        Map<String, Object> result = new HashMap<>();

        // 1. 类型分布
        Map<String, Long> typeDist = all.stream()
                .collect(Collectors.groupingBy(c -> isNotEmpty(c.getType()) ? c.getType() : "未知", Collectors.counting()));
        Map<String, Long> categoryDist = all.stream()
                .collect(Collectors.groupingBy(c -> isNotEmpty(c.getCategory()) ? c.getCategory() : "未分类", Collectors.counting()));
        result.put("typeDistribution", typeDist);
        result.put("categoryDistribution", categoryDist);

        // 2. 处理时长（小时）
        List<ComComplaint> done = all.stream()
                .filter(c -> c.getAcceptTime() != null && c.getFinishTime() != null)
                .collect(Collectors.toList());
        if (!done.isEmpty()) {
            double avgHours = done.stream()
                    .mapToLong(c -> (c.getFinishTime().getTime() - c.getAcceptTime().getTime()) / 3600000)
                    .average().orElse(0);
            long maxHours = done.stream()
                    .mapToLong(c -> (c.getFinishTime().getTime() - c.getAcceptTime().getTime()) / 3600000)
                    .max().orElse(0);
            long minHours = done.stream()
                    .mapToLong(c -> (c.getFinishTime().getTime() - c.getAcceptTime().getTime()) / 3600000)
                    .min().orElse(0);
            result.put("avgDurationHours", Math.round(avgHours * 10) / 10.0);
            result.put("maxDurationHours", maxHours);
            result.put("minDurationHours", minHours);
        }

        // 3. 满意度趋势（按月）
        List<ComComplaint> rated = all.stream()
                .filter(c -> c.getRating() != null && c.getCreateTime() != null)
                .collect(Collectors.toList());
        Map<String, Double> ratingTrend = rated.stream()
                .collect(Collectors.groupingBy(
                        c -> String.format("%tY-%<tm", c.getCreateTime()),
                        TreeMap::new,
                        Collectors.averagingInt(ComComplaint::getRating)
                ));
        result.put("ratingTrend", ratingTrend);
        result.put("totalCount", all.size());
        result.put("doneCount", done.size());
        result.put("ratedCount", rated.size());

        return success(result);
    }

    private boolean isNotEmpty(String s) { return s != null && !s.isEmpty(); }

    // ==================== 预警工具 ====================
    private void calcWarn(ComComplaint c) {
        if (!"处理中".equals(c.getStatus()) || c.getDeadline() == null) return;
        if (c.getReopened() != null && c.getReopened() == 1) {
            c.setWarnStatus("不满意重开");
            return;
        }
        Date now = new Date();
        if (c.getDeadline().before(now)) {
            c.setWarnStatus("红牌-超时未完成");
        } else if (isYellow(c.getDeadline(), now)) {
            c.setWarnStatus("黄牌-时限过半");
        } else {
            c.setWarnStatus("正常");
        }
    }

    private boolean isYellow(Date deadline, Date now) {
        if (deadline == null) return false;
        long total = deadline.getTime() - (deadline.getTime() - 48L * 3600 * 1000);
        // 简化：计算已过去的时间是否超过总时限的一半
        long start = deadline.getTime() - 48L * 3600 * 1000;
        long elapsed = now.getTime() - start;
        return elapsed > 24L * 3600 * 1000; // 超过24小时（48小时的一半）
    }
}
