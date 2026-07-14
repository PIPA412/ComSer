package com.zsc.module.dashboard.controller;

import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.page.TableDataInfo;
import com.zsc.module.dashboard.domain.ComAlertRecord;
import com.zsc.module.dashboard.domain.ComAlertRule;
import com.zsc.module.dashboard.service.IComAlertRecordService;
import com.zsc.module.dashboard.service.IComAlertRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 预警规则 & 记录 Controller
 */
@RestController
@RequestMapping("/com/dashboard/alert")
public class AlertController extends BaseController {

    @Autowired
    private IComAlertRuleService alertRuleService;

    @Autowired
    private IComAlertRecordService alertRecordService;

    // ==================== 预警规则 ====================

    @PreAuthorize("@ss.hasPermi('com:dashboard:alert')")
    @GetMapping("/rule/list")
    public TableDataInfo ruleList(ComAlertRule rule) {
        startPage();
        return getDataTable(alertRuleService.lambdaQuery()
                .like(rule.getRuleName() != null, ComAlertRule::getRuleName, rule.getRuleName())
                .orderByAsc(ComAlertRule::getRuleId)
                .list());
    }

    @PreAuthorize("@ss.hasPermi('com:dashboard:alert')")
    @GetMapping("/rule/{ruleId}")
    public AjaxResult getRule(@PathVariable Long ruleId) {
        return success(alertRuleService.getById(ruleId));
    }

    @PreAuthorize("@ss.hasPermi('com:dashboard:alert')")
    @PostMapping("/rule")
    public AjaxResult addRule(@RequestBody ComAlertRule rule) {
        rule.setCreateBy(getUsername());
        rule.setCreateTime(new Date());
        return toAjax(alertRuleService.save(rule));
    }

    @PreAuthorize("@ss.hasPermi('com:dashboard:alert')")
    @PutMapping("/rule")
    public AjaxResult updateRule(@RequestBody ComAlertRule rule) {
        rule.setUpdateBy(getUsername());
        rule.setUpdateTime(new Date());
        return toAjax(alertRuleService.updateById(rule));
    }

    @PreAuthorize("@ss.hasPermi('com:dashboard:alert')")
    @DeleteMapping("/rule/{ruleIds}")
    public AjaxResult deleteRule(@PathVariable Long[] ruleIds) {
        return toAjax(alertRuleService.removeByIds(java.util.Arrays.asList(ruleIds)));
    }

    // ==================== 预警记录 ====================

    @PreAuthorize("@ss.hasPermi('com:dashboard:alert')")
    @GetMapping("/record/list")
    public TableDataInfo recordList(ComAlertRecord record) {
        startPage();
        return getDataTable(alertRecordService.lambdaQuery()
                .eq(record.getHandleStatus() != null, ComAlertRecord::getHandleStatus, record.getHandleStatus())
                .ge(record.getTriggerTime() != null, ComAlertRecord::getTriggerTime, record.getTriggerTime())
                .orderByDesc(ComAlertRecord::getTriggerTime)
                .list());
    }

    /** 获取当前待处理的预警（供驾驶舱首页使用，已停用规则的预警不显示） */
    @PreAuthorize("@ss.hasPermi('com:dashboard:view')")
    @GetMapping("/record/pending")
    public AjaxResult pendingRecords() {
        // 获取所有启用中的规则ID
        java.util.Set<Long> activeRuleIds = alertRuleService.lambdaQuery()
                .eq(ComAlertRule::getStatus, "0")
                .list().stream().map(ComAlertRule::getRuleId).collect(java.util.stream.Collectors.toSet());
        return success(alertRecordService.lambdaQuery()
                .eq(ComAlertRecord::getHandleStatus, "待处理")
                .orderByDesc(ComAlertRecord::getTriggerTime)
                .list().stream()
                .filter(r -> r.getRuleId() != null && activeRuleIds.contains(r.getRuleId()))
                .collect(java.util.stream.Collectors.toList()));
    }

    /** 标记预警处置 */
    @PreAuthorize("@ss.hasPermi('com:dashboard:alert')")
    @PutMapping("/record/handle")
    public AjaxResult handleRecord(@RequestBody ComAlertRecord record) {
        ComAlertRecord db = alertRecordService.getById(record.getRecordId());
        if (db == null) return error("记录不存在");
        db.setHandleStatus(record.getHandleStatus());
        db.setHandleBy(getUsername());
        db.setHandleTime(new Date());
        db.setHandleRemark(record.getHandleRemark());
        return toAjax(alertRecordService.updateById(db));
    }

    /** 获取所有预警规则（含已停用，供前端管理） */
    @PreAuthorize("@ss.hasPermi('com:dashboard:view')")
    @GetMapping("/rule/all")
    public AjaxResult allRules() {
        return success(alertRuleService.lambdaQuery().orderByAsc(ComAlertRule::getRuleId).list());
    }
}
