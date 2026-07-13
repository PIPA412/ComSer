package com.zsc.module.convenience.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 居民端 - 服务项目 VO
 *
 * @author zsc
 */
@Data
public class ServiceItemVO {

    /** 项目ID */
    private Long itemId;

    /** 服务名称 */
    private String itemName;

    /** 服务价格 */
    private BigDecimal price;

    /** 服务时长（分钟） */
    private Integer duration;

    /** 预约方式 */
    private String bookingMethod;

    /** 服务详情 */
    private String serviceDetail;
}
