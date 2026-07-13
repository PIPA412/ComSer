package com.zsc.module.announcement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.page.TableDataInfo;
import com.zsc.common.utils.SecurityUtils;
import com.zsc.module.announcement.domain.*;
import com.zsc.module.announcement.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社区信息发布 Controller
 *
 * @author zsc
 */
@RestController
@RequestMapping("/com/announcement")
public class ComAnnouncementController extends BaseController {

    @Autowired
    private IComAnnouncementService announcementService;

    @Autowired
    private IComAnnouncementReadService readService;

    // ==================== 公告 ====================
    @PreAuthorize("@ss.hasPermi('com:announcement:list')")
    @GetMapping("/list")
    public TableDataInfo list(ComAnnouncement announcement) {
        Page<ComAnnouncement> page = startPage();
        announcementService.lambdaQuery()
                .like(announcement.getTitle() != null, ComAnnouncement::getTitle, announcement.getTitle())
                .eq(announcement.getCategory() != null, ComAnnouncement::getCategory, announcement.getCategory())
                .eq(announcement.getStatus() != null, ComAnnouncement::getStatus, announcement.getStatus())
                .orderByDesc(ComAnnouncement::getCreateTime)
                .page(page);
        TableDataInfo rsp = new TableDataInfo();
        rsp.setCode(com.zsc.common.constant.HttpStatus.SUCCESS);
        rsp.setMsg("查询成功");
        rsp.setRows(page.getRecords());
        rsp.setTotal(page.getTotal());
        return rsp;
    }

    @PreAuthorize("@ss.hasPermi('com:announcement:query')")
    @GetMapping("/{announcementId}")
    public AjaxResult getInfo(@PathVariable Long announcementId) {
        return success(announcementService.getById(announcementId));
    }

    @PreAuthorize("@ss.hasPermi('com:announcement:add')")
    @PostMapping
    public AjaxResult add(@RequestBody ComAnnouncement announcement) {
        String err = announcementService.validate(announcement);
        if (err != null) return error(err);
        announcement.setCreateBy(getUsername());
        announcement.setStatus("草稿");
        announcementService.save(announcement);
        return success(announcement.getAnnouncementId());
    }

    @PreAuthorize("@ss.hasPermi('com:announcement:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody ComAnnouncement announcement) {
        String err = announcementService.validate(announcement);
        if (err != null) return error(err);
        announcement.setUpdateBy(getUsername());
        return toAjax(announcementService.updateById(announcement));
    }

    /** 发布 */
    @PreAuthorize("@ss.hasPermi('com:announcement:publish')")
    @PutMapping("/publish/{announcementId}")
    public AjaxResult publish(@PathVariable Long announcementId) {
        ComAnnouncement announcement = new ComAnnouncement();
        announcement.setAnnouncementId(announcementId);
        announcement.setStatus("已发布");
        announcement.setPublishTime(new java.util.Date());
        announcement.setUpdateBy(getUsername());
        return toAjax(announcementService.updateById(announcement));
    }

    /** 撤回 */
    @PreAuthorize("@ss.hasPermi('com:announcement:publish')")
    @PutMapping("/revoke/{announcementId}")
    public AjaxResult revoke(@PathVariable Long announcementId) {
        ComAnnouncement announcement = new ComAnnouncement();
        announcement.setAnnouncementId(announcementId);
        announcement.setStatus("已撤回");
        announcement.setUpdateBy(getUsername());
        return toAjax(announcementService.updateById(announcement));
    }

    @PreAuthorize("@ss.hasPermi('com:announcement:remove')")
    @DeleteMapping("/{announcementIds}")
    public AjaxResult remove(@PathVariable Long[] announcementIds) {
        return toAjax(announcementService.removeByIds(java.util.Arrays.asList(announcementIds)));
    }

    // ==================== 阅读记录 ====================
    @PreAuthorize("@ss.hasPermi('com:announcement:read:list')")
    @GetMapping("/read/list")
    public TableDataInfo readList(ComAnnouncementRead read) {
        Page<ComAnnouncementRead> page = startPage();
        List<ComAnnouncementRead> list = readService.lambdaQuery()
                .eq(read.getAnnouncementId() != null, ComAnnouncementRead::getAnnouncementId, read.getAnnouncementId())
                .page(page).getRecords();
        return getDataTable(list);
    }

    /** 记录阅读（所有登录用户可调用，点击公告详情时触发，无需额外权限） */
    @PostMapping("/{announcementId}/read")
    public AjaxResult recordRead(@PathVariable Long announcementId) {
        // 校验公告存在且已发布
        ComAnnouncement announcement = announcementService.getById(announcementId);
        if (announcement == null || !"已发布".equals(announcement.getStatus())) {
            return success(); // 静默忽略，不暴露公告是否存在
        }
        Long userId = SecurityUtils.getUserId();
        // 去重：同一用户重复阅读同一公告则更新时间
        ComAnnouncementRead exist = readService.lambdaQuery()
                .eq(ComAnnouncementRead::getAnnouncementId, announcementId)
                .eq(ComAnnouncementRead::getUserId, userId)
                .one();
        if (exist != null) {
            exist.setReadTime(new java.util.Date());
            readService.updateById(exist);
        } else {
            ComAnnouncementRead record = new ComAnnouncementRead();
            record.setAnnouncementId(announcementId);
            record.setUserId(userId);
            record.setReadTime(new java.util.Date());
            readService.save(record);
        }
        return success();
    }

    /** 公告阅读次数统计 */
    @GetMapping("/read/count/{announcementId}")
    public AjaxResult readCount(@PathVariable Long announcementId) {
        long count = readService.lambdaQuery()
                .eq(ComAnnouncementRead::getAnnouncementId, announcementId)
                .count();
        return success(count);
    }

    /** 批量阅读次数统计 */
    @GetMapping("/read/batch-count")
    public AjaxResult batchReadCount(@RequestParam String ids) {
        if (ids == null || ids.isBlank()) {
            return success(new java.util.LinkedHashMap<>());
        }
        java.util.Map<Long, Long> result = new java.util.LinkedHashMap<>();
        for (String idStr : ids.split(",")) {
            try {
                Long announcementId = Long.valueOf(idStr.trim());
                long count = readService.lambdaQuery()
                        .eq(ComAnnouncementRead::getAnnouncementId, announcementId)
                        .count();
                result.put(announcementId, count);
            } catch (NumberFormatException ignored) {
                // 跳过非法ID
            }
        }
        return success(result);
    }

    // ==================== 首页展示（所有登录用户可见） ====================
    @GetMapping("/published")
    public AjaxResult publishedList() {
        List<ComAnnouncement> list = announcementService.lambdaQuery()
                .eq(ComAnnouncement::getStatus, "已发布")
                .and(qw -> qw.isNull(ComAnnouncement::getExpireDate).or().gt(ComAnnouncement::getExpireDate, new java.util.Date()))
                .orderByDesc(ComAnnouncement::getPublishTime)
                .last("limit 6")
                .list();
        return success(list);
    }
}
