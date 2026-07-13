package com.zsc.module.visitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsc.module.visitor.domain.ComVisitor;

public interface IComVisitorService extends IService<ComVisitor> {

    /**
     * 审批通过访客并生成通行二维码
     *
     * @param visitorId 访客ID
     * @param operator  操作人
     * @return 更新后的访客对象
     */
    ComVisitor approveWithQrCode(Long visitorId, String operator);

    /**
     * 获取访客通行二维码 Base64 图片
     *
     * @param visitorId 访客ID
     * @return Base64 编码的 PNG 图片，无二维码时返回 null
     */
    String getQrCodeBase64(Long visitorId);
}
