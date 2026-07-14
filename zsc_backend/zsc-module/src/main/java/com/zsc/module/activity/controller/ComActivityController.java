package com.zsc.module.activity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.page.TableDataInfo;
import com.zsc.module.activity.domain.*;
import com.zsc.module.activity.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 社区活动管理 Controller
 *
 * @author zsc
 */
@RestController
@RequestMapping("/com/activity")
public class ComActivityController extends BaseController {

    @Autowired
    private IComActivityService activityService;

    @Autowired
    private IComActivitySignupService signupService;

    // ==================== 活动 ====================
    @PreAuthorize("@ss.hasPermi('com:activity:list')")
    @GetMapping("/list")
    public TableDataInfo list(ComActivity activity) {
        Page<ComActivity> page = startPage();
        List<ComActivity> list = activityService.lambdaQuery()
                .like(activity.getTitle() != null, ComActivity::getTitle, activity.getTitle())
                .eq(activity.getActivityType() != null, ComActivity::getActivityType, activity.getActivityType())
                .eq(activity.getStatus() != null, ComActivity::getStatus, activity.getStatus())
                .orderByDesc(ComActivity::getIsTop)
                .orderByDesc(ComActivity::getCreateTime)
                .page(page).getRecords();
        // 从报名记录实时统计人数
        list.forEach(a -> {
            long c = signupService.lambdaQuery().eq(ComActivitySignup::getActivityId, a.getActivityId()).count();
            a.setActualParticipants((int) c);
        });
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:activity:query')")
    @GetMapping("/{activityId}")
    public AjaxResult getInfo(@PathVariable Long activityId) {
        return success(activityService.getById(activityId));
    }

    @PreAuthorize("@ss.hasPermi('com:activity:add')")
    @PostMapping
    public AjaxResult add(@RequestBody ComActivity activity) {
        activity.setCreateBy(getUsername());
        activity.setCreateTime(new java.util.Date());
        if (activity.getStatus() == null) activity.setStatus("草稿");
        if (activity.getActualParticipants() == null) activity.setActualParticipants(0);
        return toAjax(activityService.save(activity));
    }

    /** 发布活动（草稿/待审核 → 报名中） */
    @PreAuthorize("@ss.hasPermi('com:activity:edit')")
    @PutMapping("/publish")
    public AjaxResult publish(@RequestBody ComActivity activity) {
        ComActivity db = activityService.getById(activity.getActivityId());
        if (db == null) return error("活动不存在");
        if (!"草稿".equals(db.getStatus()) && !"待审核".equals(db.getStatus())) return error("仅草稿/待审核状态可发布");
        db.setStatus("报名中");
        db.setUpdateBy(getUsername());
        // 生成6位签到码
        if (db.getCheckinCode() == null || db.getCheckinCode().isEmpty()) {
            db.setCheckinCode(String.format("%06d", new java.util.Random().nextInt(1000000)));
        }
        return toAjax(activityService.updateById(db));
    }

    /** 保存活动回顾 */
    @PreAuthorize("@ss.hasPermi('com:activity:edit')")
    @PutMapping("/review")
    public AjaxResult saveReview(@RequestBody ComActivity activity) {
        ComActivity db = activityService.getById(activity.getActivityId());
        if (db == null) return error("活动不存在");
        db.setReview(activity.getReview());
        db.setPhotos(activity.getPhotos());
        db.setUpdateBy(getUsername());
        return toAjax(activityService.updateById(db));
    }

    /** 结束活动 */
    @PreAuthorize("@ss.hasPermi('com:activity:edit')")
    @PutMapping("/finish")
    public AjaxResult finish(@RequestBody ComActivity activity) {
        ComActivity db = activityService.getById(activity.getActivityId());
        if (db == null) return error("活动不存在");
        db.setStatus("已结束");
        db.setUpdateBy(getUsername());
        return toAjax(activityService.updateById(db));
    }

    /** 置顶/取消置顶 */
    @PreAuthorize("@ss.hasPermi('com:activity:edit')")
    @PutMapping("/top")
    public AjaxResult toggleTop(@RequestBody ComActivity activity) {
        ComActivity db = activityService.getById(activity.getActivityId());
        if (db == null) return error("活动不存在");
        db.setIsTop(db.getIsTop() != null && db.getIsTop() == 1 ? 0 : 1);
        activityService.updateById(db);
        return success(db.getIsTop() == 1 ? "已置顶" : "已取消置顶");
    }

    /** 取消活动 */
    @PreAuthorize("@ss.hasPermi('com:activity:edit')")
    @PutMapping("/cancel")
    public AjaxResult cancel(@RequestBody ComActivity activity) {
        ComActivity db = activityService.getById(activity.getActivityId());
        if (db == null) return error("活动不存在");
        db.setStatus("已取消");
        db.setUpdateBy(getUsername());
        return toAjax(activityService.updateById(db));
    }

    @PreAuthorize("@ss.hasPermi('com:activity:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody ComActivity activity) {
        activity.setUpdateBy(getUsername());
        return toAjax(activityService.updateById(activity));
    }

    @PreAuthorize("@ss.hasPermi('com:activity:remove')")
    @DeleteMapping("/{activityIds}")
    public AjaxResult remove(@PathVariable Long[] activityIds) {
        return toAjax(activityService.removeByIds(java.util.Arrays.asList(activityIds)));
    }

    // ==================== 居民浏览已发布活动 ====================
    @GetMapping("/published")
    public TableDataInfo published(ComActivity activity) {
        Page<ComActivity> page = startPage();
        List<ComActivity> list = activityService.lambdaQuery()
                .in(ComActivity::getStatus, "报名中", "已结束")
                .like(activity.getTitle() != null, ComActivity::getTitle, activity.getTitle())
                .eq(activity.getActivityType() != null, ComActivity::getActivityType, activity.getActivityType())
                .orderByDesc(ComActivity::getIsTop)
                .orderByDesc(ComActivity::getCreateTime)
                .page(page).getRecords();
        list.forEach(a -> {
            long c = signupService.lambdaQuery().eq(ComActivitySignup::getActivityId, a.getActivityId()).count();
            a.setActualParticipants((int) c);
        });
        return getDataTable(list);
    }

    // ==================== 活动报名 ====================
    @PreAuthorize("@ss.hasPermi('com:activity:signup:list')")
    @GetMapping("/signup/list")
    public TableDataInfo signupList(ComActivitySignup signup) {
        Page<ComActivitySignup> page = startPage();
        List<ComActivitySignup> list = signupService.lambdaQuery()
                .eq(signup.getActivityId() != null, ComActivitySignup::getActivityId, signup.getActivityId())
                .eq(signup.getUserId() != null, ComActivitySignup::getUserId, signup.getUserId())
                .eq(signup.getStatus() != null, ComActivitySignup::getStatus, signup.getStatus())
                .eq(signup.getAttendStatus() != null, ComActivitySignup::getAttendStatus, signup.getAttendStatus())
                .orderByDesc(ComActivitySignup::getCreateTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:activity:signup:add')")
    @PostMapping("/signup")
    public AjaxResult signupAdd(@RequestBody ComActivitySignup signup) {
        // 校验活动状态
        ComActivity activity = activityService.getById(signup.getActivityId());
        if (activity == null) return error("活动不存在");
        if (!"报名中".equals(activity.getStatus())) return error("该活动暂不接受报名");
        // 校验报名时间窗口
        Date now = new Date();
        if (activity.getSignupStartTime() != null && now.before(activity.getSignupStartTime()))
            return error("报名尚未开始");
        if (activity.getSignupDeadline() != null && now.after(activity.getSignupDeadline()))
            return error("报名已截止");
        // 校验重复报名
        long count = signupService.lambdaQuery()
                .eq(ComActivitySignup::getActivityId, signup.getActivityId())
                .eq(ComActivitySignup::getUserId, getUserId())
                .count();
        if (count > 0) return error("您已报名该活动");
        // 校验人数上限
        if (activity.getMaxParticipants() != null && activity.getMaxParticipants() > 0
                && activity.getActualParticipants() >= activity.getMaxParticipants()) {
            return error("报名人数已满");
        }
        // 报名
        signup.setUserId(getUserId());
        signup.setCreateBy(getUsername());
        signup.setCreateTime(new java.util.Date());
        signup.setStatus("已报名");
        signupService.save(signup);
        // 更新实际人数
        activity.setActualParticipants((activity.getActualParticipants() == null ? 0 : activity.getActualParticipants()) + 1);
        activityService.updateById(activity);
        return success("报名成功");
    }

    /** 取消报名 */
    @DeleteMapping("/signup/{activityId}")
    public AjaxResult signupCancel(@PathVariable Long activityId) {
        ComActivitySignup signup = signupService.lambdaQuery()
                .eq(ComActivitySignup::getActivityId, activityId)
                .eq(ComActivitySignup::getUserId, getUserId())
                .one();
        if (signup == null) return error("未找到报名记录");
        signupService.removeById(signup.getSignupId());
        // 更新实际人数
        ComActivity activity = activityService.getById(activityId);
        if (activity != null) {
            int cur = activity.getActualParticipants() == null ? 0 : activity.getActualParticipants();
            activity.setActualParticipants(Math.max(0, cur - 1));
            activityService.updateById(activity);
        }
        return success("已取消报名");
    }

    /** 我的报名 */
    @GetMapping("/my-signups")
    public AjaxResult mySignups() {
        List<ComActivitySignup> list = signupService.lambdaQuery()
                .eq(ComActivitySignup::getUserId, getUserId())
                .orderByDesc(ComActivitySignup::getCreateTime)
                .list();
        return success(list);
    }

    // ==================== 报名审核 ====================
    /** 批量通过 */
    @PreAuthorize("@ss.hasPermi('com:activity:signup:list')")
    @PutMapping("/signup/approve")
    public AjaxResult batchApprove(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("ids");
        if (ids == null || ids.isEmpty()) return error("请选择记录");
        List<ComActivitySignup> list = signupService.listByIds(ids.stream().map(Long::valueOf).collect(Collectors.toList()));
        list.forEach(s -> s.setStatus("已通过"));
        signupService.updateBatchById(list);
        return success("已通过 " + list.size() + " 条");
    }

    /** 批量拒绝 */
    @PreAuthorize("@ss.hasPermi('com:activity:signup:list')")
    @PutMapping("/signup/reject")
    public AjaxResult batchReject(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("ids");
        String reason = (String) body.get("reason");
        if (ids == null || ids.isEmpty()) return error("请选择记录");
        if (reason == null || reason.trim().isEmpty()) return error("请填写拒绝原因");
        List<ComActivitySignup> list = signupService.listByIds(ids.stream().map(Long::valueOf).collect(Collectors.toList()));
        list.forEach(s -> { s.setStatus("已拒绝"); s.setRejectReason(reason); });
        signupService.updateBatchById(list);
        return success("已拒绝 " + list.size() + " 条");
    }

    /** 导出报名名单Excel */
    @PreAuthorize("@ss.hasPermi('com:activity:signup:list')")
    @GetMapping("/signup/export/{activityId}")
    public void exportSignup(@PathVariable Long activityId, HttpServletResponse response) throws IOException {
        List<ComActivitySignup> list = signupService.lambdaQuery()
                .eq(ComActivitySignup::getActivityId, activityId)
                .orderByAsc(ComActivitySignup::getCreateTime).list();

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("报名名单");
        Row header = sheet.createRow(0);
        String[] titles = {"序号","报名人","状态","报名时间","签到时间","拒绝原因"};
        for (int i = 0; i < titles.length; i++) header.createCell(i).setCellValue(titles[i]);

        int rowIdx = 1;
        for (ComActivitySignup s : list) {
            Row r = sheet.createRow(rowIdx++);
            r.createCell(0).setCellValue(rowIdx - 1);
            r.createCell(1).setCellValue(s.getCreateBy());
            r.createCell(2).setCellValue(s.getStatus());
            r.createCell(3).setCellValue(s.getCreateTime() != null ? s.getCreateTime().toString() : "");
            r.createCell(4).setCellValue(s.getSigninTime() != null ? s.getSigninTime().toString() : "");
            r.createCell(5).setCellValue(s.getRejectReason() != null ? s.getRejectReason() : "");
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("报名名单.xlsx", "UTF-8"));
        wb.write(response.getOutputStream());
        wb.close();
    }

    /** 居民扫码签到 */
    @PostMapping("/checkin")
    public AjaxResult checkin(@RequestBody Map<String, Object> body) {
        String code = (String) body.get("code");
        if (code == null || code.trim().isEmpty()) return error("请输入签到码");
        // 查找活动
        ComActivity activity = activityService.lambdaQuery()
                .eq(ComActivity::getCheckinCode, code.trim())
                .eq(ComActivity::getStatus, "报名中")
                .one();
        if (activity == null) return error("签到码无效或活动未开放");
        // 查找当前用户的报名
        ComActivitySignup signup = signupService.lambdaQuery()
                .eq(ComActivitySignup::getActivityId, activity.getActivityId())
                .eq(ComActivitySignup::getUserId, getUserId())
                .one();
        if (signup == null) return error("您未报名该活动");
        if ("已签到".equals(signup.getAttendStatus())) return error("已签到，无需重复");
        // 签到
        signup.setAttendStatus("已签到");
        signup.setSigninTime(new java.util.Date());
        signup.setSigninMethod("签到码");
        signupService.updateById(signup);
        return success("签到成功 - " + activity.getTitle());
    }

    /** 管理员标记缺席 */
    @PreAuthorize("@ss.hasPermi('com:activity:signup:list')")
    @PutMapping("/signup/absent/{signupId}")
    public AjaxResult markAbsent(@PathVariable Long signupId) {
        ComActivitySignup s = signupService.getById(signupId);
        if (s == null) return error("记录不存在");
        s.setAttendStatus("已缺席");
        signupService.updateById(s);
        return success("已标记缺席");
    }

    /** 签到 */
    @PreAuthorize("@ss.hasPermi('com:activity:signin')")
    @PutMapping("/signin/{signupId}")
    public AjaxResult signin(@PathVariable Long signupId) {
        ComActivitySignup signup = new ComActivitySignup();
        signup.setSignupId(signupId);
        signup.setSigninTime(new java.util.Date());
        signup.setSigninMethod("扫码");
        return toAjax(signupService.updateById(signup));
    }
}
