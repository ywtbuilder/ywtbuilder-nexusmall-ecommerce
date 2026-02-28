package com.mall.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.marketing.entity.SmsHomeAdvertise;
import com.mall.module.marketing.entity.SmsHomeNewProduct;
import com.mall.module.marketing.entity.SmsHomeRecommendProduct;
import com.mall.module.marketing.entity.SmsHomeRecommendSubject;
import com.mall.module.marketing.mapper.SmsHomeNewProductMapper;
import com.mall.module.marketing.mapper.SmsHomeRecommendProductMapper;
import com.mall.module.marketing.mapper.SmsHomeRecommendSubjectMapper;
import com.mall.module.marketing.service.HomeAdvertiseService;
import com.mall.module.product.entity.PmsProduct;
import com.mall.module.product.entity.PmsProductCategory;
import com.mall.module.product.service.BrandService;
import com.mall.module.product.service.ProductCategoryService;
import com.mall.module.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 首页
 */
@Tag(name = "Home", description = "首页")
@RestController
@RequestMapping("/home")
public class HomeController {

    // 首页内容聚合的商品数量限制（数据库查询后再内存分页）
    private static final int HOME_CONTENT_NEW_PRODUCT_LIMIT = 12;
    private static final int HOME_CONTENT_HOT_PRODUCT_LIMIT = 12;
    private static final int HOME_CONTENT_FALLBACK_LIMIT = 12;
    private static final long HOME_CONTENT_CACHE_TTL_MILLIS = 30_000L;
    private static final long HOME_CONTENT_LITE_CACHE_TTL_MILLIS = 30_000L;

    private final ProductService productService;
    private final ProductCategoryService productCategoryService;
    private final BrandService brandService;
    private final HomeAdvertiseService homeAdvertiseService;
    private final SmsHomeRecommendSubjectMapper recommendSubjectMapper;
    private final SmsHomeNewProductMapper homeNewProductMapper;
    private final SmsHomeRecommendProductMapper homeRecommendProductMapper;
    private volatile CachedHomeContent cachedHomeContent;
    private volatile CachedHomeContent cachedHomeContentLite;

    private static final class CachedHomeContent {
        private final Map<String, Object> payload;
        private final long expireAtMillis;

        private CachedHomeContent(Map<String, Object> payload, long expireAtMillis) {
            this.payload = payload;
            this.expireAtMillis = expireAtMillis;
        }

        private boolean isExpired(long nowMillis) {
            return nowMillis >= expireAtMillis;
        }
    }

    public HomeController(ProductService productService,
                          ProductCategoryService productCategoryService,
                          BrandService brandService,
                          HomeAdvertiseService homeAdvertiseService,
                          SmsHomeRecommendSubjectMapper recommendSubjectMapper,
                          SmsHomeNewProductMapper homeNewProductMapper,
                          SmsHomeRecommendProductMapper homeRecommendProductMapper) {
        this.productService = productService;
        this.productCategoryService = productCategoryService;
        this.brandService = brandService;
        this.homeAdvertiseService = homeAdvertiseService;
        this.recommendSubjectMapper = recommendSubjectMapper;
        this.homeNewProductMapper = homeNewProductMapper;
        this.homeRecommendProductMapper = homeRecommendProductMapper;
    }

    @Operation(summary = "首页内容聚合")
    @GetMapping("/content")
    public CommonResult<Map<String, Object>> content() {
        long nowMillis = System.currentTimeMillis();
        CachedHomeContent cached = cachedHomeContent;
        if (cached != null && !cached.isExpired(nowMillis)) {
            return CommonResult.success(cached.payload);
        }

        synchronized (this) {
            cached = cachedHomeContent;
            nowMillis = System.currentTimeMillis();
            if (cached != null && !cached.isExpired(nowMillis)) {
                return CommonResult.success(cached.payload);
            }
        }

        List<PmsProduct> newProducts = getNewProducts(HOME_CONTENT_NEW_PRODUCT_LIMIT);
        List<PmsProduct> hotProducts = getRecommendProducts(HOME_CONTENT_HOT_PRODUCT_LIMIT);

        if (newProducts.isEmpty() || hotProducts.isEmpty()) {
            List<PmsProduct> fallbackProducts = getFallbackProducts(HOME_CONTENT_FALLBACK_LIMIT);
            if (newProducts.isEmpty()) {
                newProducts = fallbackProducts;
            }
            if (hotProducts.isEmpty()) {
                hotProducts = fallbackProducts;
            }
        }

        List<SmsHomeAdvertise> advertiseList = listVisibleAdvertise();

        Map<String, Object> result = new HashMap<>();
        result.put("advertiseList", advertiseList);
        result.put("brandList", brandService.list(null, 1, 8).getRecords());
        result.put("newProductList", newProducts);
        result.put("hotProductList", hotProducts);
        result.put("subjectList", getRecommendSubjects(4));
        cachedHomeContent = new CachedHomeContent(result, nowMillis + HOME_CONTENT_CACHE_TTL_MILLIS);
        return CommonResult.success(result);
    }

    @Operation(summary = "首页首屏轻量聚合")
    @GetMapping("/content-lite")
    public CommonResult<Map<String, Object>> contentLite() {
        long nowMillis = System.currentTimeMillis();
        CachedHomeContent cached = cachedHomeContentLite;
        if (cached != null && !cached.isExpired(nowMillis)) {
            return CommonResult.success(cached.payload);
        }

        synchronized (this) {
            cached = cachedHomeContentLite;
            nowMillis = System.currentTimeMillis();
            if (cached != null && !cached.isExpired(nowMillis)) {
                return CommonResult.success(cached.payload);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("advertiseList", listVisibleAdvertise());
        cachedHomeContentLite = new CachedHomeContent(result, nowMillis + HOME_CONTENT_LITE_CACHE_TTL_MILLIS);
        return CommonResult.success(result);
    }

    @Operation(summary = "首页商品分类")
    @GetMapping("/productCateList/{parentId}")
    public CommonResult<CommonPage<PmsProductCategory>> productCateList(@PathVariable Long parentId) {
        Page<PmsProductCategory> page = productCategoryService.list(parentId, 1, 100);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "推荐商品列表")
    @GetMapping("/recommendProductList")
    public CommonResult<List<PmsProduct>> recommendProductList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                               @RequestParam(defaultValue = "4") Integer pageSize) {
        List<Long> productIds = homeRecommendProductMapper.selectList(
                        new LambdaQueryWrapper<SmsHomeRecommendProduct>()
                                .eq(SmsHomeRecommendProduct::getRecommendStatus, 1)
                                .orderByAsc(SmsHomeRecommendProduct::getSort))
                .stream()
                .map(SmsHomeRecommendProduct::getProductId)
                .toList();
        List<PmsProduct> visibleProducts = loadVisibleProductsByOrderedIds(productIds);
        return CommonResult.success(page(visibleProducts, pageNum, pageSize));
    }

    @Operation(summary = "热销商品列表")
    @GetMapping("/hotProductList")
    public CommonResult<List<PmsProduct>> hotProductList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                          @RequestParam(defaultValue = "4") Integer pageSize) {
        List<Long> productIds = homeRecommendProductMapper.selectList(
                        new LambdaQueryWrapper<SmsHomeRecommendProduct>()
                                .eq(SmsHomeRecommendProduct::getRecommendStatus, 1)
                                .orderByAsc(SmsHomeRecommendProduct::getSort))
                .stream()
                .map(SmsHomeRecommendProduct::getProductId)
                .toList();
        List<PmsProduct> visibleProducts = loadVisibleProductsByOrderedIds(productIds);
        return CommonResult.success(page(visibleProducts, pageNum, pageSize));
    }

    @Operation(summary = "新品推荐列表")
    @GetMapping("/newProductList")
    public CommonResult<List<PmsProduct>> newProductList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                          @RequestParam(defaultValue = "4") Integer pageSize) {
        List<Long> productIds = homeNewProductMapper.selectList(
                        new LambdaQueryWrapper<SmsHomeNewProduct>()
                                .eq(SmsHomeNewProduct::getRecommendStatus, 1)
                                .orderByAsc(SmsHomeNewProduct::getSort))
                .stream()
                .map(SmsHomeNewProduct::getProductId)
                .toList();
        List<PmsProduct> visibleProducts = loadVisibleProductsByOrderedIds(productIds);
        return CommonResult.success(page(visibleProducts, pageNum, pageSize));
    }

    @Operation(summary = "专题推荐列表")
    @GetMapping("/subjectList")
    public CommonResult<List<SmsHomeRecommendSubject>> subjectList(@RequestParam(required = false) Long cateId,
                                                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                                                    @RequestParam(defaultValue = "4") Integer pageSize) {
        LambdaQueryWrapper<SmsHomeRecommendSubject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsHomeRecommendSubject::getRecommendStatus, 1);
        wrapper.orderByAsc(SmsHomeRecommendSubject::getSort);
        List<SmsHomeRecommendSubject> subjects = recommendSubjectMapper.selectList(wrapper);
        return CommonResult.success(page(subjects, pageNum, pageSize));
    }

    private List<PmsProduct> getNewProducts(int limit) {
        List<Long> ids = homeNewProductMapper.selectList(
                        new LambdaQueryWrapper<SmsHomeNewProduct>()
                                .eq(SmsHomeNewProduct::getRecommendStatus, 1)
                                .orderByAsc(SmsHomeNewProduct::getSort))
                .stream()
                .map(SmsHomeNewProduct::getProductId)
                .toList();
        List<PmsProduct> visible = loadVisibleProductsByOrderedIds(ids);
        return visible.size() <= limit ? visible : visible.subList(0, limit);
    }

    private List<PmsProduct> getRecommendProducts(int limit) {
        List<Long> ids = homeRecommendProductMapper.selectList(
                        new LambdaQueryWrapper<SmsHomeRecommendProduct>()
                                .eq(SmsHomeRecommendProduct::getRecommendStatus, 1)
                                .orderByAsc(SmsHomeRecommendProduct::getSort))
                .stream()
                .map(SmsHomeRecommendProduct::getProductId)
                .toList();
        List<PmsProduct> visible = loadVisibleProductsByOrderedIds(ids);
        return visible.size() <= limit ? visible : visible.subList(0, limit);
    }

    private List<SmsHomeRecommendSubject> getRecommendSubjects(int limit) {
        List<SmsHomeRecommendSubject> subjects = recommendSubjectMapper.selectList(
                new LambdaQueryWrapper<SmsHomeRecommendSubject>()
                        .eq(SmsHomeRecommendSubject::getRecommendStatus, 1)
                        .orderByAsc(SmsHomeRecommendSubject::getSort));
        return subjects.size() <= limit ? subjects : subjects.subList(0, limit);
    }

    private List<PmsProduct> getFallbackProducts(int limit) {
        Page<PmsProduct> page = productService.list(null, null, null, 1, null, 1, limit);
        return page.getRecords();
    }

    private List<SmsHomeAdvertise> listVisibleAdvertise() {
        return homeAdvertiseService.list(null, 1, null, 1, 20)
                .getRecords()
                .stream()
                .filter(item -> Objects.equals(item.getStatus(), 1))
                .toList();
    }

    private List<PmsProduct> loadVisibleProductsByOrderedIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        Map<Long, PmsProduct> visibleMap = productService.listByIds(ids).stream()
                .filter(this::isAppVisible)
                .collect(Collectors.toMap(PmsProduct::getId, p -> p, (a, b) -> a));

        return ids.stream()
                .map(visibleMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private boolean isAppVisible(PmsProduct product) {
        return product != null && Integer.valueOf(1).equals(product.getPublishStatus());
    }

    private <T> List<T> page(List<T> data, Integer pageNum, Integer pageSize) {
        if (data == null || data.isEmpty()) {
            return List.of();
        }
        int safePageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int safePageSize = (pageSize == null || pageSize < 1) ? 4 : pageSize;
        int start = (safePageNum - 1) * safePageSize;
        if (start >= data.size()) {
            return List.of();
        }
        int end = Math.min(start + safePageSize, data.size());
        return data.subList(start, end);
    }
}
