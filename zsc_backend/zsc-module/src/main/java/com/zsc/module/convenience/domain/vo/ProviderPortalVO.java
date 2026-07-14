package com.zsc.module.convenience.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 居民端 - 服务商列表 VO
 *
 * @author zsc
 */
@Data
public class ProviderPortalVO {

    /** 服务商ID */
    private Long providerId;

    /** 服务商名称 */
    private String providerName;

    /** 服务类型 */
    private String serviceType;

    /** 联系电话 */
    private String contactPhone;

    /** 地址 */
    private String address;

    /** 描述 */
    private String description;

    /** 入驻时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date settleDate;

    /** 服务项目数量 */
    private Long serviceItemCount;

    /** 平均评分 */
    private Double avgRating;

    /** 优惠信息（备注字段） */
    private String remark;
}
