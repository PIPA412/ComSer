package com.zsc.module.parking.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.page.TableDataInfo;
import com.zsc.common.utils.SecurityUtils;
import com.zsc.module.fee.domain.ComFeeBill;
import com.zsc.module.parking.domain.*;
import com.zsc.module.parking.service.*;
import com.zsc.module.property.domain.ComOwner;
import com.zsc.module.property.domain.ComOwnerRoom;
import com.zsc.module.property.domain.ComRoom;
import com.zsc.module.property.service.IComOwnerRoomService;
import com.zsc.module.property.service.IComOwnerService;
import com.zsc.module.property.service.IComRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 停车管理 Controller
 *
 * @author zsc
 */
@RestController
@RequestMapping("/com/parking")
public class ComParkingController extends BaseController {

    // ==================== 车位管理 ====================
    @Autowired
    private IComParkingSpotService spotService;

    @PreAuthorize("@ss.hasPermi('com:parking:spot:list')")
    @GetMapping("/spot/list")
    public TableDataInfo spotList(ComParkingSpot spot) {
        Page<ComParkingSpot> page = startPage();
        List<ComParkingSpot> list = spotService.lambdaQuery()
                .like(spot.getSpotCode() != null, ComParkingSpot::getSpotCode, spot.getSpotCode())
                .eq(spot.getSpotType() != null, ComParkingSpot::getSpotType, spot.getSpotType())
                .eq(spot.getStatus() != null, ComParkingSpot::getStatus, spot.getStatus())
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:parking:spot:query')")
    @GetMapping("/spot/{spotId}")
    public AjaxResult spotGet(@PathVariable Long spotId) {
        return success(spotService.getById(spotId));
    }

    @PreAuthorize("@ss.hasPermi('com:parking:spot:add')")
    @PostMapping("/spot")
    public AjaxResult spotAdd(@RequestBody ComParkingSpot spot) {
        spot.setCreateBy(getUsername());
        return toAjax(spotService.save(spot));
    }

    @PreAuthorize("@ss.hasPermi('com:parking:spot:edit')")
    @PutMapping("/spot")
    public AjaxResult spotEdit(@RequestBody ComParkingSpot spot) {
        spot.setUpdateBy(getUsername());
        return toAjax(spotService.updateById(spot));
    }

    @PreAuthorize("@ss.hasPermi('com:parking:spot:remove')")
    @DeleteMapping("/spot/{spotIds}")
    public AjaxResult spotRemove(@PathVariable Long[] spotIds) {
        return toAjax(spotService.removeByIds(java.util.Arrays.asList(spotIds)));
    }

    // ==================== 车辆管理 ====================
    @Autowired
    private IComVehicleService vehicleService;
    @Autowired
    private IComOwnerService ownerService;
    @Autowired
    private IComOwnerRoomService ownerRoomService;
    @Autowired
    private IComRoomService roomService;

    @PreAuthorize("@ss.hasPermi('com:parking:vehicle:list')")
    @GetMapping("/vehicle/list")
    public TableDataInfo vehicleList(ComVehicle vehicle) {
        Page<ComVehicle> page = startPage();
        List<ComVehicle> list = vehicleService.lambdaQuery()
                .like(vehicle.getPlateNumber() != null, ComVehicle::getPlateNumber, vehicle.getPlateNumber())
                .eq(vehicle.getOwnerId() != null, ComVehicle::getOwnerId, vehicle.getOwnerId())
                .page(page).getRecords();
        // 填充车主信息、车位编号
        for (ComVehicle v : list) {
            if (v.getOwnerId() != null) {
                ComOwner owner = ownerService.getById(v.getOwnerId());
                if (owner != null) {
                    v.setOwnerName(owner.getOwnerName());
                    v.setOwnerPhone(owner.getPhone());
                    // 查业主当前关联的房屋
                    ComOwnerRoom or = ownerRoomService.lambdaQuery()
                            .eq(ComOwnerRoom::getOwnerId, owner.getOwnerId())
                            .eq(ComOwnerRoom::getIsCurrent, "1")
                            .one();
                    if (or != null) {
                        ComRoom room = roomService.getById(or.getRoomId());
                        if (room != null) v.setRoomNumber(room.getRoomNumber());
                    }
                }
            }
            if (v.getSpotId() != null) {
                ComParkingSpot spot = spotService.getById(v.getSpotId());
                if (spot != null) v.setSpotCode(spot.getSpotCode());
            }
        }
        return getDataTable(list);
    }

    // 业主列表（用于车辆表单选择车主）
    @GetMapping("/owner/list")
    public TableDataInfo ownerList(ComOwner owner) {
        Page<ComOwner> page = startPage();
        List<ComOwner> list = ownerService.lambdaQuery()
                .like(owner.getOwnerName() != null, ComOwner::getOwnerName, owner.getOwnerName())
                .orderByDesc(ComOwner::getOwnerId)
                .page(page).getRecords();
        return getDataTable(list);
    }

    // ==================== 居民端 - 我的车辆 ====================
    @GetMapping("/my/vehicles")
    public TableDataInfo myVehicles() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) return getDataTable(new ArrayList<>());
        ComOwner owner = ownerService.lambdaQuery().eq(ComOwner::getUserId, userId).one();
        if (owner == null) return getDataTable(new ArrayList<>());

        Page<ComVehicle> page = startPage();
        List<ComVehicle> list = vehicleService.lambdaQuery()
                .eq(ComVehicle::getOwnerId, owner.getOwnerId())
                .page(page).getRecords();
        return getDataTable(list);
    }

    // ==================== 居民端 - 我的停车记录 ====================
    @GetMapping("/my/records")
    public TableDataInfo myRecords() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) return getDataTable(new ArrayList<>());
        ComOwner owner = ownerService.lambdaQuery().eq(ComOwner::getUserId, userId).one();
        if (owner == null) return getDataTable(new ArrayList<>());

        // 获取该用户的所有车辆ID
        List<ComVehicle> vehicles = vehicleService.lambdaQuery()
                .eq(ComVehicle::getOwnerId, owner.getOwnerId()).list();
        if (vehicles.isEmpty()) return getDataTable(new ArrayList<>());
        List<Long> vehicleIds = vehicles.stream().map(ComVehicle::getVehicleId).collect(Collectors.toList());

        Page<ComParkingRecord> page = startPage();
        List<ComParkingRecord> list = recordService.lambdaQuery()
                .in(ComParkingRecord::getVehicleId, vehicleIds)
                .orderByDesc(ComParkingRecord::getEntryTime)
                .page(page).getRecords();
        // 填充车牌号
        for (ComParkingRecord r : list) {
            ComVehicle v = vehicleService.getById(r.getVehicleId());
            if (v != null) r.setPlateNumber(v.getPlateNumber());
        }
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:parking:vehicle:query')")
    @GetMapping("/vehicle/{vehicleId}")
    public AjaxResult vehicleGet(@PathVariable Long vehicleId) {
        return success(vehicleService.getById(vehicleId));
    }

    @PreAuthorize("@ss.hasPermi('com:parking:vehicle:add')")
    @PostMapping("/vehicle")
    public AjaxResult vehicleAdd(@RequestBody ComVehicle vehicle) {
        // 检查车位是否已被其他车占用
        if (vehicle.getSpotId() != null) {
            ComVehicle exist = vehicleService.lambdaQuery()
                    .eq(ComVehicle::getSpotId, vehicle.getSpotId())
                    .ne(ComVehicle::getStatus, "1")
                    .one();
            if (exist != null) return error("该车位已被车辆 [" + exist.getPlateNumber() + "] 占用，请先解绑");
            // 临时车位不允许被车辆绑定
            ComParkingSpot spot = spotService.getById(vehicle.getSpotId());
            if (spot != null && "临时".equals(spot.getSpotType())) {
                return error("临时车位（" + spot.getSpotCode() + "）不允许被车辆绑定，仅用于临时停车登记");
            }
        }
        vehicle.setCreateBy(getUsername());
        // 如果前端没传 ownerId，自动从当前登录用户获取
        if (vehicle.getOwnerId() == null) {
            Long userId = SecurityUtils.getUserId();
            if (userId != null) {
                ComOwner owner = ownerService.lambdaQuery().eq(ComOwner::getUserId, userId).one();
                if (owner != null) vehicle.setOwnerId(owner.getOwnerId());
            }
        }
        // 如果没传 status，默认为正常
        if (vehicle.getStatus() == null) vehicle.setStatus("0");
        boolean ok = vehicleService.save(vehicle);
        // 同步更新车位状态为"已占用"
        if (ok && vehicle.getSpotId() != null) {
            ComParkingSpot spot = spotService.getById(vehicle.getSpotId());
            if (spot != null && !"已占用".equals(spot.getStatus())) {
                spot.setStatus("已占用");
                spotService.updateById(spot);
            }
        }
        return toAjax(ok);
    }

    @PreAuthorize("@ss.hasPermi('com:parking:vehicle:edit')")
    @PutMapping("/vehicle")
    public AjaxResult vehicleEdit(@RequestBody ComVehicle vehicle) {
        // 修改时也要检查车位唯一性（排除自己）
        if (vehicle.getSpotId() != null) {
            ComVehicle exist = vehicleService.lambdaQuery()
                    .eq(ComVehicle::getSpotId, vehicle.getSpotId())
                    .ne(ComVehicle::getVehicleId, vehicle.getVehicleId())
                    .ne(ComVehicle::getStatus, "1")
                    .one();
            if (exist != null) return error("该车位已被车辆 [" + exist.getPlateNumber() + "] 占用，请先解绑");
            // 临时车位不允许被车辆绑定
            ComParkingSpot spot = spotService.getById(vehicle.getSpotId());
            if (spot != null && "临时".equals(spot.getSpotType())) {
                return error("临时车位（" + spot.getSpotCode() + "）不允许被车辆绑定，仅用于临时停车登记");
            }
        }
        // 记录原来绑定的车位，用于解绑时更新状态
        ComVehicle oldVehicle = vehicleService.getById(vehicle.getVehicleId());
        Long oldSpotId = oldVehicle != null ? oldVehicle.getSpotId() : null;

        vehicle.setUpdateBy(getUsername());
        boolean ok = vehicleService.updateById(vehicle);

        // 同步更新车位状态
        if (ok) {
            // 原车位解绑 → 状态改回"空闲"（只有当没有其他车占用时）
            if (oldSpotId != null && !oldSpotId.equals(vehicle.getSpotId())) {
                ComVehicle stillOccupied = vehicleService.lambdaQuery()
                        .eq(ComVehicle::getSpotId, oldSpotId)
                        .ne(ComVehicle::getVehicleId, vehicle.getVehicleId())
                        .ne(ComVehicle::getStatus, "1")
                        .one();
                if (stillOccupied == null) {
                    ComParkingSpot oldSpot = spotService.getById(oldSpotId);
                    if (oldSpot != null) {
                        oldSpot.setStatus("空闲");
                        spotService.updateById(oldSpot);
                    }
                }
            }
            // 新车位占用 → 状态改为"已占用"
            if (vehicle.getSpotId() != null) {
                ComParkingSpot newSpot = spotService.getById(vehicle.getSpotId());
                if (newSpot != null && !"已占用".equals(newSpot.getStatus())) {
                    newSpot.setStatus("已占用");
                    spotService.updateById(newSpot);
                }
            }
        }
        return toAjax(ok);
    }

    @PreAuthorize("@ss.hasPermi('com:parking:vehicle:remove')")
    @DeleteMapping("/vehicle/{vehicleIds}")
    public AjaxResult vehicleRemove(@PathVariable Long[] vehicleIds) {
        // 先查询要删除的车辆，释放它们占用的车位
        List<ComVehicle> vehicles = vehicleService.listByIds(java.util.Arrays.asList(vehicleIds));
        boolean ok = vehicleService.removeByIds(java.util.Arrays.asList(vehicleIds));
        if (ok) {
            for (ComVehicle v : vehicles) {
                if (v.getSpotId() != null) {
                    // 检查该车位是否还有其他车占用
                    ComVehicle stillOccupied = vehicleService.lambdaQuery()
                            .eq(ComVehicle::getSpotId, v.getSpotId())
                            .ne(ComVehicle::getStatus, "1")
                            .one();
                    if (stillOccupied == null) {
                        ComParkingSpot spot = spotService.getById(v.getSpotId());
                        if (spot != null) {
                            spot.setStatus("空闲");
                            spotService.updateById(spot);
                        }
                    }
                }
            }
        }
        return toAjax(ok);
    }

    // ==================== 停车记录 ====================
    @Autowired
    private IComParkingRecordService recordService;

    @PreAuthorize("@ss.hasPermi('com:parking:record:list')")
    @GetMapping("/record/list")
    public TableDataInfo recordList(ComParkingRecord record) {
        Page<ComParkingRecord> page = startPage();
        List<ComParkingRecord> list = recordService.lambdaQuery()
                .eq(record.getVehicleId() != null, ComParkingRecord::getVehicleId, record.getVehicleId())
                .orderByDesc(ComParkingRecord::getEntryTime)
                .page(page).getRecords();
        // 填充车牌号和车位编号
        for (ComParkingRecord r : list) {
            if (r.getVehicleId() != null) {
                ComVehicle v = vehicleService.getById(r.getVehicleId());
                if (v != null) r.setPlateNumber(v.getPlateNumber());
            }
            if (r.getSpotId() != null) {
                ComParkingSpot s = spotService.getById(r.getSpotId());
                if (s != null) r.setSpotCode(s.getSpotCode());
            }
        }
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:parking:record:add')")
    @PostMapping("/record")
    public AjaxResult recordAdd(@RequestBody Map<String, Object> params) {
        // 支持车牌号 + 车位编号 入参（更直观）
        ComParkingRecord record = new ComParkingRecord();
        if (params.get("entryTime") != null) {
            try {
                record.setEntryTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(params.get("entryTime").toString()));
            } catch (java.text.ParseException e) {
                return error("入场时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
            }
        }
        if (params.get("plateNumber") != null) {
            String plate = params.get("plateNumber").toString();
            ComVehicle v = vehicleService.lambdaQuery().eq(ComVehicle::getPlateNumber, plate).one();
            if (v == null) return error("车牌号不存在：" + plate);
            record.setVehicleId(v.getVehicleId());
        } else if (params.get("vehicleId") != null) {
            record.setVehicleId(Long.valueOf(params.get("vehicleId").toString()));
        }
        if (params.get("spotCode") != null) {
            String code = params.get("spotCode").toString();
            ComParkingSpot s = spotService.lambdaQuery().eq(ComParkingSpot::getSpotCode, code).one();
            if (s == null) return error("车位编号不存在：" + code);
            record.setSpotId(s.getSpotId());
        } else if (params.get("spotId") != null) {
            record.setSpotId(Long.valueOf(params.get("spotId").toString()));
        }
        record.setCreateBy(getUsername());
        if (record.getEntryTime() == null) {
            record.setEntryTime(new Date());
        }
        return toAjax(recordService.save(record));
    }
    // ==================== 出场登记（自动计费） ====================
    @PreAuthorize("@ss.hasPermi('com:parking:record:exit')")
    @PutMapping("/record/exit")
    public AjaxResult recordExit(@RequestBody Map<String, Object> params) {
        Long recordId = Long.valueOf(params.get("recordId").toString());
        ComParkingRecord old = recordService.getById(recordId);
        if (old == null) return error("停车记录不存在");
        if (old.getExitTime() != null) return error("已出场");

        // 解析出场时间（前端发的是字符串）
        Date exitTime = new Date();
        if (params.get("exitTime") != null) {
            try {
                exitTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(params.get("exitTime").toString());
            } catch (java.text.ParseException e) {
                return error("出场时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
            }
        }
        old.setExitTime(exitTime);

        // 判断该车辆是否绑定了固定车位（=月卡车位，已签月卡不计费）
        ComVehicle vehicleForCheck = old.getVehicleId() != null ? vehicleService.getById(old.getVehicleId()) : null;
        boolean isMonthly = vehicleForCheck != null && vehicleForCheck.getSpotId() != null;

        BigDecimal fee = BigDecimal.ZERO;
        if (isMonthly) {
            // 月卡车位：出场不计费
            old.setPayStatus("已缴");  // 月卡已缴，无需缴费
            old.setFee(BigDecimal.ZERO);
        } else {
            // 临时停车：按小时计费
            long millis = exitTime.getTime() - old.getEntryTime().getTime();
            long hours = millis / (1000 * 60 * 60);
            if (millis % (1000 * 60 * 60) > 0) hours += 1;  // 不足1小时按1小时
            if (hours < 1) hours = 1;

            // 从车位表获取小时计费
            BigDecimal hourlyFee = new BigDecimal("5.00");
            if (old.getSpotId() != null) {
                ComParkingSpot spot = spotService.getById(old.getSpotId());
                if (spot != null && spot.getHourlyFee() != null) {
                    hourlyFee = spot.getHourlyFee();
                }
            }
            fee = hourlyFee.multiply(BigDecimal.valueOf(hours));
            old.setFee(fee);
            old.setPayStatus("未缴");
        }
        recordService.updateById(old);

        // 同步生成费用账单（让居民端能缴费）—— 月卡的不用生成
        if (isMonthly || fee.compareTo(BigDecimal.ZERO) == 0) {
            // 月卡车位，跳过账单生成
            return success(old);
        }
        try {
            ComVehicle vehicle = vehicleForCheck;
            if (vehicle != null && vehicle.getOwnerId() != null) {
                ComOwnerRoom or = ownerRoomService.lambdaQuery()
                        .eq(ComOwnerRoom::getOwnerId, vehicle.getOwnerId())
                        .eq(ComOwnerRoom::getIsCurrent, "1")
                        .one();
                if (or != null) {
                    ComFeeBill bill = new ComFeeBill();
                    bill.setBillNo("PARK-" + System.currentTimeMillis());
                    bill.setRoomId(or.getRoomId());
                    bill.setItemId(getOrCreateParkingFeeItem());
                    bill.setAmount(fee);
                    bill.setBillPeriod("停车费");
                    bill.setDueDate(new java.sql.Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L));
                    bill.setStatus("未缴");
                    bill.setCreateBy(getUsername());
                    com.zsc.module.fee.service.IComFeeBillService feeBillService = com.zsc.common.utils.spring.SpringUtils.getBean(com.zsc.module.fee.service.IComFeeBillService.class);
                    if (feeBillService != null) feeBillService.save(bill);
                }
            }
        } catch (Exception e) {
            // 同步账单失败不影响出场登记
            e.printStackTrace();
        }

        return success(old);
    }

    /**
     * 获取或创建"停车费"费用项目
     */
    private Long getOrCreateParkingFeeItem() {
        com.zsc.module.fee.service.IComFeeItemService feeItemService = com.zsc.common.utils.spring.SpringUtils.getBean(com.zsc.module.fee.service.IComFeeItemService.class);
        if (feeItemService == null) return null;
        com.zsc.module.fee.domain.ComFeeItem item = feeItemService.lambdaQuery()
                .eq(com.zsc.module.fee.domain.ComFeeItem::getItemName, "停车费")
                .one();
        if (item != null) return item.getItemId();
        item = new com.zsc.module.fee.domain.ComFeeItem();
        item.setItemName("停车费");
        item.setChargeType("固定");
        item.setUnitPrice(new BigDecimal("5.00"));
        item.setBillingCycle("次");
        item.setStatus("0");
        item.setCreateBy(getUsername());
        feeItemService.save(item);
        return item.getItemId();
    }

    // ==================== 月卡管理 ====================
    @Autowired
    private IComParkingMonthlyApplyService monthlyApplyService;

    // 居民端：申请月卡
    @PreAuthorize("@ss.hasPermi('com:parking:monthly:renew')")
    @PostMapping("/monthly/apply")
    public AjaxResult monthlyApply(@RequestBody Map<String, Object> params) {
        Long spotId = Long.valueOf(params.get("spotId").toString());
        int months = Integer.parseInt(params.get("months").toString());
        ComParkingSpot spot = spotService.getById(spotId);
        if (spot == null) return error("车位不存在");

        // 找到当前登录用户对应的业主
        Long userId = SecurityUtils.getUserId();
        if (userId == null) return error("未登录");
        ComOwner owner = ownerService.lambdaQuery().eq(ComOwner::getUserId, userId).one();
        if (owner == null) return error("未找到业主信息");

        // 找到该业主名下的车辆
        if (params.get("vehicleId") == null) return error("请选择车辆");
        Long vehicleId = Long.valueOf(params.get("vehicleId").toString());
        ComVehicle vehicle = vehicleService.getById(vehicleId);
        if (vehicle == null) return error("车辆不存在");
        if (vehicle.getOwnerId() == null || !vehicle.getOwnerId().equals(owner.getOwnerId())) return error("该车辆不属于您");
        if (vehicle.getSpotId() != null) return error("该车辆（" + vehicle.getPlateNumber() + "）已绑定固定车位（" + (spotService.getById(vehicle.getSpotId()) != null ? spotService.getById(vehicle.getSpotId()).getSpotCode() : "已删除") + "），不能再申请月卡");
        if (!"0".equals(vehicle.getStatus())) return error("车辆待审核中，请先等审核通过");

        ComParkingMonthlyApply apply = new ComParkingMonthlyApply();
        apply.setOwnerId(owner.getOwnerId());
        apply.setVehicleId(vehicleId);
        apply.setSpotId(spotId);
        apply.setMonths(months);
        apply.setAmount(spot.getMonthlyFee().multiply(BigDecimal.valueOf(months)));
        apply.setStatus("待审核");
        apply.setCreateBy(getUsername());
        monthlyApplyService.save(apply);
        return success("申请已提交，等待管理端审核");
    }

    // 管理端：月卡申请列表
    @PreAuthorize("@ss.hasPermi('com:parking:monthly:renew')")
    @GetMapping("/monthly/list")
    public TableDataInfo monthlyApplyList(ComParkingMonthlyApply q) {
        Page<ComParkingMonthlyApply> page = startPage();
        List<ComParkingMonthlyApply> list = monthlyApplyService.lambdaQuery()
                .eq(q.getStatus() != null, ComParkingMonthlyApply::getStatus, q.getStatus())
                .orderByDesc(ComParkingMonthlyApply::getCreateTime)
                .page(page).getRecords();
        // 填充关联信息
        for (ComParkingMonthlyApply a : list) {
            if (a.getVehicleId() != null) {
                ComVehicle v = vehicleService.getById(a.getVehicleId());
                if (v != null) a.setPlateNumber(v.getPlateNumber());
            }
            if (a.getSpotId() != null) {
                ComParkingSpot s = spotService.getById(a.getSpotId());
                if (s != null) a.setSpotCode(s.getSpotCode());
            }
            if (a.getOwnerId() != null) {
                ComOwner o = ownerService.getById(a.getOwnerId());
                if (o != null) a.setOwnerName(o.getOwnerName());
            }
        }
        return getDataTable(list);
    }

    // 管理端：审核月卡申请
    @PreAuthorize("@ss.hasPermi('com:parking:monthly:renew')")
    @PutMapping("/monthly/approve")
    public AjaxResult monthlyApprove(@RequestBody Map<String, Object> params) {
        Long applyId = Long.valueOf(params.get("applyId").toString());
        boolean approved = "true".equals(params.get("approved")) || Boolean.TRUE.equals(params.get("approved"));
        String type = params.get("type") != null ? params.get("type").toString() : "apply"; // apply=新申请 renew=续费

        ComParkingMonthlyApply apply = monthlyApplyService.getById(applyId);
        if (apply == null) return error("申请不存在");
        if (!"待审核".equals(apply.getStatus())) return error("该申请已处理");

        if (approved) {
            ComVehicle vehicle = vehicleService.getById(apply.getVehicleId());
            if (vehicle == null) return error("车辆不存在");

            if ("renew".equals(type)) {
                // 续费：车辆已有绑定车位，直接续期
                vehicle.setSpotId(apply.getSpotId()); // 保持绑定
                // 记录续费到期时间（当前时间 + 月数）
            } else {
                // 新申请：绑定车辆到车位
                if (vehicle.getSpotId() != null) return error("该车辆已绑定车位");

                // 检查车位是否被占用
                ComVehicle exist = vehicleService.lambdaQuery()
                        .eq(ComVehicle::getSpotId, apply.getSpotId())
                        .ne(ComVehicle::getStatus, "1")
                        .one();
                if (exist != null) return error("该车位已被其他车辆占用");

                vehicle.setSpotId(apply.getSpotId());

                // 车位状态更新为已占用
                ComParkingSpot spot = spotService.getById(apply.getSpotId());
                if (spot != null) {
                    spot.setStatus("已占用");
                    spotService.updateById(spot);
                }
            }
            vehicleService.updateById(vehicle);
            apply.setStatus("已通过");
        } else {
            apply.setStatus("已拒绝");
        }
        apply.setApproveTime(new Date());
        apply.setApproveBy(getUsername());
        monthlyApplyService.updateById(apply);
        return success("审核" + (approved ? "通过" : "拒绝") + "完成");
    }

    // 居民端：我的月卡（已绑定固定车位的车辆）
    @GetMapping("/monthly/my/cards")
    public AjaxResult monthlyMyCards() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) return success(new ArrayList<>());
        ComOwner owner = ownerService.lambdaQuery().eq(ComOwner::getUserId, userId).one();
        if (owner == null) return success(new ArrayList<>());

        List<ComVehicle> vehicles = vehicleService.lambdaQuery()
                .eq(ComVehicle::getOwnerId, owner.getOwnerId())
                .isNotNull(ComVehicle::getSpotId)
                .eq(ComVehicle::getStatus, "0")
                .list();
        List<java.util.Map<String, Object>> result = new ArrayList<>();
        for (ComVehicle v : vehicles) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("vehicleId", v.getVehicleId());
            m.put("plateNumber", v.getPlateNumber());
            m.put("brand", v.getBrand());
            m.put("vehicleType", v.getVehicleType());
            m.put("color", v.getColor());
            if (v.getSpotId() != null) {
                ComParkingSpot spot = spotService.getById(v.getSpotId());
                if (spot != null) {
                    m.put("spotCode", spot.getSpotCode());
                    m.put("spotLocation", spot.getLocation());
                    m.put("monthlyFee", spot.getMonthlyFee());
                }
            }
            // 最近一次通过审核的申请记录作为到期时间参考
            ComParkingMonthlyApply last = monthlyApplyService.lambdaQuery()
                    .eq(ComParkingMonthlyApply::getVehicleId, v.getVehicleId())
                    .eq(ComParkingMonthlyApply::getStatus, "已通过")
                    .orderByDesc(ComParkingMonthlyApply::getCreateTime)
                    .last("LIMIT 1")
                    .one();
            if (last != null) {
                m.put("months", last.getMonths());
                m.put("applyTime", last.getCreateTime());
                // 粗略计算到期日（拿 createTime 加上 N 个月）
                try {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    java.util.Date base = last.getCreateTime() != null ? last.getCreateTime() : new Date();
                    cal.setTime(base);
                    cal.add(java.util.Calendar.MONTH, last.getMonths() != null ? last.getMonths() : 1);
                    m.put("expireDate", new java.text.SimpleDateFormat("yyyy-MM-dd").format(cal.getTime()));
                } catch (Exception e) {
                    m.put("expireDate", "-");
                }
            } else {
                m.put("months", 1);
                m.put("expireDate", "-");
            }
            result.add(m);
        }
        return success(result);
    }

    // 居民端：续费申请
    @PreAuthorize("@ss.hasPermi('com:parking:monthly:renew')")
    @PostMapping("/monthly/renew")
    public AjaxResult monthlyRenew(@RequestBody Map<String, Object> params) {
        Long vehicleId = Long.valueOf(params.get("vehicleId").toString());
        int months = Integer.parseInt(params.get("months").toString());

        Long userId = SecurityUtils.getUserId();
        ComOwner owner = ownerService.lambdaQuery().eq(ComOwner::getUserId, userId).one();
        if (owner == null) return error("未找到业主信息");

        ComVehicle vehicle = vehicleService.getById(vehicleId);
        if (vehicle == null) return error("车辆不存在");
        if (vehicle.getSpotId() == null) return error("该车辆未办理月卡，请先申请");
        if (!owner.getOwnerId().equals(vehicle.getOwnerId())) return error("该车辆不属于您");

        ComParkingSpot spot = spotService.getById(vehicle.getSpotId());
        if (spot == null) return error("绑定车位不存在");

        ComParkingMonthlyApply apply = new ComParkingMonthlyApply();
        apply.setOwnerId(owner.getOwnerId());
        apply.setVehicleId(vehicleId);
        apply.setSpotId(vehicle.getSpotId());
        apply.setMonths(months);
        apply.setAmount(spot.getMonthlyFee().multiply(BigDecimal.valueOf(months)));
        apply.setStatus("待审核");
        apply.setCreateBy(getUsername());
        apply.setRemark("续费");
        monthlyApplyService.save(apply);
        return success("续费申请已提交，等待管理端审核");
    }

    // 居民端：我的月卡申请记录
    @GetMapping("/monthly/my")
    public TableDataInfo monthlyMy() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) return getDataTable(new ArrayList<>());
        ComOwner owner = ownerService.lambdaQuery().eq(ComOwner::getUserId, userId).one();
        if (owner == null) return getDataTable(new ArrayList<>());

        Page<ComParkingMonthlyApply> page = startPage();
        List<ComParkingMonthlyApply> list = monthlyApplyService.lambdaQuery()
                .eq(ComParkingMonthlyApply::getOwnerId, owner.getOwnerId())
                .orderByDesc(ComParkingMonthlyApply::getCreateTime)
                .page(page).getRecords();
        for (ComParkingMonthlyApply a : list) {
            if (a.getVehicleId() != null) {
                ComVehicle v = vehicleService.getById(a.getVehicleId());
                if (v != null) a.setPlateNumber(v.getPlateNumber());
            }
            if (a.getSpotId() != null) {
                ComParkingSpot s = spotService.getById(a.getSpotId());
                if (s != null) a.setSpotCode(s.getSpotCode());
            }
        }
        return getDataTable(list);
    }

}
