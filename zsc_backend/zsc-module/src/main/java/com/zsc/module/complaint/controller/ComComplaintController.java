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

import java.util.List;

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
                .eq(complaint.getStatus() != null, ComComplaint::getStatus, complaint.getStatus())
                .like(complaint.getTitle() != null, ComComplaint::getTitle, complaint.getTitle())
                .eq(complaint.getUserId() != null, ComComplaint::getUserId, complaint.getUserId())
                .orderByDesc(ComComplaint::getCreateTime)
                .page(page).getRecords();
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
        if (db.getRating() != null) return error("已评价，不可重复评价");
        db.setRating(complaint.getRating());
        db.setUpdateBy(getUsername());
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
        // TODO: 更新投诉状态
        return toAjax(feedbackService.save(feedback));
    }
}
