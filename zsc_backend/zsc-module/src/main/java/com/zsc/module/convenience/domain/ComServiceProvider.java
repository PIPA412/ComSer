package com.zsc.module.convenience.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务商 com_service_provider
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_service_provider")
public class ComServiceProvider extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
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

    /** 状态（0正常 1停用） */
    private String status;
}
