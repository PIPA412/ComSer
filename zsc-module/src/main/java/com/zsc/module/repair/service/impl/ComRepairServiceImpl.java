package com.zsc.module.repair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.repair.domain.ComRepair;
import com.zsc.module.repair.domain.ComRepairRecord;
import com.zsc.module.repair.mapper.ComRepairMapper;
import com.zsc.module.repair.mapper.ComRepairRecordMapper;
import com.zsc.module.repair.service.IComRepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 报修管理 Service 实现
 *
 * @author zsc
 */
@Service
public class ComRepairServiceImpl extends ServiceImpl<ComRepairMapper, ComRepair> implements IComRepairService {

    @Autowired
    private ComRepairRecordMapper repairRecordMapper;

    /**
     * 生成报修编号：BX + yyyyMMdd + 4位序号
     */
    @Override
    public String generateRepairNo() {
        String datePrefix = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String prefix = "BX" + datePrefix;
        int prefixLen = prefix.length();
        // 查询当天最大编号
        LambdaQueryWrapper<ComRepair> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(ComRepair::getRepairNo, prefix)
               .orderByDesc(ComRepair::getRepairNo)
               .last("LIMIT 1");
        ComRepair last = getOne(wrapper);
        int seq = 1;
        if (last != null && last.getRepairNo() != null && last.getRepairNo().length() >= prefixLen) {
            try {
                seq = Integer.parseInt(last.getRepairNo().substring(prefixLen)) + 1;
            } catch (NumberFormatException e) {
                seq = 1;
            }
        }
        return prefix + String.format("%04d", seq);
    }

    /**
     * 受理报修单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptRepair(Long repairId, Long assigneeId, String updateBy) {
        // 更新报修单
        ComRepair repair = new ComRepair();
        repair.setRepairId(repairId);
        repair.setAssigneeId(assigneeId);
        repair.setStatus("处理中");
        repair.setAcceptTime(new Date());
        repair.setUpdateBy(updateBy);
        boolean updated = updateById(repair);

        // 创建维修记录
        ComRepairRecord record = new ComRepairRecord();
        record.setRepairId(repairId);
        record.setWorkerId(assigneeId);
        record.setActionType("派单");
        record.setDescription("管理员已指派维修人员");
        record.setCreateBy(updateBy);
        repairRecordMapper.insert(record);

        return updated;
    }

    /**
     * 完工
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean finishRepair(Long repairId, String handleNote, String proofUrls, String updateBy) {
        // 查询原报修单获取受理人
        ComRepair original = getById(repairId);

        // 更新报修单
        ComRepair repair = new ComRepair();
        repair.setRepairId(repairId);
        repair.setStatus("已完成");
        repair.setFinishTime(new Date());
        if (handleNote != null && !handleNote.isEmpty()) {
            repair.setHandleNote(handleNote);
        }
        repair.setUpdateBy(updateBy);
        boolean updated = updateById(repair);

        // 创建维修记录
        ComRepairRecord record = new ComRepairRecord();
        record.setRepairId(repairId);
        record.setWorkerId(original != null ? original.getAssigneeId() : null);
        record.setActionType("完工");
        record.setDescription("维修完成");
        if (proofUrls != null && !proofUrls.isEmpty()) {
            record.setProofUrls(proofUrls);
        }
        record.setCreateBy(updateBy);
        repairRecordMapper.insert(record);

        return updated;
    }

    /**
     * 评价（评价后状态保持"已完成"，不改为"已评价"）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rateRepair(Long repairId, Integer rating, String feedback, String updateBy) {
        ComRepair repair = new ComRepair();
        repair.setRepairId(repairId);
        repair.setRating(rating);
        repair.setFeedback(feedback);
        // 不再修改状态，保持"已完成"
        repair.setUpdateBy(updateBy);
        boolean updated = updateById(repair);

        // 创建评价记录
        ComRepair original = getById(repairId);
        ComRepairRecord record = new ComRepairRecord();
        record.setRepairId(repairId);
        record.setWorkerId(original != null ? original.getAssigneeId() : null);
        record.setActionType("评价");
        record.setDescription("用户评价：" + rating + "星" + (feedback != null && !feedback.isEmpty() ? "，反馈：" + feedback : ""));
        record.setCreateBy(updateBy);
        repairRecordMapper.insert(record);

        return updated;
    }

    /**
     * 取消报修
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelRepair(Long repairId, String updateBy) {
        ComRepair repair = new ComRepair();
        repair.setRepairId(repairId);
        repair.setStatus("已取消");
        repair.setUpdateBy(updateBy);
        boolean updated = updateById(repair);

        // 创建取消记录
        ComRepairRecord record = new ComRepairRecord();
        record.setRepairId(repairId);
        record.setActionType("取消");
        record.setDescription("报修已取消");
        record.setCreateBy(updateBy);
        repairRecordMapper.insert(record);

        return updated;
    }

    /**
     * 直接修改状态（管理员操作，支持待处理↔处理中↔已完成）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long repairId, String newStatus, String updateBy) {
        ComRepair repair = new ComRepair();
        repair.setRepairId(repairId);
        repair.setStatus(newStatus);
        repair.setUpdateBy(updateBy);

        String actionDesc;
        String actionType;
        if ("处理中".equals(newStatus)) {
            repair.setAcceptTime(new Date());
            actionDesc = "状态变更为：处理中";
            actionType = "状态变更";
        } else if ("已完成".equals(newStatus)) {
            repair.setFinishTime(new Date());
            actionDesc = "状态变更为：已完成";
            actionType = "状态变更";
        } else if ("待处理".equals(newStatus)) {
            actionDesc = "状态回退为：待处理";
            actionType = "状态变更";
        } else {
            actionDesc = "状态更新为" + newStatus;
            actionType = "状态变更";
        }

        boolean updated = updateById(repair);

        // 创建操作记录
        ComRepairRecord record = new ComRepairRecord();
        record.setRepairId(repairId);
        record.setActionType(actionType);
        record.setDescription(actionDesc);
        record.setCreateBy(updateBy);
        repairRecordMapper.insert(record);

        return updated;
    }

    /**
     * 填充超时信息（计算字段，不持久化）
     * 一般工单48h未完成、紧急工单6h未完成视为超时
     */
    @Override
    public void populateTimeoutInfo(ComRepair repair) {
        if (repair == null || repair.getCreateTime() == null) return;
        // 只有未完成的工单才计算超时
        if ("已完成".equals(repair.getStatus()) || "已取消".equals(repair.getStatus())) {
            repair.setElapsedHours(0L);
            repair.setTimeoutWarning(false);
            return;
        }
        long elapsed = System.currentTimeMillis() - repair.getCreateTime().getTime();
        long elapsedHours = elapsed / (1000 * 60 * 60);
        repair.setElapsedHours(elapsedHours);

        boolean isUrgent = "紧急".equals(repair.getUrgency());
        long threshold = isUrgent ? 6 : 48;
        repair.setTimeoutWarning(elapsedHours > threshold);
    }

    /**
     * 批量填充超时信息
     */
    @Override
    public void populateTimeoutInfo(java.util.List<ComRepair> repairList) {
        if (repairList != null) {
            repairList.forEach(this::populateTimeoutInfo);
        }
    }
}
