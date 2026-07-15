package com.zsc.module.fee.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.page.TableDataInfo;
import com.zsc.module.fee.domain.*;
import com.zsc.module.fee.service.*;
import com.zsc.module.property.domain.ComOwner;
import com.zsc.module.property.domain.ComOwnerRoom;
import com.zsc.module.property.domain.ComRoom;
import com.zsc.module.property.service.IComOwnerRoomService;
import com.zsc.module.property.service.IComOwnerService;
import com.zsc.module.property.service.IComRoomService;
import com.zsc.common.utils.EmailUtil;
import com.zsc.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;


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

    // ==================== 房屋列表（用于下拉选择） ====================
    @GetMapping("/room/list")
    public TableDataInfo roomList(ComRoom room) {
        Page<ComRoom> page = startPage();
        List<ComRoom> list = roomService.lambdaQuery()
                .like(room.getRoomNumber() != null, ComRoom::getRoomNumber, room.getRoomNumber())
                .orderByAsc(ComRoom::getRoomId)
                .page(page).getRecords();
        return getDataTable(list);
    }

    // ==================== 账单管理 ====================
    @Autowired
    private IComFeeBillService feeBillService;
    @Autowired
    private IComRoomService roomService;
    @Autowired
    private IComOwnerRoomService ownerRoomService;
    @Autowired
    private IComOwnerService ownerService;
    @Autowired
    private EmailUtil emailUtil;

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

        // 关联查询，填充房间号、业主信息
        for (ComFeeBill b : list) {
            if (b.getRoomId() != null) {
                ComRoom room = roomService.getById(b.getRoomId());
                if (room != null) b.setRoomNumber(room.getRoomNumber());
                ComOwnerRoom or = ownerRoomService.lambdaQuery()
                        .eq(ComOwnerRoom::getRoomId, b.getRoomId())
                        .eq(ComOwnerRoom::getIsCurrent, "1")
                        .one();
                if (or != null && or.getOwnerId() != null) {
                    ComOwner owner = ownerService.getById(or.getOwnerId());
                    if (owner != null) {
                        b.setOwnerName(owner.getOwnerName());
                        b.setOwnerPhone(owner.getPhone());
                    }
                }
            }
            if (b.getItemId() != null) {
                ComFeeItem item = feeItemService.getById(b.getItemId());
                if (item != null) b.setItemName(item.getItemName());
            }
        }
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
    public AjaxResult paymentList(ComFeePayment payment) {
        List<ComFeePayment> list = paymentService.lambdaQuery()
                .eq(payment.getBillId() != null, ComFeePayment::getBillId, payment.getBillId())
                .eq(payment.getStatus() != null, ComFeePayment::getStatus, payment.getStatus())
                .orderByDesc(ComFeePayment::getPayTime)
                .list();
        // 用 Map 包装额外信息
        List<java.util.Map<String, Object>> result = new ArrayList<>();
        for (ComFeePayment p : list) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("paymentId", p.getPaymentId());
            m.put("billId", p.getBillId());
            m.put("amount", p.getAmount());
            m.put("payMethod", p.getPayMethod());
            m.put("transactionNo", p.getTransactionNo());
            m.put("payTime", p.getPayTime());
            m.put("status", p.getStatus());
            // 填充账单信息
            if (p.getBillId() != null) {
                ComFeeBill bill = feeBillService.getById(p.getBillId());
                if (bill != null) {
                    m.put("billNo", bill.getBillNo());
                    m.put("itemName", bill.getItemName() != null ? bill.getItemName() : "（已删除）");
                    m.put("billPeriod", bill.getBillPeriod());
                    if (bill.getRoomId() != null) {
                        ComRoom room = roomService.getById(bill.getRoomId());
                        m.put("roomNumber", room != null ? room.getRoomNumber() : "-");
                        ComOwnerRoom or = ownerRoomService.lambdaQuery()
                                .eq(ComOwnerRoom::getRoomId, bill.getRoomId())
                                .eq(ComOwnerRoom::getIsCurrent, "1")
                                .one();
                        if (or != null) {
                            ComOwner owner = ownerService.getById(or.getOwnerId());
                            m.put("ownerName", owner != null ? owner.getOwnerName() : "-");
                        } else {
                            m.put("ownerName", "-");
                        }
                    } else {
                        m.put("roomNumber", "-");
                        m.put("ownerName", "-");
                    }
                }
            }
            result.add(m);
        }
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('com:fee:payment:add')")
    @PostMapping("/payment")
    public AjaxResult paymentAdd(@RequestBody ComFeePayment payment) {
        payment.setCreateBy(getUsername());
        // TODO: 更新账单状态
        return toAjax(paymentService.save(payment));
    }

    // ==================== 模拟支付 ====================
    @PreAuthorize("@ss.hasPermi('com:fee:pay:mock')")
    @PutMapping("/pay/{billId}")
    public AjaxResult mockPay(@PathVariable Long billId, @RequestBody ComFeePayment payment) {
        ComFeeBill bill = feeBillService.getById(billId);
        if (bill == null) return error("账单不存在");
        if ("已缴".equals(bill.getStatus())) return error("该账单已缴费");

        // 1. 更新账单状态
        bill.setStatus("已缴");
        bill.setPaidAmount(bill.getAmount());
        bill.setPayTime(new Date());
        bill.setPayMethod(payment.getPayMethod());
        feeBillService.updateById(bill);

        // 2. 记录缴费记录
        payment.setBillId(billId);
        payment.setAmount(bill.getAmount());
        payment.setStatus("成功");
        payment.setCreateBy(getUsername());
        paymentService.save(payment);

        return success("缴费成功");
    }

    // ==================== 批量生成账单 ====================
    @PreAuthorize("@ss.hasPermi('com:fee:bill:batch')")
    @PostMapping("/bill/batch")
    public AjaxResult batchGenerate(@RequestBody Map<String, Object> params) {
        Long itemId = Long.valueOf(params.get("itemId").toString());
        String period = (String) params.get("billPeriod");

        ComFeeItem item = feeItemService.getById(itemId);
        if (item == null) return error("费用项目不存在");

        // 获取需要生成账单的房屋列表
        List<Long> roomIds = new ArrayList<>();
        Object roomIdsObj = params.get("roomIds");
        if (roomIdsObj instanceof List && !((List<?>) roomIdsObj).isEmpty()) {
            // 前端传了指定的房屋ID列表
            for (Object id : (List<?>) roomIdsObj) {
                roomIds.add(Long.valueOf(id.toString()));
            }
        } else {
            // 未指定房屋，则给所有当前已关联业主的房屋生成账单
            List<ComOwnerRoom> activeRooms = ownerRoomService.lambdaQuery()
                    .eq(ComOwnerRoom::getIsCurrent, "1").list();
            for (ComOwnerRoom or : activeRooms) {
                if (!roomIds.contains(or.getRoomId())) {
                    roomIds.add(or.getRoomId());
                }
            }
        }

        List<ComFeeBill> bills = new ArrayList<>();
        for (Long roomId : roomIds) {
            // 检查该房屋本月是否已生成过该费用项目的账单（避免重复）
            boolean exists = feeBillService.lambdaQuery()
                    .eq(ComFeeBill::getRoomId, roomId)
                    .eq(ComFeeBill::getItemId, itemId)
                    .eq(ComFeeBill::getBillPeriod, period)
                    .count() > 0;
            if (exists) continue;

            ComFeeBill bill = new ComFeeBill();
            bill.setBillNo("BILL-" + System.currentTimeMillis() + "-" + roomId);
            bill.setRoomId(roomId);
            bill.setItemId(itemId);
            bill.setAmount(item.getUnitPrice());
            bill.setBillPeriod(period);
            bill.setStatus("未缴");
            bill.setCreateBy(getUsername());
            bills.add(bill);
        }
        if (bills.isEmpty()) {
            return success("所有房屋该周期的账单已存在，无需重复生成");
        }
        feeBillService.saveBatch(bills);
        return success("成功生成 " + bills.size() + " 条账单");
    }

    // ==================== 费用统计 ====================
    @PreAuthorize("@ss.hasPermi('com:fee:statistics')")
    @GetMapping("/statistics")
    public AjaxResult statistics(@RequestParam(required = false) String period) {
        // 查询本月数据（默认当前月）
        if (period == null || period.isEmpty()) {
            period = new java.text.SimpleDateFormat("yyyy-MM").format(new Date());
        }

        // 1. 总体数据
        List<ComFeeBill> allBills = feeBillService.lambdaQuery()
                .eq(ComFeeBill::getBillPeriod, period)
                .list();
        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
        java.math.BigDecimal paidAmount = java.math.BigDecimal.ZERO;
        java.math.BigDecimal unpaidAmount = java.math.BigDecimal.ZERO;
        for (ComFeeBill b : allBills) {
            totalAmount = totalAmount.add(b.getAmount() == null ? java.math.BigDecimal.ZERO : b.getAmount());
            if ("已缴".equals(b.getStatus())) {
                paidAmount = paidAmount.add(b.getAmount() == null ? java.math.BigDecimal.ZERO : b.getAmount());
            } else {
                unpaidAmount = unpaidAmount.add(b.getAmount() == null ? java.math.BigDecimal.ZERO : b.getAmount());
            }
        }
        double rate = totalAmount.compareTo(java.math.BigDecimal.ZERO) > 0
                ? paidAmount.multiply(new java.math.BigDecimal(100)).divide(totalAmount, 2, java.math.RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        // 2. 按费项统计
        List<ComFeeItem> allItems = feeItemService.list();
        java.util.List<java.util.Map<String, Object>> itemStats = new java.util.ArrayList<>();
        for (ComFeeItem item : allItems) {
            java.math.BigDecimal itemTotal = java.math.BigDecimal.ZERO;
            java.math.BigDecimal itemPaid = java.math.BigDecimal.ZERO;
            int itemCount = 0;
            int itemPaidCount = 0;
            for (ComFeeBill b : allBills) {
                if (item.getItemId().equals(b.getItemId())) {
                    itemCount++;
                    itemTotal = itemTotal.add(b.getAmount() == null ? java.math.BigDecimal.ZERO : b.getAmount());
                    if ("已缴".equals(b.getStatus())) {
                        itemPaid = itemPaid.add(b.getAmount() == null ? java.math.BigDecimal.ZERO : b.getAmount());
                        itemPaidCount++;
                    }
                }
            }
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("itemId", item.getItemId());
            m.put("itemName", item.getItemName());
            m.put("count", itemCount);
            m.put("paidCount", itemPaidCount);
            m.put("total", itemTotal);
            m.put("paid", itemPaid);
            m.put("unpaid", itemTotal.subtract(itemPaid));
            double itemRate = itemTotal.compareTo(java.math.BigDecimal.ZERO) > 0
                    ? itemPaid.multiply(new java.math.BigDecimal(100)).divide(itemTotal, 2, java.math.RoundingMode.HALF_UP).doubleValue()
                    : 0.0;
            m.put("rate", itemRate);
            itemStats.add(m);
        }

        // 3. 欠费排行（按欠费金额倒序，取前10）
        List<ComFeeBill> overdueBills = feeBillService.lambdaQuery()
                .eq(ComFeeBill::getBillPeriod, period)
                .ne(ComFeeBill::getStatus, "已缴")
                .orderByDesc(ComFeeBill::getAmount)
                .last("LIMIT 10")
                .list();
        java.util.List<java.util.Map<String, Object>> overdueRank = new java.util.ArrayList<>();
        for (ComFeeBill b : overdueBills) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("billNo", b.getBillNo());
            m.put("amount", b.getAmount());
            String ownerName = "-", roomNumber = "-";
            if (b.getRoomId() != null) {
                ComRoom room = roomService.getById(b.getRoomId());
                if (room != null) roomNumber = room.getRoomNumber();
                ComOwnerRoom or = ownerRoomService.lambdaQuery()
                        .eq(ComOwnerRoom::getRoomId, b.getRoomId())
                        .eq(ComOwnerRoom::getIsCurrent, "1")
                        .one();
                if (or != null) {
                    ComOwner owner = ownerService.getById(or.getOwnerId());
                    if (owner != null) ownerName = owner.getOwnerName();
                }
            }
            m.put("ownerName", ownerName);
            m.put("roomNumber", roomNumber);
            overdueRank.add(m);
        }

        // 4. 楼栋统计（按 room_id 关联 com_room.unit_id.building_id 聚合）
        java.util.List<java.util.Map<String, Object>> buildingStats = new java.util.ArrayList<>();
        // 简化处理：先查询所有单元
        com.zsc.module.property.service.IComUnitService unitService = null;
        com.zsc.module.property.service.IComBuildingService buildingService = null;
        try {
            unitService = com.zsc.common.utils.spring.SpringUtils.getBean(com.zsc.module.property.service.IComUnitService.class);
            buildingService = com.zsc.common.utils.spring.SpringUtils.getBean(com.zsc.module.property.service.IComBuildingService.class);
        } catch (Exception e) {}

        // 返回数据
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("period", period);
        result.put("totalAmount", totalAmount);
        result.put("paidAmount", paidAmount);
        result.put("unpaidAmount", unpaidAmount);
        result.put("rate", rate);
        result.put("totalCount", allBills.size());
        result.put("paidCount", allBills.stream().filter(b -> "已缴".equals(b.getStatus())).count());
        result.put("unpaidCount", allBills.stream().filter(b -> !"已缴".equals(b.getStatus())).count());
        result.put("itemStats", itemStats);
        result.put("overdueRank", overdueRank);
        return success(result);
    }

    // ==================== 发送催缴邮件 ====================
    @PreAuthorize("@ss.hasPermi('com:fee:remind')")
    @PostMapping("/remind/{billId}")
    public AjaxResult sendRemind(@PathVariable Long billId) {
        ComFeeBill bill = feeBillService.getById(billId);
        if (bill == null) return error("账单不存在");

        // 通过房屋找到业主
        if (bill.getRoomId() != null) {
            ComOwnerRoom or = ownerRoomService.lambdaQuery()
                    .eq(ComOwnerRoom::getRoomId, bill.getRoomId())
                    .eq(ComOwnerRoom::getIsCurrent, "1")
                    .one();
            if (or != null && or.getOwnerId() != null) {
                ComOwner owner = ownerService.getById(or.getOwnerId());
                if (owner != null && owner.getEmail() != null && !owner.getEmail().isEmpty()) {
                    String dueDate = bill.getDueDate() != null ?
                            new java.text.SimpleDateFormat("yyyy-MM-dd").format(bill.getDueDate()) : "未设置";
                    boolean ok = emailUtil.sendArrearsReminder(
                            owner.getEmail(),
                            owner.getOwnerName(),
                            bill.getBillNo(),
                            bill.getAmount().toString(),
                            dueDate,
                            bill.getBillPeriod()
                    );
                    if (ok) return success("催缴邮件已发送至 " + owner.getEmail());
                    else return error("邮件发送失败，请检查邮箱配置");
                }
            }
        }
        return error("未找到业主邮箱信息，无法发送");
    }

    // ==================== 居民端 - 我的账单 ====================
    @GetMapping("/my/bills")
    public TableDataInfo myBills(ComFeeBill bill) {
        // 根据当前登录用户的userId找到关联的业主
        Long userId = SecurityUtils.getUserId();
        ComOwner owner = null;
        if (userId != null) {
            owner = ownerService.lambdaQuery().eq(ComOwner::getUserId, userId).one();
        }
        if (owner == null) {
            // 兜底：通过用户名查
            String username = getUsername();
            owner = ownerService.lambdaQuery()
                    .and(q -> q.eq(ComOwner::getOwnerName, username).or().eq(ComOwner::getPhone, username))
                    .one();
        }
        if (owner == null) {
            return getDataTable(new ArrayList<>());
        }
        // 找到业主关联的当前有效房屋
        List<ComOwnerRoom> ownerRooms = ownerRoomService.lambdaQuery()
                .eq(ComOwnerRoom::getOwnerId, owner.getOwnerId())
                .eq(ComOwnerRoom::getIsCurrent, "1")
                .list();
        if (ownerRooms.isEmpty()) {
            return getDataTable(new ArrayList<>());
        }
        List<Long> roomIds = ownerRooms.stream().map(ComOwnerRoom::getRoomId).toList();

        Page<ComFeeBill> page = startPage();
        List<ComFeeBill> list = feeBillService.lambdaQuery()
                .in(ComFeeBill::getRoomId, roomIds)
                .eq(bill.getStatus() != null, ComFeeBill::getStatus, bill.getStatus())
                .orderByDesc(ComFeeBill::getCreateTime)
                .page(page).getRecords();
        // 填充关联信息
        for (ComFeeBill b : list) {
            if (b.getItemId() != null) {
                ComFeeItem item = feeItemService.getById(b.getItemId());
                if (item != null) b.setItemName(item.getItemName());
            }
            if (b.getRoomId() != null) {
                ComRoom room = roomService.getById(b.getRoomId());
                if (room != null) b.setRoomNumber(room.getRoomNumber());
            }
        }
        return getDataTable(list);
    }

    // ==================== 居民端 - 我的缴费记录 ====================
    @GetMapping("/my/payments")
    public TableDataInfo myPayments(ComFeePayment payment) {
        // 先找到当前用户关联的房屋
        Long userId = SecurityUtils.getUserId();
        ComOwner owner = null;
        if (userId != null) {
            owner = ownerService.lambdaQuery().eq(ComOwner::getUserId, userId).one();
        }
        if (owner == null) return getDataTable(new ArrayList<>());
        List<ComOwnerRoom> ownerRooms = ownerRoomService.lambdaQuery()
                .eq(ComOwnerRoom::getOwnerId, owner.getOwnerId())
                .eq(ComOwnerRoom::getIsCurrent, "1")
                .list();
        if (ownerRooms.isEmpty()) return getDataTable(new ArrayList<>());
        List<Long> roomIds = ownerRooms.stream().map(ComOwnerRoom::getRoomId).toList();

        // 查到该房屋所有已缴费的账单
        List<Long> billIds = feeBillService.lambdaQuery()
                .in(ComFeeBill::getRoomId, roomIds)
                .eq(ComFeeBill::getStatus, "已缴")
                .list().stream().map(ComFeeBill::getBillId).toList();
        if (billIds.isEmpty()) return getDataTable(new ArrayList<>());

        Page<ComFeePayment> page = startPage();
        List<ComFeePayment> list = paymentService.lambdaQuery()
                .in(ComFeePayment::getBillId, billIds)
                .orderByDesc(ComFeePayment::getPayTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

}
