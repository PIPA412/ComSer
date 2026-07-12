package com.zsc.module.visitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.page.TableDataInfo;
import com.zsc.common.utils.SecurityUtils;
import com.zsc.module.visitor.domain.*;
import com.zsc.module.visitor.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 访客管理 Controller
 *
 * @author zsc
 */
@RestController
@RequestMapping("/com/visitor")
public class ComVisitorController extends BaseController {

    @Autowired
    private IComVisitorService visitorService;

    @Autowired
    private IComVisitorRecordService visitorRecordService;

    // ==================== 访客 ====================
    @PreAuthorize("@ss.hasPermi('com:visitor:list')")
    @GetMapping("/list")
    public TableDataInfo list(ComVisitor visitor) {
        Page<ComVisitor> page = startPage();
        LambdaQueryWrapper<ComVisitor> qw = new LambdaQueryWrapper<>();
        qw.like(visitor.getVisitorName() != null, ComVisitor::getVisitorName, visitor.getVisitorName())
          .eq(visitor.getVisitorPhone() != null, ComVisitor::getVisitorPhone, visitor.getVisitorPhone())
          .eq(visitor.getStatus() != null, ComVisitor::getStatus, visitor.getStatus());
        // 管理员指定查询某 invitee 时不过滤；非管理员只能看自己提交的
        if (!SecurityUtils.hasPermi("com:visitor:approve") || visitor.getInviterId() != null) {
            Long filterId = visitor.getInviterId() != null ? visitor.getInviterId() : SecurityUtils.getUserId();
            qw.eq(ComVisitor::getInviterId, filterId);
        }
        qw.orderByDesc(ComVisitor::getCreateTime);
        visitorService.page(page, qw);
        return getDataTable(page.getRecords());
    }

    @PreAuthorize("@ss.hasPermi('com:visitor:query')")
    @GetMapping("/{visitorId}")
    public AjaxResult getInfo(@PathVariable Long visitorId) {
        ComVisitor visitor = visitorService.getById(visitorId);
        if (visitor != null && !SecurityUtils.hasPermi("com:visitor:approve")
                && !SecurityUtils.getUserId().equals(visitor.getInviterId())) {
            return error("无权查看该访客信息");
        }
        return success(visitor);
    }

    @PreAuthorize("@ss.hasPermi('com:visitor:add')")
    @PostMapping
    public AjaxResult add(@RequestBody ComVisitor visitor) {
        visitor.setCreateBy(getUsername());
        // 非管理员自动绑定当前用户为邀请人
        if (!SecurityUtils.hasPermi("com:visitor:approve")) {
            visitor.setInviterId(SecurityUtils.getUserId());
        }
        visitorService.save(visitor);
        return success(visitor.getVisitorId());
    }

    /** 修改访客信息 */
    @PreAuthorize("@ss.hasPermi('com:visitor:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody ComVisitor visitor) {
        ComVisitor existing = visitorService.getById(visitor.getVisitorId());
        if (existing != null && !SecurityUtils.hasPermi("com:visitor:approve")
                && !SecurityUtils.getUserId().equals(existing.getInviterId())) {
            return error("无权修改该访客信息");
        }
        visitor.setUpdateBy(getUsername());
        return toAjax(visitorService.updateById(visitor));
    }

    /** 审批通过 */
    @PreAuthorize("@ss.hasPermi('com:visitor:approve')")
    @PutMapping("/approve/{visitorId}")
    public AjaxResult approve(@PathVariable Long visitorId) {
        ComVisitor visitor = new ComVisitor();
        visitor.setVisitorId(visitorId);
        visitor.setStatus("已通过");
        visitor.setUpdateBy(getUsername());
        return toAjax(visitorService.updateById(visitor));
    }

    /** 审批拒绝 */
    @PreAuthorize("@ss.hasPermi('com:visitor:approve')")
    @PutMapping("/reject/{visitorId}")
    public AjaxResult reject(@PathVariable Long visitorId) {
        ComVisitor visitor = new ComVisitor();
        visitor.setVisitorId(visitorId);
        visitor.setStatus("已拒绝");
        visitor.setUpdateBy(getUsername());
        return toAjax(visitorService.updateById(visitor));
    }

    /** 签离 */
    @PreAuthorize("@ss.hasPermi('com:visitor:checkout')")
    @PutMapping("/checkout/{visitorId}")
    public AjaxResult checkout(@PathVariable Long visitorId) {
        ComVisitor visitor = new ComVisitor();
        visitor.setVisitorId(visitorId);
        visitor.setStatus("已签离");
        visitor.setLeaveTime(new java.util.Date());
        visitor.setUpdateBy(getUsername());
        return toAjax(visitorService.updateById(visitor));
    }

    @PreAuthorize("@ss.hasPermi('com:visitor:remove')")
    @DeleteMapping("/{visitorIds}")
    public AjaxResult remove(@PathVariable Long[] visitorIds) {
        return toAjax(visitorService.removeByIds(java.util.Arrays.asList(visitorIds)));
    }

    // ==================== 通行记录 ====================
    @PreAuthorize("@ss.hasPermi('com:visitor:record:list')")
    @GetMapping("/record/list")
    public TableDataInfo recordList(ComVisitorRecord record) {
        Page<ComVisitorRecord> page = startPage();
        List<ComVisitorRecord> list = visitorRecordService.lambdaQuery()
                .eq(record.getVisitorId() != null, ComVisitorRecord::getVisitorId, record.getVisitorId())
                .orderByDesc(ComVisitorRecord::getPassTime)
                .page(page).getRecords();
        return getDataTable(list);
    }
}
