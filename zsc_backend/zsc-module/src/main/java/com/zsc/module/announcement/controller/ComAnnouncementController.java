package com.zsc.module.announcement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.page.TableDataInfo;
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
        List<ComAnnouncement> list = announcementService.lambdaQuery()
                .like(announcement.getTitle() != null, ComAnnouncement::getTitle, announcement.getTitle())
                .eq(announcement.getCategory() != null, ComAnnouncement::getCategory, announcement.getCategory())
                .eq(announcement.getStatus() != null, ComAnnouncement::getStatus, announcement.getStatus())
                .orderByDesc(ComAnnouncement::getCreateTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:announcement:query')")
    @GetMapping("/{announcementId}")
    public AjaxResult getInfo(@PathVariable Long announcementId) {
        return success(announcementService.getById(announcementId));
    }

    @PreAuthorize("@ss.hasPermi('com:announcement:add')")
    @PostMapping
    public AjaxResult add(@RequestBody ComAnnouncement announcement) {
        announcement.setCreateBy(getUsername());
        announcement.setStatus("草稿");
        return toAjax(announcementService.save(announcement));
    }

    @PreAuthorize("@ss.hasPermi('com:announcement:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody ComAnnouncement announcement) {
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
        // TODO: 推送通知
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
}
