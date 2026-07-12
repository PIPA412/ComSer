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
        // 查询当天最大编号
        LambdaQueryWrapper<ComRepair> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(ComRepair::getRepairNo, "BX" + datePrefix)
               .orderByDesc(ComRepair::getRepairNo)
               .last("LIMIT 1");
        ComRepair last = getOne(wrapper);
        int seq = 1;
        if (last != null && last.getRepairNo() != null && last.getRepairNo().length() >= 16) {
            try {
                seq = Integer.parseInt(last.getRepairNo().substring(12)) + 1;
            } catch (NumberFormatException e) {
                seq = 1;
            }
        }
        return "BX" + datePrefix + String.format("%04d", seq);
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
        record.setActionType("受理");
        record.setDescription("受理报修单，维修人员已接单");
        record.setCreateBy(updateBy);
        repairRecordMapper.insert(record);

        return updated;
    }

    /**
     * 完工
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean finishRepair(Long repairId, String updateBy) {
        // 更新报修单
        ComRepair repair = new ComRepair();
        repair.setRepairId(repairId);
        repair.setStatus("已完成");
        repair.setFinishTime(new Date());
        repair.setUpdateBy(updateBy);
        boolean updated = updateById(repair);

        // 查询原报修单获取受理人
        ComRepair original = getById(repairId);

        // 创建维修记录
        ComRepairRecord record = new ComRepairRecord();
        record.setRepairId(repairId);
        record.setWorkerId(original != null ? original.getAssigneeId() : null);
        record.setActionType("完工");
        record.setDescription("维修完成");
        record.setCreateBy(updateBy);
        repairRecordMapper.insert(record);

        return updated;
    }

    /**
     * 评价
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rateRepair(Long repairId, Integer rating, String feedback, String updateBy) {
        ComRepair repair = new ComRepair();
        repair.setRepairId(repairId);
        repair.setRating(rating);
        repair.setFeedback(feedback);
        repair.setStatus("已评价");
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
}
