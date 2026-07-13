package com.zsc.module.visitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.common.tools.QRCodeUtils;
import com.zsc.module.visitor.domain.ComVisitor;
import com.zsc.module.visitor.mapper.ComVisitorMapper;
import com.zsc.module.visitor.service.IComVisitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service
public class ComVisitorServiceImpl extends ServiceImpl<ComVisitorMapper, ComVisitor> implements IComVisitorService {

    private static final Logger log = LoggerFactory.getLogger(ComVisitorServiceImpl.class);

    @Override
    public ComVisitor approveWithQrCode(Long visitorId, String operator) {
        ComVisitor exist = getById(visitorId);
        if (exist == null) {
            log.warn("approveWithQrCode: 访客不存在 visitorId={}", visitorId);
            return null;
        }
        if (!"待审批".equals(exist.getStatus())) {
            log.warn("approveWithQrCode: 状态不允许审批 visitorId={}, status={}", visitorId, exist.getStatus());
            return null;
        }
        // 生成二维码内容
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.HOUR_OF_DAY, 24);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String qrContent = String.format("VISITOR|%d|%s|%s|%d|%s|%s",
                exist.getVisitorId(),
                exist.getVisitorName() != null ? exist.getVisitorName() : "",
                exist.getVisitorPhone() != null ? exist.getVisitorPhone() : "",
                exist.getRoomId() != null ? exist.getRoomId() : 0,
                sdf.format(now),
                sdf.format(cal.getTime()));

        log.info("生成访客二维码 visitorId={}, content={}", visitorId, qrContent);

        // 状态条件防竞态：只有"待审批"状态的记录才会被更新
        LambdaUpdateWrapper<ComVisitor> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ComVisitor::getVisitorId, visitorId)
               .eq(ComVisitor::getStatus, "待审批")
               .set(ComVisitor::getStatus, "已通过")
               .set(ComVisitor::getQrCode, qrContent)
               .set(ComVisitor::getArrivalTime, now)
               .set(ComVisitor::getUpdateBy, operator);

        boolean updated = update(wrapper);
        log.info("访客审批更新结果 visitorId={}, updated={}", visitorId, updated);

        if (!updated) {
            log.warn("approveWithQrCode: 更新失败（可能已被并发审批）visitorId={}", visitorId);
            return null;
        }

        exist.setStatus("已通过");
        exist.setQrCode(qrContent);
        exist.setArrivalTime(now);
        exist.setUpdateBy(operator);
        return exist;
    }

    @Override
    public String getQrCodeBase64(Long visitorId) {
        ComVisitor visitor = getById(visitorId);
        if (visitor == null) {
            log.warn("getQrCodeBase64: 访客不存在 visitorId={}", visitorId);
            return null;
        }
        if (visitor.getQrCode() == null || visitor.getQrCode().isEmpty()) {
            log.warn("getQrCodeBase64: qrCode 为空 visitorId={}", visitorId);
            return null;
        }
        log.info("生成二维码图片 visitorId={}, contentLen={}", visitorId, visitor.getQrCode().length());
        return QRCodeUtils.generateBase64(visitor.getQrCode());
    }
}
