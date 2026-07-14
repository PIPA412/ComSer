package com.zsc.module.convenience.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 居民端 - 服务商详情 VO
 *
 * @author zsc
 */
@Data
public class ProviderDetailVO {

    /** 服务商ID */
    private Long providerId;

    /** 服务商名称 */
    private String providerName;

    /** 服务类型 */
    private String serviceType;

    /** 联系人 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 地址 */
    private String address;

    /** 描述 */
    private String description;

    /** 入驻时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date settleDate;

    /** 平均评分 */
    private Double avgRating;

    /** 备注（优惠信息） */
    private String remark;

    /** 服务项目列表 */
    private List<ServiceItemVO> items;
}
