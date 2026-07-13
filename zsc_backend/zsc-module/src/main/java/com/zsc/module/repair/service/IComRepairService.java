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
    boolean finishRepair(Long repairId, String updateBy);

    /**
     * 评价
     */
    boolean rateRepair(Long repairId, Integer rating, String feedback, String updateBy);

    /**
     * 取消报修
     */
    boolean cancelRepair(Long repairId, String updateBy);
}
