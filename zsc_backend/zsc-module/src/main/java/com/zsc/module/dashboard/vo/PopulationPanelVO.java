package com.zsc.module.dashboard.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 人口概览面板 VO
 */
@Data
public class PopulationPanelVO {

    /** 核心指标 */
    private Long totalRooms;
    private Long occupiedRooms;
    private BigDecimal occupancyRate;
    private Long totalPopulation;
    private Long ownerCount;
    private Long tenantCount;

    /** 年龄分布（年龄段 → 人数） */
    private Map<String, Long> ageDistribution;

    /** 性别分布（男/女 → 人数） */
    private Map<String, Long> genderDistribution;

    /** 近12个月入住迁出趋势 */
    private List<MonthlyTrend> moveInOutTrend;

    /** 各楼栋入住率 */
    private List<BuildingOccupancy> buildingOccupancyList;

    @Data
    public static class MonthlyTrend {
        private String month;
        private Long moveIn;
        private Long moveOut;
    }

    @Data
    public static class BuildingOccupancy {
        private String buildingName;
        private BigDecimal occupancyRate;
        private Long totalRooms;
        private Long occupiedRooms;
    }
}
