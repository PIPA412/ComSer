package com.zsc.module.dashboard.service;

import com.zsc.module.dashboard.vo.*;

import java.util.List;

/**
 * 数据驾驶舱 Service 接口
 *
 * @author zsc
 */
public interface IDashboardService {

    /**
     * 人口概览面板：总户数 / 入住率 / 总人口 / 业主租户比 / 性别分布 / 年龄分布
     *                   月度入住迁出趋势 / 各楼栋入住率对比
     */
    PopulationPanelVO getPopulationPanel(Long buildingId, String timeRange);

    /**
     * 房屋概览面板：房屋状态分布 / 面积段分布 / 户型分布
     */
    RoomPanelVO getRoomPanel(Long buildingId, String timeRange);

    /**
     * 工单概览面板：今日新增 / 各状态工单数 / 类型 TOP5 / 月度趋势
     *                  维修人员效率排名 / 超时工单预警滚动列表
     */
    RepairPanelVO getRepairPanel(Long buildingId, String timeRange);

    /**
     * 收费概览面板：本月应收 / 实收 / 收缴率 / 累计欠费 / 月度收缴趋势
     *                  费用结构占比 / 楼栋收缴率排名 / 欠费超90天预警
     */
    FeePanelVO getFeePanel(Long buildingId, String timeRange);

    /**
     * 投诉概览面板：近30天新增 / 待处理 / 超时数量 / 类型分布
     *                  满意度趋势 / 平均处理时长趋势
     */
    ComplaintPanelVO getComplaintPanel(Long buildingId, String timeRange);

    /**
     * 活动概览面板：年度场次 / 总参与人次 / 平均签到率 / 类型分布 / 月度趋势
     */
    ActivityPanelVO getActivityPanel(Long buildingId, String timeRange);

    /**
     * 楼栋筛选列表（全局筛选器使用）
     */
    List<DashboardVO.BuildingOption> getBuildingOptions();
}
