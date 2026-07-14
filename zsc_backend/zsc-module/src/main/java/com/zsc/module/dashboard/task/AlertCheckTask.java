package com.zsc.module.dashboard.task;

import com.zsc.module.dashboard.domain.ComAlertRecord;
import com.zsc.module.dashboard.domain.ComAlertRule;
import com.zsc.module.dashboard.service.IComAlertRecordService;
import com.zsc.module.dashboard.service.IComAlertRuleService;
import com.zsc.module.fee.domain.ComFeeBill;
import com.zsc.module.fee.service.IComFeeBillService;
import com.zsc.module.complaint.domain.ComComplaint;
import com.zsc.module.complaint.service.IComComplaintService;
import com.zsc.module.repair.domain.ComRepair;
import com.zsc.module.repair.service.IComRepairService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预警检测定时任务：每小时检测一次业务指标是否触发预警规则
 */
@Component
public class AlertCheckTask {

    private static final Logger log = LoggerFactory.getLogger(AlertCheckTask.class);

    @Autowired
    private IComAlertRuleService alertRuleService;

    @Autowired
    private IComAlertRecordService alertRecordService;

    @Autowired
    private IComFeeBillService feeBillService;

    @Autowired
    private IComComplaintService complaintService;

    @Autowired
    private IComRepairService repairService;

    /** 每小时整点检测 */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkAllRules() {
        log.info("[预警检测] 开始扫描预警规则...");
        List<ComAlertRule> rules = alertRuleService.lambdaQuery()
                .eq(ComAlertRule::getStatus, "0") // 只检测已启用的规则
                .list();
        if (rules.isEmpty()) {
            log.info("[预警检测] 无启用的预警规则，跳过");
            return;
        }
        for (ComAlertRule rule : rules) {
            try {
                BigDecimal currentValue = getMetricValue(rule.getMetricKey());
                if (currentValue == null) continue;
                boolean triggered = isTriggered(currentValue, rule.getThreshold(),
                        rule.getCompareType());
                if (triggered) {
                    // 检查是否已有未处理的同规则预警（避免重复触发）
                    long pending = alertRecordService.lambdaQuery()
                            .eq(ComAlertRecord::getRuleId, rule.getRuleId())
                            .eq(ComAlertRecord::getHandleStatus, "待处理")
                            .count();
                    if (pending == 0) {
                        ComAlertRecord record = new ComAlertRecord();
                        record.setRuleId(rule.getRuleId());
                        record.setRuleName(rule.getRuleName());
                        record.setMetricName(rule.getMetricName());
                        record.setTriggerValue(currentValue);
                        record.setThreshold(rule.getThreshold());
                        record.setTriggerTime(new Date());
                        record.setHandleStatus("待处理");
                        record.setCreateTime(new Date());
                        alertRecordService.save(record);
                        log.warn("[预警触发] {}: 当前值={}, 阈值={}",
                                rule.getRuleName(), currentValue, rule.getThreshold());
                    }
                }
            } catch (Exception e) {
                log.error("[预警检测] 规则 {} 检测异常", rule.getRuleName(), e);
            }
        }
        log.info("[预警检测] 扫描完成");
    }

    /** 根据 metricKey 获取当前指标值 */
    private BigDecimal getMetricValue(String metricKey) {
        if (metricKey == null) return null;
        switch (metricKey) {
            case "collection_rate": {
                // 当月收缴率
                List<ComFeeBill> all = feeBillService.lambdaQuery().list();
                if (all.isEmpty()) return BigDecimal.ZERO;
                BigDecimal receivable = BigDecimal.ZERO, received = BigDecimal.ZERO;
                String thisMonth = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
                for (ComFeeBill b : all) {
                    if (b.getBillPeriod() != null && b.getBillPeriod().startsWith(thisMonth)) {
                        receivable = receivable.add(nz(b.getAmount()));
                        if ("已缴".equals(b.getStatus())) received = received.add(nz(b.getPaidAmount()));
                    }
                }
                if (receivable.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.valueOf(100);
                return received.multiply(BigDecimal.valueOf(100))
                        .divide(receivable, 1, RoundingMode.HALF_UP);
            }
            case "complaint_monthly": {
                // 月度投诉量
                String thisMonth = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
                return BigDecimal.valueOf(complaintService.lambdaQuery()
                        .like(ComComplaint::getCreateTime, thisMonth)
                        .eq(ComComplaint::getType, "投诉")
                        .count());
            }
            case "repair_overdue_rate": {
                // 报修超时率 = 超24h未完成工单 / 总工单
                List<ComRepair> all = repairService.lambdaQuery().list();
                if (all.isEmpty()) return BigDecimal.ZERO;
                Date now = new Date();
                long total = all.size();
                long overdue = all.stream()
                        .filter(r -> r.getCreateTime() != null
                                && !"已完成".equals(r.getStatus())
                                && !"已评价".equals(r.getStatus())
                                && !"已取消".equals(r.getStatus())
                                && now.getTime() - r.getCreateTime().getTime() > 24L * 3600 * 1000)
                        .count();
                return BigDecimal.valueOf(overdue * 100.0 / total).setScale(1, RoundingMode.HALF_UP);
            }
            default:
                log.warn("[预警检测] 未知指标: {}", metricKey);
                return null;
        }
    }

    private boolean isTriggered(BigDecimal current, BigDecimal threshold, String compareType) {
        if (current == null || threshold == null) return false;
        if (compareType == null) compareType = "GT";
        switch (compareType) {
            case "GT": return current.compareTo(threshold) > 0;
            case "LT": return current.compareTo(threshold) < 0;
            case "GTE": return current.compareTo(threshold) >= 0;
            case "LTE": return current.compareTo(threshold) <= 0;
            default: return false;
        }
    }

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}
