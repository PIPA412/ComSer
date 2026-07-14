package com.zsc.module.activity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.page.TableDataInfo;
import com.zsc.module.activity.domain.*;
import com.zsc.module.activity.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社区活动管理 Controller
 *
 * @author zsc
 */
@RestController
@RequestMapping("/com/activity")
public class ComActivityController extends BaseController {

    @Autowired
    private IComActivityService activityService;

    @Autowired
    private IComActivitySignupService signupService;

    // ==================== 活动 ====================
    @PreAuthorize("@ss.hasPermi('com:activity:list')")
    @GetMapping("/list")
    public TableDataInfo list(ComActivity activity) {
        Page<ComActivity> page = startPage();
        List<ComActivity> list = activityService.lambdaQuery()
                .like(activity.getTitle() != null, ComActivity::getTitle, activity.getTitle())
                .eq(activity.getActivityType() != null, ComActivity::getActivityType, activity.getActivityType())
                .eq(activity.getStatus() != null, ComActivity::getStatus, activity.getStatus())
                .orderByDesc(ComActivity::getCreateTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:activity:query')")
    @GetMapping("/{activityId}")
    public AjaxResult getInfo(@PathVariable Long activityId) {
        return success(activityService.getById(activityId));
    }

    @PreAuthorize("@ss.hasPermi('com:activity:add')")
    @PostMapping
    public AjaxResult add(@RequestBody ComActivity activity) {
        activity.setCreateBy(getUsername());
        activity.setCreateTime(new java.util.Date());
        if (activity.getStatus() == null) activity.setStatus("草稿");
        if (activity.getActualParticipants() == null) activity.setActualParticipants(0);
        return toAjax(activityService.save(activity));
    }

    /** 发布活动（草稿/待审核 → 报名中） */
    @PreAuthorize("@ss.hasPermi('com:activity:edit')")
    @PutMapping("/publish")
    public AjaxResult publish(@RequestBody ComActivity activity) {
        ComActivity db = activityService.getById(activity.getActivityId());
        if (db == null) return error("活动不存在");
        if (!"草稿".equals(db.getStatus()) && !"待审核".equals(db.getStatus())) return error("仅草稿/待审核状态可发布");
        db.setStatus("报名中");
        db.setUpdateBy(getUsername());
        return toAjax(activityService.updateById(db));
    }

    /** 结束活动 */
    @PreAuthorize("@ss.hasPermi('com:activity:edit')")
    @PutMapping("/finish")
    public AjaxResult finish(@RequestBody ComActivity activity) {
        ComActivity db = activityService.getById(activity.getActivityId());
        if (db == null) return error("活动不存在");
        db.setStatus("已结束");
        db.setUpdateBy(getUsername());
        return toAjax(activityService.updateById(db));
    }

    /** 取消活动 */
    @PreAuthorize("@ss.hasPermi('com:activity:edit')")
    @PutMapping("/cancel")
    public AjaxResult cancel(@RequestBody ComActivity activity) {
        ComActivity db = activityService.getById(activity.getActivityId());
        if (db == null) return error("活动不存在");
        db.setStatus("已取消");
        db.setUpdateBy(getUsername());
        return toAjax(activityService.updateById(db));
    }

    @PreAuthorize("@ss.hasPermi('com:activity:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody ComActivity activity) {
        activity.setUpdateBy(getUsername());
        return toAjax(activityService.updateById(activity));
    }

    @PreAuthorize("@ss.hasPermi('com:activity:remove')")
    @DeleteMapping("/{activityIds}")
    public AjaxResult remove(@PathVariable Long[] activityIds) {
        return toAjax(activityService.removeByIds(java.util.Arrays.asList(activityIds)));
    }

    // ==================== 活动报名 ====================
    @PreAuthorize("@ss.hasPermi('com:activity:signup:list')")
    @GetMapping("/signup/list")
    public TableDataInfo signupList(ComActivitySignup signup) {
        Page<ComActivitySignup> page = startPage();
        List<ComActivitySignup> list = signupService.lambdaQuery()
                .eq(signup.getActivityId() != null, ComActivitySignup::getActivityId, signup.getActivityId())
                .eq(signup.getUserId() != null, ComActivitySignup::getUserId, signup.getUserId())
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:activity:signup:add')")
    @PostMapping("/signup")
    public AjaxResult signupAdd(@RequestBody ComActivitySignup signup) {
        signup.setCreateBy(getUsername());
        signup.setStatus("已报名");
        // TODO: 校验报名人数上限
        return toAjax(signupService.save(signup));
    }

    /** 签到 */
    @PreAuthorize("@ss.hasPermi('com:activity:signin')")
    @PutMapping("/signin/{signupId}")
    public AjaxResult signin(@PathVariable Long signupId) {
        ComActivitySignup signup = new ComActivitySignup();
        signup.setSignupId(signupId);
        signup.setSigninTime(new java.util.Date());
        signup.setSigninMethod("扫码");
        return toAjax(signupService.updateById(signup));
    }
}
