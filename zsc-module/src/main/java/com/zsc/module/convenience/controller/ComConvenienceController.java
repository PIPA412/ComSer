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
 * 便民服务管理 Controller
 *
 * @author zsc
 */
@RestController
@RequestMapping("/com/convenience")
public class ComConvenienceController extends BaseController {

    // ==================== 服务商 ====================
    @Autowired
    private IComServiceProviderService providerService;

    @PreAuthorize("@ss.hasPermi('com:convenience:provider:list')")
    @GetMapping("/provider/list")
    public TableDataInfo providerList(ComServiceProvider provider) {
        Page<ComServiceProvider> page = startPage();
        List<ComServiceProvider> list = providerService.lambdaQuery()
                .like(provider.getProviderName() != null, ComServiceProvider::getProviderName, provider.getProviderName())
                .eq(provider.getServiceType() != null, ComServiceProvider::getServiceType, provider.getServiceType())
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:convenience:provider:query')")
    @GetMapping("/provider/{providerId}")
    public AjaxResult providerGet(@PathVariable Long providerId) {
        return success(providerService.getById(providerId));
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

    // ==================== 服务项目 ====================
    @Autowired
    private IComServiceItemService itemService;

    @PreAuthorize("@ss.hasPermi('com:convenience:item:list')")
    @GetMapping("/item/list")
    public TableDataInfo itemList(ComServiceItem item) {
        Page<ComServiceItem> page = startPage();
        List<ComServiceItem> list = itemService.lambdaQuery()
                .like(item.getItemName() != null, ComServiceItem::getItemName, item.getItemName())
                .eq(item.getProviderId() != null, ComServiceItem::getProviderId, item.getProviderId())
                .page(page).getRecords();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('com:convenience:item:query')")
    @GetMapping("/item/{itemId}")
    public AjaxResult itemGet(@PathVariable Long itemId) {
        return success(itemService.getById(itemId));
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

    // ==================== 服务订单 ====================
    @Autowired
    private IComServiceOrderService orderService;

    @PreAuthorize("@ss.hasPermi('com:convenience:order:list')")
    @GetMapping("/order/list")
    public TableDataInfo orderList(ComServiceOrder order) {
        Page<ComServiceOrder> page = startPage();
        List<ComServiceOrder> list = orderService.lambdaQuery()
                .eq(order.getUserId() != null, ComServiceOrder::getUserId, order.getUserId())
                .eq(order.getStatus() != null, ComServiceOrder::getStatus, order.getStatus())
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
        order.setStatus("待接单");
        // TODO: 生成订单编号
        return toAjax(orderService.save(order));
    }
}
