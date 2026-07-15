package com.zsc.module.convenience.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.page.TableDataInfo;
import com.zsc.module.convenience.domain.*;
import com.zsc.module.convenience.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 便民服务整合管理 Controller
 *
 * @author zsc
 */
@RestController
@RequestMapping("/com/convenience")
public class ComConvenienceController extends BaseController {

    // ==================== 服务商管理 ====================
    @Autowired
    private IComServiceProviderService providerService;

    @PreAuthorize("@ss.hasPermi('com:convenience:provider:list')")
    @GetMapping("/provider/list")
    public TableDataInfo providerList(ComServiceProvider provider) {
        Page<ComServiceProvider> page = startPage();
        List<ComServiceProvider> list = providerService.lambdaQuery()
                .like(provider.getProviderName() != null, ComServiceProvider::getProviderName, provider.getProviderName())
                .like(provider.getContactName() != null, ComServiceProvider::getContactName, provider.getContactName())
                .like(provider.getContactPhone() != null, ComServiceProvider::getContactPhone, provider.getContactPhone())
                .eq(provider.getStatus() != null, ComServiceProvider::getStatus, provider.getStatus())
                .orderByDesc(ComServiceProvider::getCreateTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:convenience:provider:query')")
    @GetMapping("/provider/{providerId}")
    public AjaxResult providerGet(@PathVariable Long providerId) {
        return success(providerService.getById(providerId));
    }

    /** 获取所有启用的服务商（下拉选择用） */
    @PreAuthorize("@ss.hasPermi('com:convenience:provider:list')")
    @GetMapping("/provider/all")
    public AjaxResult providerAll() {
        List<ComServiceProvider> list = providerService.lambdaQuery()
                .eq(ComServiceProvider::getStatus, "0")
                .list();
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('com:convenience:provider:add')")
    @PostMapping("/provider")
    public AjaxResult providerAdd(@RequestBody ComServiceProvider provider) {
        provider.setCreateBy(getUsername());
        return toAjax(providerService.save(provider));
    }

    @PreAuthorize("@ss.hasPermi('com:convenience:provider:edit')")
    @PutMapping("/provider")
    public AjaxResult providerEdit(@RequestBody ComServiceProvider provider) {
        provider.setUpdateBy(getUsername());
        return toAjax(providerService.updateById(provider));
    }

    @PreAuthorize("@ss.hasPermi('com:convenience:provider:remove')")
    @DeleteMapping("/provider/{providerIds}")
    public AjaxResult providerRemove(@PathVariable Long[] providerIds) {
        return toAjax(providerService.removeByIds(java.util.Arrays.asList(providerIds)));
    }

    // ==================== 服务项目管理 ====================
    @Autowired
    private IComServiceItemService itemService;

    @PreAuthorize("@ss.hasPermi('com:convenience:item:list')")
    @GetMapping("/item/list")
    public TableDataInfo itemList(ComServiceItem item) {
        Page<ComServiceItem> page = startPage();
        List<ComServiceItem> list = itemService.lambdaQuery()
                .like(item.getItemName() != null, ComServiceItem::getItemName, item.getItemName())
                .eq(item.getProviderId() != null, ComServiceItem::getProviderId, item.getProviderId())
                .eq(item.getBookingMethod() != null, ComServiceItem::getBookingMethod, item.getBookingMethod())
                .eq(item.getStatus() != null, ComServiceItem::getStatus, item.getStatus())
                .orderByDesc(ComServiceItem::getCreateTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:convenience:item:query')")
    @GetMapping("/item/{itemId}")
    public AjaxResult itemGet(@PathVariable Long itemId) {
        return success(itemService.getById(itemId));
    }

    /** 根据服务商获取服务项目（下拉选择用） */
    @PreAuthorize("@ss.hasPermi('com:convenience:item:list')")
    @GetMapping("/item/byProvider/{providerId}")
    public AjaxResult itemByProvider(@PathVariable Long providerId) {
        List<ComServiceItem> list = itemService.lambdaQuery()
                .eq(ComServiceItem::getProviderId, providerId)
                .eq(ComServiceItem::getStatus, "0")
                .list();
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('com:convenience:item:add')")
    @PostMapping("/item")
    public AjaxResult itemAdd(@RequestBody ComServiceItem item) {
        item.setCreateBy(getUsername());
        return toAjax(itemService.save(item));
    }

    @PreAuthorize("@ss.hasPermi('com:convenience:item:edit')")
    @PutMapping("/item")
    public AjaxResult itemEdit(@RequestBody ComServiceItem item) {
        item.setUpdateBy(getUsername());
        return toAjax(itemService.updateById(item));
    }

    @PreAuthorize("@ss.hasPermi('com:convenience:item:remove')")
    @DeleteMapping("/item/{itemIds}")
    public AjaxResult itemRemove(@PathVariable Long[] itemIds) {
        return toAjax(itemService.removeByIds(java.util.Arrays.asList(itemIds)));
    }

    // ==================== 预约订单管理 ====================
    @Autowired
    private IComServiceOrderService orderService;

    @PreAuthorize("@ss.hasPermi('com:convenience:order:list')")
    @GetMapping("/order/list")
    public TableDataInfo orderList(ComServiceOrder order) {
        Page<ComServiceOrder> page = startPage();
        List<ComServiceOrder> list = orderService.lambdaQuery()
                .eq(order.getStatus() != null, ComServiceOrder::getStatus, order.getStatus())
                .eq(order.getItemId() != null, ComServiceOrder::getItemId, order.getItemId())
                .eq(order.getUserId() != null, ComServiceOrder::getUserId, order.getUserId())
                .like(order.getOrderNo() != null, ComServiceOrder::getOrderNo, order.getOrderNo())
                .like(order.getContactName() != null, ComServiceOrder::getContactName, order.getContactName())
                .orderByDesc(ComServiceOrder::getCreateTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:convenience:order:query')")
    @GetMapping("/order/{orderId}")
    public AjaxResult orderGet(@PathVariable Long orderId) {
        return success(orderService.getById(orderId));
    }

    @PreAuthorize("@ss.hasPermi('com:convenience:order:add')")
    @PostMapping("/order")
    public AjaxResult orderAdd(@RequestBody ComServiceOrder order) {
        order.setCreateBy(getUsername());
        // 从服务项目自动获取金额
        ComServiceItem item = itemService.getById(order.getItemId());
        if (item != null) {
            order.setAmount(item.getPrice());
        }
        order.setStatus("待接单");
        return toAjax(orderService.save(order));
    }

    /** 接单（待接单 → 已接单） */
    @PreAuthorize("@ss.hasPermi('com:convenience:order:accept')")
    @PutMapping("/order/accept")
    public AjaxResult orderAccept(@RequestBody ComServiceOrder order) {
        ComServiceOrder entity = orderService.getById(order.getOrderId());
        if (entity == null) {
            return error("订单不存在");
        }
        if (!"待接单".equals(entity.getStatus())) {
            return error("当前状态不可接单");
        }
        entity.setStatus("已接单");
        entity.setUpdateBy(getUsername());
        return toAjax(orderService.updateById(entity));
    }

    /** 完成（已接单 → 已完成） */
    @PreAuthorize("@ss.hasPermi('com:convenience:order:complete')")
    @PutMapping("/order/complete")
    public AjaxResult orderComplete(@RequestBody ComServiceOrder order) {
        ComServiceOrder entity = orderService.getById(order.getOrderId());
        if (entity == null) {
            return error("订单不存在");
        }
        if (!"已接单".equals(entity.getStatus())) {
            return error("当前状态不可完成");
        }
        entity.setStatus("已完成");
        entity.setUpdateBy(getUsername());
        return toAjax(orderService.updateById(entity));
    }

    /** 取消（待接单 → 已取消） */
    @PreAuthorize("@ss.hasPermi('com:convenience:order:cancel')")
    @PutMapping("/order/cancel")
    public AjaxResult orderCancel(@RequestBody ComServiceOrder order) {
        ComServiceOrder entity = orderService.getById(order.getOrderId());
        if (entity == null) {
            return error("订单不存在");
        }
        if (!"待接单".equals(entity.getStatus())) {
            return error("当前状态不可取消");
        }
        entity.setStatus("已取消");
        entity.setUpdateBy(getUsername());
        return toAjax(orderService.updateById(entity));
    }

    @PreAuthorize("@ss.hasPermi('com:convenience:order:remove')")
    @DeleteMapping("/order/{orderIds}")
    public AjaxResult orderRemove(@PathVariable Long[] orderIds) {
        return toAjax(orderService.removeByIds(java.util.Arrays.asList(orderIds)));
    }

    // ==================== 服务评价管理 ====================
    @Autowired
    private IComServiceReviewService reviewService;

    @PreAuthorize("@ss.hasPermi('com:convenience:review:list')")
    @GetMapping("/review/list")
    public TableDataInfo reviewList(ComServiceReview review) {
        Page<ComServiceReview> page = startPage();
        List<ComServiceReview> list = reviewService.lambdaQuery()
                .eq(review.getItemId() != null, ComServiceReview::getItemId, review.getItemId())
                .eq(review.getProviderId() != null, ComServiceReview::getProviderId, review.getProviderId())
                .eq(review.getRating() != null, ComServiceReview::getRating, review.getRating())
                .eq(review.getOrderId() != null, ComServiceReview::getOrderId, review.getOrderId())
                .orderByDesc(ComServiceReview::getCreateTime)
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:convenience:review:query')")
    @GetMapping("/review/{reviewId}")
    public AjaxResult reviewGet(@PathVariable Long reviewId) {
        return success(reviewService.getById(reviewId));
    }

    @PreAuthorize("@ss.hasPermi('com:convenience:review:add')")
    @PostMapping("/review")
    public AjaxResult reviewAdd(@RequestBody ComServiceReview review) {
        // 校验评分范围
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            return error("评分范围为1-5星");
        }
        // 校验订单是否存在且已完成
        ComServiceOrder order = orderService.getById(review.getOrderId());
        if (order == null) {
            return error("订单不存在");
        }
        if (!"已完成".equals(order.getStatus())) {
            return error("只有已完成的订单才能评价");
        }
        // 防止重复评价
        long count = reviewService.lambdaQuery()
                .eq(ComServiceReview::getOrderId, review.getOrderId())
                .count();
        if (count > 0) {
            return error("该订单已评价，不可重复评价");
        }
        review.setUserId(order.getUserId());
        review.setItemId(order.getItemId());
        review.setCreateBy(getUsername());
        return toAjax(reviewService.save(review));
    }

    @PreAuthorize("@ss.hasPermi('com:convenience:review:edit')")
    @PutMapping("/review")
    public AjaxResult reviewEdit(@RequestBody ComServiceReview review) {
        if (review.getRating() != null && (review.getRating() < 1 || review.getRating() > 5)) {
            return error("评分范围为1-5星");
        }
        review.setUpdateBy(getUsername());
        return toAjax(reviewService.updateById(review));
    }

    @PreAuthorize("@ss.hasPermi('com:convenience:review:remove')")
    @DeleteMapping("/review/{reviewIds}")
    public AjaxResult reviewRemove(@PathVariable Long[] reviewIds) {
        return toAjax(reviewService.removeByIds(java.util.Arrays.asList(reviewIds)));
    }
}
