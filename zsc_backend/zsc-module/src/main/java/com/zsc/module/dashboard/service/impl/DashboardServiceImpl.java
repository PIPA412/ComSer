package com.zsc.module.dashboard.service.impl;

import com.zsc.module.dashboard.service.IDashboardService;
import com.zsc.module.dashboard.vo.*;
import com.zsc.module.activity.domain.ComActivity;
import com.zsc.module.activity.domain.ComActivitySignup;
import com.zsc.module.activity.service.IComActivityService;
import com.zsc.module.activity.service.IComActivitySignupService;
import com.zsc.module.complaint.domain.ComComplaint;
import com.zsc.module.complaint.service.IComComplaintService;
import com.zsc.module.fee.domain.ComFeeBill;
import com.zsc.module.fee.domain.ComFeeItem;
import com.zsc.module.fee.service.IComFeeBillService;
import com.zsc.module.fee.service.IComFeeItemService;
import com.zsc.module.property.domain.ComBuilding;
import com.zsc.module.property.domain.ComOwner;
import com.zsc.module.property.domain.ComOwnerRoom;
import com.zsc.module.property.domain.ComRoom;
import com.zsc.module.property.domain.ComUnit;
import com.zsc.module.property.service.IComBuildingService;
import com.zsc.module.property.service.IComOwnerRoomService;
import com.zsc.module.property.service.IComOwnerService;
import com.zsc.module.property.service.IComRoomService;
import com.zsc.module.property.service.IComUnitService;
import com.zsc.module.repair.domain.ComRepair;
import com.zsc.module.repair.service.IComRepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements IDashboardService {

    @Autowired private IComBuildingService buildingService;
    @Autowired private IComRoomService roomService;
    @Autowired private IComUnitService unitService;
    @Autowired private IComOwnerService ownerService;
    @Autowired private IComOwnerRoomService ownerRoomService;
    @Autowired private IComRepairService repairService;
    @Autowired private IComFeeBillService feeBillService;
    @Autowired private IComFeeItemService feeItemService;
    @Autowired private IComComplaintService complaintService;
    @Autowired private IComActivityService activityService;
    @Autowired private IComActivitySignupService activitySignupService;

    private final Map<Long, Set<Long>> buildingRoomIdCache = new HashMap<>();

    private Set<Long> getBuildingRoomIds(Long buildingId) {
        if (buildingId == null) return null;
        return buildingRoomIdCache.computeIfAbsent(buildingId, bid -> {
            List<ComUnit> units = unitService.lambdaQuery().eq(ComUnit::getBuildingId, bid).list();
            Set<Long> unitIds = units.stream().map(ComUnit::getUnitId).collect(Collectors.toSet());
            if (unitIds.isEmpty()) return Collections.emptySet();
            return roomService.lambdaQuery().in(ComRoom::getUnitId, unitIds).list().stream()
                    .map(ComRoom::getRoomId).collect(Collectors.toSet());
        });
    }
    private void clearCache() { buildingRoomIdCache.clear(); }

    // ============ 工具方法 ============
    private static String fmtDate(Date d) { return d == null ? "" : new java.text.SimpleDateFormat("yyyy-MM-dd").format(d); }
    private static String fmtMonth(Date d) { return d == null ? "" : new java.text.SimpleDateFormat("yyyy-MM").format(d); }
    private static String fmtYear(Date d) { return d == null ? "" : new java.text.SimpleDateFormat("yyyy").format(d); }
    private static String fmtDateTime(Date d) { return d == null ? "" : new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(d); }
    private static String nvl(String s, String def) { return s == null || s.isEmpty() ? def : s; }
    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private static String monthStr(int offset) { return LocalDate.now().minusMonths(offset).format(DateTimeFormatter.ofPattern("yyyy-MM")); }
    private static boolean startsWith(String s, String prefix) { return s != null && s.startsWith(prefix); }
    private static boolean after(Date d, Date ref) { return d != null && d.after(ref); }
    private static boolean before(Date d, Date ref) { return d != null && d.before(ref); }
    private static BigDecimal pct(long part, long total) { return BigDecimal.valueOf(part * 100.0 / total).setScale(1, RoundingMode.HALF_UP); }
    private static BigDecimal rate(BigDecimal got, BigDecimal rec) { return rec.compareTo(BigDecimal.ZERO) > 0 ? got.multiply(BigDecimal.valueOf(100)).divide(rec, 1, RoundingMode.HALF_UP) : BigDecimal.ZERO; }
    private static Date ago(int days) { return new Date(System.currentTimeMillis() - days * 24L * 3600 * 1000); }
    private static <T> long count(List<T> list, java.util.function.Predicate<T> pred) { return list.stream().filter(pred).count(); }
    private int monthsFromRange(String timeRange) {
        if (timeRange == null) return 12;
        switch (timeRange) { case "1month": return 1; case "3months": return 3; case "6months": return 6; default: return 12; }
    }
    private Date dateFromRange(String timeRange) {
        return ago(monthsFromRange(timeRange) * 30);
    }

    // ==================== (1) 人口概览 ====================
    @Override
    public PopulationPanelVO getPopulationPanel(Long buildingId, String timeRange) {
        clearCache();
        PopulationPanelVO vo = new PopulationPanelVO();
        Set<Long> fIds = getBuildingRoomIds(buildingId);

        List<ComRoom> allRooms = roomService.lambdaQuery().list();
        long totalRooms = fIds == null ? allRooms.size() : allRooms.stream().filter(r -> fIds.contains(r.getRoomId())).count();
        vo.setTotalRooms(totalRooms);

        List<ComOwnerRoom> current = ownerRoomService.lambdaQuery().eq(ComOwnerRoom::getIsCurrent, "1").list();
        Set<Long> occIds = current.stream().map(ComOwnerRoom::getRoomId).collect(Collectors.toSet());
        if (fIds != null) occIds.retainAll(fIds);
        vo.setOccupiedRooms((long) occIds.size());
        vo.setOccupancyRate(totalRooms > 0 ? pct(vo.getOccupiedRooms(), totalRooms) : BigDecimal.ZERO);

        List<ComOwner> owners = ownerService.lambdaQuery().eq(ComOwner::getStatus, "0").list();
        vo.setTotalPopulation((long) owners.size());
        vo.setOwnerCount(owners.stream().filter(o -> "业主".equals(o.getOwnerType())).count());
        vo.setTenantCount(owners.stream().filter(o -> "租户".equals(o.getOwnerType())).count());

        Map<String, Long> gender = new LinkedHashMap<>();
        gender.put("男", owners.stream().filter(o -> "0".equals(o.getSex())).count());
        gender.put("女", owners.stream().filter(o -> "1".equals(o.getSex())).count());
        vo.setGenderDistribution(gender);

        // 年龄分布：从身份证号提取出生日期计算年龄
        Map<String, Long> ageGroups = new LinkedHashMap<>();
        ageGroups.put("0-18岁", 0L);
        ageGroups.put("19-35岁", 0L);
        ageGroups.put("36-50岁", 0L);
        ageGroups.put("51-65岁", 0L);
        ageGroups.put("65岁以上", 0L);
        int currentYear = java.time.LocalDate.now().getYear();
        for (ComOwner o : owners) {
            int age = calcAgeFromIdCard(o.getIdCard(), currentYear);
            if (age <= 18) ageGroups.merge("0-18岁", 1L, Long::sum);
            else if (age <= 35) ageGroups.merge("19-35岁", 1L, Long::sum);
            else if (age <= 50) ageGroups.merge("36-50岁", 1L, Long::sum);
            else if (age <= 65) ageGroups.merge("51-65岁", 1L, Long::sum);
            else ageGroups.merge("65岁以上", 1L, Long::sum);
        }
        vo.setAgeDistribution(ageGroups);

        int trendMonths = monthsFromRange(timeRange);
        List<PopulationPanelVO.MonthlyTrend> trends = buildMonthTrends(trendMonths);
        List<ComOwnerRoom> allRel = ownerRoomService.lambdaQuery().list();
        for (ComOwnerRoom r : allRel) {
            String inM = fmtMonth(r.getCheckInDate()), outM = fmtMonth(r.getCheckOutDate());
            for (PopulationPanelVO.MonthlyTrend t : trends) {
                if (inM != null && t.getMonth().compareTo(inM) >= 0) t.setMoveIn(t.getMoveIn() + 1);
                if (outM != null && t.getMonth().equals(outM)) t.setMoveOut(t.getMoveOut() + 1);
            }
        }
        vo.setMoveInOutTrend(trends);

        List<ComBuilding> buildings = buildingService.lambdaQuery().list();
        List<PopulationPanelVO.BuildingOccupancy> bList = new ArrayList<>();
        for (ComBuilding b : buildings) {
            PopulationPanelVO.BuildingOccupancy bo = new PopulationPanelVO.BuildingOccupancy();
            bo.setBuildingName(b.getBuildingName());
            Set<Long> bRids = getBuildingRoomIds(b.getBuildingId());
            bo.setTotalRooms((long) bRids.size());
            long bOcc = current.stream().filter(r -> bRids.contains(r.getRoomId())).count();
            bo.setOccupiedRooms(bOcc);
            bo.setOccupancyRate(bRids.isEmpty() ? BigDecimal.ZERO : pct(bOcc, bRids.size()));
            bList.add(bo);
        }
        vo.setBuildingOccupancyList(bList);
        return vo;
    }

    // ==================== (2) 房屋概览 ====================
    @Override
    public RoomPanelVO getRoomPanel(Long buildingId, String timeRange) {
        clearCache();
        Set<Long> fIds = getBuildingRoomIds(buildingId);
        List<ComRoom> rooms = roomService.lambdaQuery().list();
        if (fIds != null) rooms = rooms.stream().filter(r -> fIds.contains(r.getRoomId())).collect(Collectors.toList());
        RoomPanelVO vo = new RoomPanelVO();

        Map<String, Long> statusMap = new LinkedHashMap<>();
        for (String s : new String[]{"空置", "自住", "出租", "未入住"}) statusMap.put(s, 0L);
        for (ComRoom r : rooms) statusMap.merge(nvl(r.getUseStatus(), "空置"), 1L, Long::sum);
        vo.setStatusDistribution(statusMap);

        Map<String, Long> areaMap = new LinkedHashMap<>();
        for (String s : new String[]{"<60㎡", "60-90㎡", "90-120㎡", "120-150㎡", ">150㎡"}) areaMap.put(s, 0L);
        for (ComRoom r : rooms) {
            BigDecimal a = r.getArea(); if (a == null) continue;
            if (a.compareTo(BigDecimal.valueOf(60)) < 0) areaMap.merge("<60㎡", 1L, Long::sum);
            else if (a.compareTo(BigDecimal.valueOf(90)) < 0) areaMap.merge("60-90㎡", 1L, Long::sum);
            else if (a.compareTo(BigDecimal.valueOf(120)) < 0) areaMap.merge("90-120㎡", 1L, Long::sum);
            else if (a.compareTo(BigDecimal.valueOf(150)) < 0) areaMap.merge("120-150㎡", 1L, Long::sum);
            else areaMap.merge(">150㎡", 1L, Long::sum);
        }
        vo.setAreaDistribution(areaMap);

        Map<String, Long> layoutMap = new LinkedHashMap<>();
        for (ComRoom r : rooms) layoutMap.merge(nvl(r.getRoomType(), "住宅"), 1L, Long::sum);
        vo.setLayoutDistribution(layoutMap);
        return vo;
    }

    // ==================== (3) 工单概览 ====================
    @Override
    public RepairPanelVO getRepairPanel(Long buildingId, String timeRange) {
        clearCache();
        List<ComRepair> all = repairService.lambdaQuery().list();
        Set<Long> fIds = getBuildingRoomIds(buildingId);
        if (fIds != null) all = all.stream().filter(r -> fIds.contains(r.getRoomId())).collect(Collectors.toList());

        RepairPanelVO vo = new RepairPanelVO();
        Date now = new Date();
        String today = fmtDate(now);
        int chartMonths = monthsFromRange(timeRange);
        Date rangeStart = dateFromRange(timeRange);

        vo.setTodayNew(count(all, r -> fmtDate(r.getCreateTime()).equals(today)));
        vo.setPendingCount(count(all, r -> "待受理".equals(r.getStatus())));
        vo.setProcessingCount(count(all, r -> "处理中".equals(r.getStatus())));
        vo.setWaitingConfirmCount(count(all, r -> "待确认".equals(r.getStatus())));

        vo.setRepairTypeTop5(all.stream().filter(r -> after(r.getCreateTime(), rangeStart))
                .collect(Collectors.groupingBy(r -> nvl(r.getRepairType(), "其他"), Collectors.counting()))
                .entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(5).collect(Collectors.toList()));

        List<RepairPanelVO.MonthlyTrend> mts = new ArrayList<>();
        for (int i = chartMonths - 1; i >= 0; i--) {
            String m = monthStr(-i);
            RepairPanelVO.MonthlyTrend t = new RepairPanelVO.MonthlyTrend(); t.setMonth(m);
            t.setCount(all.stream().filter(r -> m.equals(fmtMonth(r.getCreateTime()))).count()); mts.add(t);
        }
        vo.setMonthlyTrend(mts);

        // 维修人员效率排名（含超时率）
        long ovMs = 24L * 3600 * 1000;
        Map<Long, List<ComRepair>> byW = all.stream().filter(r -> r.getAssigneeId() != null)
                .collect(Collectors.groupingBy(ComRepair::getAssigneeId));
        List<RepairPanelVO.WorkerRank> ranks = new ArrayList<>();
        for (var e : byW.entrySet()) {
            var wrl = e.getValue();
            RepairPanelVO.WorkerRank wr = new RepairPanelVO.WorkerRank();
            wr.setWorkerName("维修人员#" + e.getKey());
            wr.setCompletedCount(wrl.stream().filter(r -> "已完成".equals(r.getStatus()) || "已评价".equals(r.getStatus())).count());
            wr.setAvgRating(BigDecimal.valueOf(wrl.stream().filter(r -> r.getRating() != null).mapToInt(ComRepair::getRating).average().orElse(0)).setScale(1, RoundingMode.HALF_UP));
            // 超时率 = 超24h未完成的 / 总工单数（已评价算完成，不计入超时）
            long overdue = wrl.stream().filter(r -> r.getCreateTime() != null && !"已完成".equals(r.getStatus()) && !"已评价".equals(r.getStatus()) && !"已取消".equals(r.getStatus()) && now.getTime() - r.getCreateTime().getTime() > ovMs).count();
            wr.setOverdueRate(wrl.isEmpty() ? BigDecimal.ZERO : BigDecimal.valueOf(overdue * 100.0 / wrl.size()).setScale(1, RoundingMode.HALF_UP));
            ranks.add(wr);
        }
        ranks.sort((a, b) -> Long.compare(b.getCompletedCount(), a.getCompletedCount()));
        vo.setWorkerRankList(ranks.stream().limit(10).collect(Collectors.toList()));

        vo.setOverdueList(all.stream()
                .filter(r -> r.getCreateTime() != null && !"已完成".equals(r.getStatus()) && !"已评价".equals(r.getStatus()) && !"已取消".equals(r.getStatus()) && now.getTime() - r.getCreateTime().getTime() > ovMs)
                .limit(20).map(r -> {
                    RepairPanelVO.OverdueItem o = new RepairPanelVO.OverdueItem();
                    o.setRepairNo(r.getRepairNo()); o.setRepairType(r.getRepairType()); o.setUrgency(r.getUrgency());
                    o.setStatus(r.getStatus()); o.setCreateTime(fmtDateTime(r.getCreateTime())); o.setIsOverdue(true);
                    return o;
                }).collect(Collectors.toList()));
        return vo;
    }

    // ==================== (4) 收费概览 ====================
    @Override
    public FeePanelVO getFeePanel(Long buildingId, String timeRange) {
        clearCache();
        List<ComFeeBill> all = feeBillService.lambdaQuery().list();
        Set<Long> fIds = getBuildingRoomIds(buildingId);
        if (fIds != null) all = all.stream().filter(b -> fIds.contains(b.getRoomId())).collect(Collectors.toList());
        FeePanelVO vo = new FeePanelVO();
        String thisMonth = monthStr(0);
        int chartMonths = monthsFromRange(timeRange);

        BigDecimal rec = BigDecimal.ZERO, got = BigDecimal.ZERO, arr = BigDecimal.ZERO;
        for (ComFeeBill b : all) {
            BigDecimal amt = nz(b.getAmount()), paid = nz(b.getPaidAmount());
            if (startsWith(b.getBillPeriod(), thisMonth)) { rec = rec.add(amt); if ("已缴".equals(b.getStatus())) got = got.add(paid); }
            if (!"已缴".equals(b.getStatus())) arr = arr.add(amt.subtract(paid));
        }
        vo.setMonthReceivable(rec); vo.setMonthReceived(got); vo.setTotalArrears(arr);
        vo.setCollectionRate(rate(got, rec));

        List<FeePanelVO.MonthlyRate> mrs = new ArrayList<>();
        for (int i = chartMonths - 1; i >= 0; i--) {
            String m = monthStr(-i); BigDecimal mr = BigDecimal.ZERO, mg = BigDecimal.ZERO;
            for (ComFeeBill b : all) {
                if (startsWith(b.getBillPeriod(), m)) { mr = mr.add(nz(b.getAmount())); if ("已缴".equals(b.getStatus())) mg = mg.add(nz(b.getPaidAmount())); }
            }
            FeePanelVO.MonthlyRate r = new FeePanelVO.MonthlyRate();
            r.setMonth(m.substring(5)); r.setRate(rate(mg, mr)); mrs.add(r);
        }
        vo.setMonthlyTrend(mrs);

        List<ComFeeItem> items = feeItemService.lambdaQuery().list();
        Map<String, BigDecimal> fs = new LinkedHashMap<>();
        for (ComFeeItem it : items) fs.put(it.getItemName(), all.stream().filter(b -> it.getItemId().equals(b.getItemId())).map(b -> nz(b.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setFeeStructure(fs);

        List<ComBuilding> blds = buildingService.lambdaQuery().list();
        List<FeePanelVO.BuildingRate> brs = new ArrayList<>();
        for (ComBuilding b : blds) {
            Set<Long> rIds = getBuildingRoomIds(b.getBuildingId());
            BigDecimal br = BigDecimal.ZERO, bg = BigDecimal.ZERO;
            for (ComFeeBill bl : all) {
                if (startsWith(bl.getBillPeriod(), thisMonth) && rIds.contains(bl.getRoomId())) { br = br.add(nz(bl.getAmount())); if ("已缴".equals(bl.getStatus())) bg = bg.add(nz(bl.getPaidAmount())); }
            }
            FeePanelVO.BuildingRate brt = new FeePanelVO.BuildingRate();
            brt.setBuildingName(b.getBuildingName()); brt.setRate(rate(bg, br)); brs.add(brt);
        }
        vo.setBuildingRateList(brs);

        Date d90 = ago(90);
        vo.setArrearsOver90DaysCount(all.stream().filter(b -> !"已缴".equals(b.getStatus()) && before(b.getDueDate(), d90)).count());
        vo.setArrearsOver90DaysAmount(all.stream().filter(b -> !"已缴".equals(b.getStatus()) && before(b.getDueDate(), d90)).map(b -> nz(b.getAmount()).subtract(nz(b.getPaidAmount()))).reduce(BigDecimal.ZERO, BigDecimal::add));
        return vo;
    }

    // ==================== (5) 投诉概览 ====================
    @Override
    public ComplaintPanelVO getComplaintPanel(Long buildingId, String timeRange) {
        ComplaintPanelVO vo = new ComplaintPanelVO();
        List<ComComplaint> all = complaintService.lambdaQuery().list();
        int chartMonths = monthsFromRange(timeRange);
        Date rangeStart = dateFromRange(timeRange);

        vo.setRecentNewCount(all.stream().filter(c -> after(c.getCreateTime(), rangeStart)).count());
        vo.setPendingCount(all.stream().filter(c -> "待受理".equals(c.getStatus())).count());
        // 超时未处理：待受理/处理中且超过48小时未完成
        Date now = new Date();
        long overdueMs = 48L * 3600 * 1000;
        vo.setOverdueCount(all.stream()
                .filter(c -> ("待受理".equals(c.getStatus()) || "处理中".equals(c.getStatus()))
                        && c.getCreateTime() != null
                        && now.getTime() - c.getCreateTime().getTime() > overdueMs)
                .count());
        vo.setTypeDistribution(all.stream().collect(Collectors.groupingBy(c -> nvl(c.getType(), "其他"), LinkedHashMap::new, Collectors.counting())));

        List<ComplaintPanelVO.MonthlySatisfaction> sl = new ArrayList<>();
        for (int i = chartMonths - 1; i >= 0; i--) {
            String m = monthStr(-i);
            ComplaintPanelVO.MonthlySatisfaction s = new ComplaintPanelVO.MonthlySatisfaction(); s.setMonth(m.substring(5));
            List<ComComplaint> mc = all.stream().filter(c -> m.equals(fmtMonth(c.getCreateTime())) && c.getRating() != null).collect(Collectors.toList());
            s.setAvgRating(BigDecimal.valueOf(mc.stream().mapToInt(ComComplaint::getRating).average().orElse(0)).setScale(1, RoundingMode.HALF_UP));
            sl.add(s);
        }
        vo.setSatisfactionTrend(sl);

        // 处理时长：计算每月 finishTime - acceptTime 的平均小时数
        List<ComplaintPanelVO.MonthlyDuration> dl = new ArrayList<>();
        for (int i = chartMonths - 1; i >= 0; i--) {
            String m = monthStr(-i);
            ComplaintPanelVO.MonthlyDuration d = new ComplaintPanelVO.MonthlyDuration(); d.setMonth(m.substring(5));
            List<ComComplaint> mc = all.stream().filter(c -> m.equals(fmtMonth(c.getCreateTime())) && c.getAcceptTime() != null && c.getFinishTime() != null).collect(Collectors.toList());
            double avgH = mc.stream().mapToDouble(c -> (c.getFinishTime().getTime() - c.getAcceptTime().getTime()) / 3600000.0).average().orElse(0);
            d.setAvgHours(BigDecimal.valueOf(avgH).setScale(1, RoundingMode.HALF_UP));
            dl.add(d);
        }
        vo.setDurationTrend(dl);
        return vo;
    }

    // ==================== (6) 活动概览 ====================
    @Override
    public ActivityPanelVO getActivityPanel(Long buildingId, String timeRange) {
        ActivityPanelVO vo = new ActivityPanelVO();
        List<ComActivity> acts = activityService.lambdaQuery().list();
        List<ComActivitySignup> signups = activitySignupService.lambdaQuery().list();
        String thisYear = String.valueOf(LocalDate.now().getYear());
        int chartMonths = monthsFromRange(timeRange);

        vo.setTotalEvents(acts.stream().filter(a -> fmtYear(a.getCreateTime()).equals(thisYear)).count());
        vo.setTotalParticipants(signups.stream().filter(s -> s.getSigninTime() != null).count());
        long signed = signups.stream().filter(s -> s.getSigninTime() != null).count();
        vo.setAvgCheckInRate(signups.isEmpty() ? BigDecimal.ZERO : pct(signed, signups.size()));
        vo.setTypeDistribution(acts.stream().collect(Collectors.groupingBy(a -> nvl(a.getActivityType(), "其他"), LinkedHashMap::new, Collectors.counting())));

        List<ActivityPanelVO.MonthlyCount> mts = new ArrayList<>();
        for (int i = chartMonths - 1; i >= 0; i--) {
            String m = monthStr(-i);
            ActivityPanelVO.MonthlyCount mc = new ActivityPanelVO.MonthlyCount(); mc.setMonth(m);
            mc.setCount(acts.stream().filter(a -> m.equals(fmtMonth(a.getCreateTime()))).count()); mts.add(mc);
        }
        vo.setMonthlyTrend(mts);
        return vo;
    }

    @Override
    public List<DashboardVO.BuildingOption> getBuildingOptions() {
        return buildingService.lambdaQuery().list().stream().map(b -> {
            DashboardVO.BuildingOption o = new DashboardVO.BuildingOption(); o.setBuildingId(b.getBuildingId()); o.setBuildingName(b.getBuildingName()); return o;
        }).collect(Collectors.toList());
    }

    /** 从18位身份证号提取出生年份计算年龄 */
    private static int calcAgeFromIdCard(String idCard, int currentYear) {
        if (idCard == null || idCard.length() < 14) return 0;
        try {
            int birthYear = Integer.parseInt(idCard.substring(6, 10));
            return currentYear - birthYear;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static List<PopulationPanelVO.MonthlyTrend> buildMonthTrends(int months) {
        List<PopulationPanelVO.MonthlyTrend> list = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            PopulationPanelVO.MonthlyTrend t = new PopulationPanelVO.MonthlyTrend(); t.setMonth(monthStr(-i)); t.setMoveIn(0L); t.setMoveOut(0L); list.add(t);
        }
        return list;
    }
}
