package com.zsc.module.dashboard.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 收费概览面板 VO
 */
@Data
public class FeePanelVO {

    /** 核心指标卡片 */
    private BigDecimal monthReceivable;
    private BigDecimal monthReceived;
    private BigDecimal collectionRate;
    private BigDecimal totalArrears;

    /** 近12个月收缴率趋势 */
    private List<MonthlyRate> monthlyTrend;

    /** 费用结构分析（费用名称 → 收入金额） */
    private Map<String, BigDecimal> feeStructure;

    /** 各楼栋收缴率 */
    private List<BuildingRate> buildingRateList;

    /** 欠费超90天预警 */
    private Long arrearsOver90DaysCount;
    private BigDecimal arrearsOver90DaysAmount;

    @Data
    public static class MonthlyRate {
        private String month;
        private BigDecimal rate;
    }

    @Data
    public static class BuildingRate {
        private String buildingName;
        private BigDecimal rate;
    }
}
