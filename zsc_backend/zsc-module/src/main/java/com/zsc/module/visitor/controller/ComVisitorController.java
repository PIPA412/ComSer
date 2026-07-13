package com.zsc.module.visitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.page.TableDataInfo;
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
        List<ComVisitor> list = visitorService.lambdaQuery()
                .like(visitor.getVisitorName() != null, ComVisitor::getVisitorName, visitor.getVisitorName())
                .eq(visitor.getVisitorPhone() != null, ComVisitor::getVisitorPhone, visitor.getVisitorPhone())
                .eq(visitor.getStatus() != null, ComVisitor::getStatus, visitor.getStatus())
                .eq(visitor.getInviterId() != null, ComVisitor::getInviterId, visitor.getInviterId())
                .orderByDesc(ComVisitor::getCreateTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:visitor:query')")
    @GetMapping("/{visitorId}")
    public AjaxResult getInfo(@PathVariable Long visitorId) {
        return success(visitorService.getById(visitorId));
    }

    @PreAuthorize("@ss.hasPermi('com:visitor:add')")
    @PostMapping
    public AjaxResult add(@RequestBody ComVisitor visitor) {
        visitor.setCreateBy(getUsername());
        // TODO: 生成通行二维码
        return toAjax(visitorService.save(visitor));
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
