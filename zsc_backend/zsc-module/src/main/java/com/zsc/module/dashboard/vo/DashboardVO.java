package com.zsc.module.dashboard.vo;

import lombok.Data;
import java.util.List;

/**
 * 数据驾驶舱统一响应 VO（所有面板汇总）
 */
@Data
public class DashboardVO {

    private PopulationPanelVO population;
    private RoomPanelVO room;
    private RepairPanelVO repair;
    private FeePanelVO fee;
    private ComplaintPanelVO complaint;
    private ActivityPanelVO activity;

    /** 可筛选的楼栋列表 */
    private List<BuildingOption> buildingOptions;

    @Data
    public static class BuildingOption {
        private Long buildingId;
        private String buildingName;
    }
}
