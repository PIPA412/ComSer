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
        TableDataInfo rsp = new TableDataInfo();
        rsp.setCode(com.zsc.common.constant.HttpStatus.SUCCESS);
        rsp.setMsg("查询成功");
        rsp.setRows(page.getRecords());
        rsp.setTotal(page.getTotal());
        return rsp;
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
        // 管理员未指定邀请人时默认绑定自己
        if (visitor.getInviterId() == null) {
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
        if (existing == null) {
            return error("访客记录不存在");
        }
        // 非管理员只能修改自己的访客记录，且不能修改状态/二维码/邀请人
        if (!SecurityUtils.hasPermi("com:visitor:approve")) {
            if (!SecurityUtils.getUserId().equals(existing.getInviterId())) {
                return error("无权修改该访客信息");
            }
            // 非管理员禁止修改特权字段
            visitor.setStatus(null);
            visitor.setQrCode(null);
            visitor.setInviterId(null);
        }
        visitor.setUpdateBy(getUsername());
        return toAjax(visitorService.updateById(visitor));
    }

    /** 审批通过（生成通行二维码） */
    @PreAuthorize("@ss.hasPermi('com:visitor:approve')")
    @PutMapping("/approve/{visitorId}")
    public AjaxResult approve(@PathVariable Long visitorId) {
        ComVisitor result = visitorService.approveWithQrCode(visitorId, getUsername());
        if (result == null) {
            return error("访客不存在或当前状态不允许审批");
        }
        return success();
    }

    /** 审批拒绝 */
    @PreAuthorize("@ss.hasPermi('com:visitor:approve')")
    @PutMapping("/reject/{visitorId}")
    public AjaxResult reject(@PathVariable Long visitorId) {
        ComVisitor exist = visitorService.getById(visitorId);
        if (exist == null) {
            return error("访客不存在");
        }
        if (!"待审批".equals(exist.getStatus())) {
            return error("仅待审批状态的访客可以拒绝");
        }
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
        ComVisitor exist = visitorService.getById(visitorId);
        if (exist == null) {
            return error("访客不存在");
        }
        if (!"已通过".equals(exist.getStatus())) {
            return error("仅已通过状态的访客可以签离");
        }
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

    /** 获取访客通行二维码（Base64 图片） */
    @PreAuthorize("@ss.hasPermi('com:visitor:query')")
    @GetMapping("/qrcode/{visitorId}")
    public AjaxResult getQrCode(@PathVariable Long visitorId) {
        String base64 = visitorService.getQrCodeBase64(visitorId);
        if (base64 == null) {
            return error("访客不存在或尚未生成通行二维码");
        }
        // 使用显式两参数形式，避免 AjaxResult.success(String) 重载把 base64 当成 msg
        return AjaxResult.success("操作成功", base64);
    }

    // ==================== 通行记录 ====================
    @PreAuthorize("@ss.hasPermi('com:visitor:record:list')")
    @GetMapping("/record/list")
    public TableDataInfo recordList(ComVisitorRecord record) {
        Page<ComVisitorRecord> page = startPage();
        visitorRecordService.lambdaQuery()
                .eq(record.getVisitorId() != null, ComVisitorRecord::getVisitorId, record.getVisitorId())
                .orderByDesc(ComVisitorRecord::getPassTime)
                .page(page);
        TableDataInfo rsp = new TableDataInfo();
        rsp.setCode(com.zsc.common.constant.HttpStatus.SUCCESS);
        rsp.setMsg("查询成功");
        rsp.setRows(page.getRecords());
        rsp.setTotal(page.getTotal());
        return rsp;
    }
}
