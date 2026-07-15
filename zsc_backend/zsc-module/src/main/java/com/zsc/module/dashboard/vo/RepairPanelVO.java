package com.zsc.module.dashboard.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 工单概览面板 VO
 */
@Data
public class RepairPanelVO {

    /** 实时统计 */
    private Long todayNew;
    private Long pendingCount;
    private Long processingCount;
    private Long waitingConfirmCount;

    /** 近30天报修类型 TOP5（类型名 → 数量） */
    private List<Map.Entry<String, Long>> repairTypeTop5;

    /** 近12个月月报修趋势 */
    private List<MonthlyTrend> monthlyTrend;

    /** 维修人员效率排名 */
    private List<WorkerRank> workerRankList;

    /** 超时/即将超时工单列表 */
    private List<OverdueItem> overdueList;

    @Data
    public static class MonthlyTrend {
        private String month;
        private Long count;
    }

    @Data
    public static class WorkerRank {
        private String workerName;
        private Long completedCount;
        private BigDecimal avgRating;
        private BigDecimal overdueRate;
    }

    @Data
    public static class OverdueItem {
        private String repairNo;
        private String repairType;
        private String urgency;
        private String status;
        private String createTime;
        private Boolean isOverdue;
    }
}
