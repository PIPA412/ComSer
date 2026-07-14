package com.zsc.module.dashboard.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 活动概览面板 VO
 */
@Data
public class ActivityPanelVO {

    /** 年度活动统计 */
    private Long totalEvents;
    private Long totalParticipants;
    private BigDecimal avgCheckInRate;

    /** 活动类型分布（类型 → 场次） */
    private Map<String, Long> typeDistribution;

    /** 每月活动场次趋势 */
    private List<MonthlyCount> monthlyTrend;

    @Data
    public static class MonthlyCount {
        private String month;
        private Long count;
    }
}
