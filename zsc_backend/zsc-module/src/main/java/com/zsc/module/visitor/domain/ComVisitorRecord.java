package com.zsc.module.visitor.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 访客通行记录 com_visitor_record
 *
 * @author zsc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_visitor_record")
public class ComVisitorRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long recordId;

    /** 访客ID */
    private Long visitorId;

    /** 通行类型（入园/出园） */
    private String passType;

    /** 通行时间 */
    private Date passTime;

    /** 门禁设备 */
    private String gateDevice;
}
