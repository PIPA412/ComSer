package com.zsc.module.dashboard.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 投诉概览面板 VO
 */
@Data
public class ComplaintPanelVO {

    /** 近30天统计 */
    private Long recentNewCount;
    private Long pendingCount;
    private Long overdueCount;

    /** 投诉类型分布（类型 → 数量） */
    private Map<String, Long> typeDistribution;

    /** 近12个月满意度趋势 */
    private List<MonthlySatisfaction> satisfactionTrend;

    /** 近12个月平均处理时长（小时） */
    private List<MonthlyDuration> durationTrend;

    @Data
    public static class MonthlySatisfaction {
        private String month;
        private BigDecimal avgRating;
    }

    @Data
    public static class MonthlyDuration {
        private String month;
        private BigDecimal avgHours;
    }
}
