package com.zsc.module.fee.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.page.TableDataInfo;
import com.zsc.module.fee.domain.*;
import com.zsc.module.fee.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 费用管理 Controller
 *
 * @author zsc
 */
@RestController
@RequestMapping("/com/fee")
public class ComFeeController extends BaseController {

    // ==================== 费用项目 ====================
    @Autowired
    private IComFeeItemService feeItemService;

    @PreAuthorize("@ss.hasPermi('com:fee:item:list')")
    @GetMapping("/item/list")
    public TableDataInfo itemList(ComFeeItem item) {
        Page<ComFeeItem> page = startPage();
        List<ComFeeItem> list = feeItemService.lambdaQuery()
                .like(item.getItemName() != null, ComFeeItem::getItemName, item.getItemName())
                .eq(item.getChargeType() != null, ComFeeItem::getChargeType, item.getChargeType())
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:fee:item:query')")
    @GetMapping("/item/{itemId}")
    public AjaxResult itemGet(@PathVariable Long itemId) {
        return success(feeItemService.getById(itemId));
    }

    @PreAuthorize("@ss.hasPermi('com:fee:item:add')")
    @PostMapping("/item")
    public AjaxResult itemAdd(@RequestBody ComFeeItem item) {
        item.setCreateBy(getUsername());
        return toAjax(feeItemService.save(item));
    }

    @PreAuthorize("@ss.hasPermi('com:fee:item:edit')")
    @PutMapping("/item")
    public AjaxResult itemEdit(@RequestBody ComFeeItem item) {
        item.setUpdateBy(getUsername());
        return toAjax(feeItemService.updateById(item));
    }

    @PreAuthorize("@ss.hasPermi('com:fee:item:remove')")
    @DeleteMapping("/item/{itemIds}")
    public AjaxResult itemRemove(@PathVariable Long[] itemIds) {
        return toAjax(feeItemService.removeByIds(java.util.Arrays.asList(itemIds)));
    }

    // ==================== 账单管理 ====================
    @Autowired
    private IComFeeBillService feeBillService;

    @PreAuthorize("@ss.hasPermi('com:fee:bill:list')")
    @GetMapping("/bill/list")
    public TableDataInfo billList(ComFeeBill bill) {
        Page<ComFeeBill> page = startPage();
        List<ComFeeBill> list = feeBillService.lambdaQuery()
                .eq(bill.getRoomId() != null, ComFeeBill::getRoomId, bill.getRoomId())
                .eq(bill.getStatus() != null, ComFeeBill::getStatus, bill.getStatus())
                .eq(bill.getBillPeriod() != null, ComFeeBill::getBillPeriod, bill.getBillPeriod())
                .orderByDesc(ComFeeBill::getCreateTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:fee:bill:query')")
    @GetMapping("/bill/{billId}")
    public AjaxResult billGet(@PathVariable Long billId) {
        return success(feeBillService.getById(billId));
    }

    @PreAuthorize("@ss.hasPermi('com:fee:bill:add')")
    @PostMapping("/bill")
    public AjaxResult billAdd(@RequestBody ComFeeBill bill) {
        bill.setCreateBy(getUsername());
        // TODO: 生成账单编号
        return toAjax(feeBillService.save(bill));
    }

    @PreAuthorize("@ss.hasPermi('com:fee:bill:remove')")
    @DeleteMapping("/bill/{billIds}")
    public AjaxResult billRemove(@PathVariable Long[] billIds) {
        return toAjax(feeBillService.removeByIds(java.util.Arrays.asList(billIds)));
    }

    // ==================== 缴费记录 ====================
    @Autowired
    private IComFeePaymentService paymentService;

    @PreAuthorize("@ss.hasPermi('com:fee:payment:list')")
    @GetMapping("/payment/list")
    public TableDataInfo paymentList(ComFeePayment payment) {
        Page<ComFeePayment> page = startPage();
        List<ComFeePayment> list = paymentService.lambdaQuery()
                .eq(payment.getBillId() != null, ComFeePayment::getBillId, payment.getBillId())
                .eq(payment.getStatus() != null, ComFeePayment::getStatus, payment.getStatus())
                .orderByDesc(ComFeePayment::getPayTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:fee:payment:add')")
    @PostMapping("/payment")
    public AjaxResult paymentAdd(@RequestBody ComFeePayment payment) {
        payment.setCreateBy(getUsername());
        // TODO: 更新账单状态
        return toAjax(paymentService.save(payment));
    }
}
