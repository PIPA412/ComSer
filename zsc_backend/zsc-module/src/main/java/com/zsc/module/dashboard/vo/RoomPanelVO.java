package com.zsc.module.dashboard.vo;

import lombok.Data;
import java.util.Map;

/**
 * 房屋概览面板 VO
 */
@Data
public class RoomPanelVO {

    /** 房屋状态分布（状态名 → 数量） */
    private Map<String, Long> statusDistribution;

    /** 面积段分布（面积段 → 数量），如 "<60㎡" */
    private Map<String, Long> areaDistribution;

    /** 户型分布（户型名 → 数量） */
    private Map<String, Long> layoutDistribution;
}
