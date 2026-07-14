package com.zsc.module.convenience.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.common.annotation.Anonymous;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.page.TableDataInfo;
import com.zsc.module.convenience.domain.ComServiceItem;
import com.zsc.module.convenience.domain.ComServiceProvider;
import com.zsc.module.convenience.domain.ComServiceReview;
import com.zsc.module.convenience.domain.vo.ProviderDetailVO;
import com.zsc.module.convenience.domain.vo.ProviderPortalVO;
import com.zsc.module.convenience.domain.vo.ServiceItemVO;
import com.zsc.module.convenience.service.IComServiceItemService;
import com.zsc.module.convenience.service.IComServiceProviderService;
import com.zsc.module.convenience.service.IComServiceReviewService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 居民端 - 便民服务 / 周边商家展示 Portal
 *
 * @author zsc
 */
@RestController
@RequestMapping("/com/convenience/portal")
@Anonymous
public class ComConveniencePortalController extends BaseController {

    @Autowired
    private IComServiceProviderService providerService;

    @Autowired
    private IComServiceItemService itemService;

    @Autowired
    private IComServiceReviewService reviewService;

    /**
     * 居民端查询服务商列表
     * <p>
     * 支持按服务类型筛选、分页。
     * 返回字段包含：服务商名称、地址、电话、评分（评价表平均分）、服务项目数量、优惠信息。
     */
    @GetMapping("/provider/list")
    public TableDataInfo providerList(@RequestParam(required = false) String serviceType) {
        // 1. 分页查询启用的服务商
        Page<ComServiceProvider> page = startPage();
        providerService.lambdaQuery()
                .eq(ComServiceProvider::getStatus, "0")
                .like(serviceType != null && !serviceType.isEmpty(),
                        ComServiceProvider::getServiceType, serviceType)
                .orderByDesc(ComServiceProvider::getCreateTime)
                .page(page);
        clearPage(); // 清除分页线程变量，防止影响后续非分页查询

        List<ComServiceProvider> providers = page.getRecords();
        if (providers.isEmpty()) {
            Page<ProviderPortalVO> emptyPage = new Page<>(page.getCurrent(), page.getSize(), 0);
            return getDataTable((List<?>) emptyPage);
        }

        // 2. 收集服务商ID
        List<Long> providerIds = providers.stream()
                .map(ComServiceProvider::getProviderId)
                .collect(Collectors.toList());

        // 3. 批量统计每个服务商已启用的服务项目数量
        Map<Long, Long> itemCountMap = new HashMap<>();
        List<ComServiceItem> allItems = itemService.lambdaQuery()
                .in(ComServiceItem::getProviderId, providerIds)
                .eq(ComServiceItem::getStatus, "0")
                .select(ComServiceItem::getProviderId)
                .list();
        for (ComServiceItem item : allItems) {
            itemCountMap.merge(item.getProviderId(), 1L, Long::sum);
        }

        // 4. 批量计算每个服务商平均评分
        Map<Long, Double> avgRatingMap = new HashMap<>();
        List<ComServiceReview> allReviews = reviewService.lambdaQuery()
                .in(ComServiceReview::getProviderId, providerIds)
                .select(ComServiceReview::getProviderId, ComServiceReview::getRating)
                .list();
        Map<Long, List<Integer>> ratingGroups = allReviews.stream()
                .collect(Collectors.groupingBy(
                        ComServiceReview::getProviderId,
                        Collectors.mapping(ComServiceReview::getRating, Collectors.toList())
                ));
        for (Map.Entry<Long, List<Integer>> entry : ratingGroups.entrySet()) {
            double avg = entry.getValue().stream()
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
            avgRatingMap.put(entry.getKey(), Math.round(avg * 10) / 10.0);
        }

        // 5. 组装 VO
        List<ProviderPortalVO> voList = providers.stream().map(p -> {
            ProviderPortalVO vo = new ProviderPortalVO();
            BeanUtils.copyProperties(p, vo);
            vo.setServiceItemCount(itemCountMap.getOrDefault(p.getProviderId(), 0L));
            vo.setAvgRating(avgRatingMap.get(p.getProviderId()));
            return vo;
        }).collect(Collectors.toList());

        return getDataTable(voList);
    }

    /**
     * 居民端查询服务商详情
     * <p>
     * 返回服务商完整信息 + 关联的所有服务项目列表 + 平均评分。
     */
    @GetMapping("/provider/{providerId}")
    public AjaxResult providerDetail(@PathVariable Long providerId) {
        // 1. 查询服务商
        ComServiceProvider provider = providerService.getById(providerId);
        if (provider == null) {
            return error("服务商不存在");
        }

        // 2. 计算平均评分
        Double avgRating = null;
        List<ComServiceReview> reviews = reviewService.lambdaQuery()
                .eq(ComServiceReview::getProviderId, providerId)
                .select(ComServiceReview::getRating)
                .list();
        if (!reviews.isEmpty()) {
            avgRating = Math.round(reviews.stream()
                    .map(ComServiceReview::getRating)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0) * 10) / 10.0;
        }

        // 3. 查询启用的服务项目列表
        List<ComServiceItem> items = itemService.lambdaQuery()
                .eq(ComServiceItem::getProviderId, providerId)
                .eq(ComServiceItem::getStatus, "0")
                .orderByAsc(ComServiceItem::getCreateTime)
                .list();

        // 4. 组装详情 VO
        ProviderDetailVO detail = new ProviderDetailVO();
        BeanUtils.copyProperties(provider, detail);
        detail.setAvgRating(avgRating);
        detail.setItems(items.stream().map(item -> {
            ServiceItemVO itemVO = new ServiceItemVO();
            BeanUtils.copyProperties(item, itemVO);
            return itemVO;
        }).collect(Collectors.toList()));

        return success(detail);
    }
}
