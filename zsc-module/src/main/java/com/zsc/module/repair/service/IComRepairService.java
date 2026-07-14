package com.zsc.module.repair.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsc.module.repair.domain.ComRepair;

public interface IComRepairService extends IService<ComRepair> {

    /**
     * 生成报修编号（格式：BX + yyyyMMdd + 4位序号）
     */
    String generateRepairNo();

    /**
     * 受理报修单
     */
    boolean acceptRepair(Long repairId, Long assigneeId, String updateBy);

    /**
     * 完工
     */
    boolean finishRepair(Long repairId, String handleNote, String proofUrls, String updateBy);

    /**
     * 评价
     */
    boolean rateRepair(Long repairId, Integer rating, String feedback, String updateBy);

    /**
     * 取消报修
     */
    boolean cancelRepair(Long repairId, String updateBy);

    /**
     * 直接修改状态（管理员操作）
     * 待处理→处理中：自动记录受理时间
     * 处理中→已完成：自动记录完成时间
     */
    boolean updateStatus(Long repairId, String newStatus, String updateBy);

    /**
     * 填充超时信息到单个报修单（一般48h/紧急6h）
     */
    void populateTimeoutInfo(ComRepair repair);

    /**
     * 批量填充超时信息
     */
    void populateTimeoutInfo(java.util.List<ComRepair> repairList);
}
