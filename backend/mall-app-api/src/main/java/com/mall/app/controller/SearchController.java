package com.mall.app.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.product.entity.PmsProduct;
import com.mall.module.product.service.ProductService;
import com.mall.module.search.entity.EsProduct;
import com.mall.module.search.service.EsProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 商品搜索
 */
@Tag(name = "Search", description = "商品搜索")
@RestController
@RequestMapping("/search")
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);
    private static final int MYSQL_FETCH_LIMIT = 2000;
    private static final int MYSQL_SEARCH_CACHE_TTL_MILLIS = 45_000;
    private static final int MYSQL_SEARCH_CACHE_MAX_ENTRIES = 256;

    private final EsProductService esProductService;
    private final ProductService productService;
    private final ConcurrentHashMap<String, CachedSearchPage> mysqlSearchCache = new ConcurrentHashMap<>();

    private static final class CachedSearchPage {
        private final CommonPage<EsProduct> page;
        private final long expireAtMillis;

        private CachedSearchPage(CommonPage<EsProduct> page, long expireAtMillis) {
            this.page = page;
            this.expireAtMillis = expireAtMillis;
        }

        private boolean isExpired(long nowMillis) {
            return nowMillis >= expireAtMillis;
        }
    }

    public SearchController(EsProductService esProductService,
                            ProductService productService) {
        this.esProductService = esProductService;
        this.productService = productService;
    }

    @Operation(summary = "综合搜索（App 端：MySQL 优先）")
    @GetMapping("/product")
    public CommonResult<CommonPage<EsProduct>> search(@RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) Long brandId,
                                                       @RequestParam(required = false) Long productCategoryId,
                                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                                       @RequestParam(defaultValue = "5") Integer pageSize,
                                                       @RequestParam(defaultValue = "0") Integer sort) {
        int safePageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int safePageSize = (pageSize == null || pageSize < 1) ? 5 : pageSize;
        return CommonResult.success(
                searchFromMysqlWithCache(keyword, brandId, productCategoryId, safePageNum, safePageSize, sort));
    }

    @Operation(summary = "综合搜索（管理/离线：ES 优先，失败回退 MySQL）")
    @GetMapping("/esProduct/search")
    public CommonResult<CommonPage<EsProduct>> searchFromEs(@RequestParam(required = false) String keyword,
                                                             @RequestParam(required = false) Long brandId,
                                                             @RequestParam(required = false) Long productCategoryId,
                                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                                             @RequestParam(defaultValue = "5") Integer pageSize,
                                                             @RequestParam(defaultValue = "0") Integer sort) {
        int safePageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int safePageSize = (pageSize == null || pageSize < 1) ? 5 : pageSize;
        try {
            org.springframework.data.domain.Page<EsProduct> page =
                    esProductService.search(keyword, brandId, productCategoryId, safePageNum, safePageSize, sort);
            if (page == null || page.getTotalElements() <= 0) {
                log.info("[Search] ES returned empty result, fallback to MySQL. keyword={}, brandId={}, categoryId={}",
                        keyword, brandId, productCategoryId);
                return CommonResult.success(searchFromMysqlWithCache(
                        keyword, brandId, productCategoryId, safePageNum, safePageSize, sort));
            }
            List<EsProduct> content = page.getContent();
            boolean allVisible = content.stream().allMatch(this::isAppVisibleByDb);
            if (!allVisible) {
                log.warn("[Search] ES has stale/unpublished docs, fallback to MySQL. keyword={}, brandId={}, categoryId={}",
                        keyword, brandId, productCategoryId);
                return CommonResult.success(searchFromMysqlWithCache(
                        keyword, brandId, productCategoryId, safePageNum, safePageSize, sort));
            }
            return CommonResult.success(CommonPage.of(content, safePageNum, safePageSize, page.getTotalElements()));
        } catch (Exception ex) {
            log.warn("[Search] ES query failed, fallback to MySQL. keyword={}, brandId={}, categoryId={}",
                    keyword, brandId, productCategoryId, ex);
            return CommonResult.success(searchFromMysqlWithCache(
                    keyword, brandId, productCategoryId, safePageNum, safePageSize, sort));
        }
    }

    private CommonPage<EsProduct> searchFromMysqlWithCache(String keyword,
                                                           Long brandId,
                                                           Long productCategoryId,
                                                           int pageNum,
                                                           int pageSize,
                                                           Integer sort) {
        long nowMillis = System.currentTimeMillis();
        String cacheKey = buildCacheKey(keyword, brandId, productCategoryId, pageNum, pageSize, sort);
        CachedSearchPage cached = mysqlSearchCache.get(cacheKey);
        if (cached != null && !cached.isExpired(nowMillis)) {
            return cached.page;
        }

        CommonPage<EsProduct> page = searchFromMysql(keyword, brandId, productCategoryId, pageNum, pageSize, sort);
        cleanupExpiredCache(nowMillis);
        mysqlSearchCache.put(cacheKey, new CachedSearchPage(page, nowMillis + MYSQL_SEARCH_CACHE_TTL_MILLIS));
        return page;
    }

    private CommonPage<EsProduct> searchFromMysql(String keyword,
                                                  Long brandId,
                                                  Long productCategoryId,
                                                  int pageNum,
                                                  int pageSize,
                                                  Integer sort) {
        // 统一“可见规则”：只返回已上架（publishStatus=1）商品。
        // 为保证排序稳定性，这里先取一定规模候选后在内存中排序并分页。
        Page<PmsProduct> mysqlPage = productService.list(
                keyword, brandId, productCategoryId, 1, null, 1, MYSQL_FETCH_LIMIT);
        List<PmsProduct> allMatched = new ArrayList<>(mysqlPage.getRecords());
        applySort(allMatched, sort);

        if (allMatched.size() >= MYSQL_FETCH_LIMIT) {
            log.warn("[Search] MySQL candidate set reached fetch limit={}, keyword={}, brandId={}, categoryId={}",
                    MYSQL_FETCH_LIMIT, keyword, brandId, productCategoryId);
        }

        List<EsProduct> mapped = allMatched.stream()
                .map(this::toEsProduct)
                .toList();
        List<EsProduct> paged = paginate(mapped, pageNum, pageSize);
        return CommonPage.of(paged, pageNum, pageSize, mapped.size());
    }

    private String buildCacheKey(String keyword,
                                 Long brandId,
                                 Long productCategoryId,
                                 int pageNum,
                                 int pageSize,
                                 Integer sort) {
        return String.join("|",
                keyword == null ? "" : keyword.trim().toLowerCase(),
                String.valueOf(brandId),
                String.valueOf(productCategoryId),
                String.valueOf(pageNum),
                String.valueOf(pageSize),
                String.valueOf(sort));
    }

    private void cleanupExpiredCache(long nowMillis) {
        mysqlSearchCache.entrySet().removeIf(entry -> entry.getValue().isExpired(nowMillis));
        if (mysqlSearchCache.size() <= MYSQL_SEARCH_CACHE_MAX_ENTRIES) {
            return;
        }
        Iterator<String> iterator = mysqlSearchCache.keySet().iterator();
        if (iterator.hasNext()) {
            mysqlSearchCache.remove(iterator.next());
        }
    }

    private void applySort(List<PmsProduct> products, Integer sort) {
        Comparator<PmsProduct> comparator;
        if (sort != null && sort == 1) {
            comparator = Comparator
                    .comparing((PmsProduct p) -> safeInt(p.getNewStatus()), Comparator.reverseOrder())
                    .thenComparing((PmsProduct p) -> safeLong(p.getId()), Comparator.reverseOrder());
        } else if (sort != null && sort == 2) {
            comparator = Comparator
                    .comparing((PmsProduct p) -> safeInt(p.getSale()), Comparator.reverseOrder())
                    .thenComparing((PmsProduct p) -> safeLong(p.getId()), Comparator.reverseOrder());
        } else if (sort != null && sort == 3) {
            comparator = Comparator
                    .comparing((PmsProduct p) -> safeDecimal(p.getPrice()))
                    .thenComparing((PmsProduct p) -> safeLong(p.getId()), Comparator.reverseOrder());
        } else {
            comparator = Comparator.comparing((PmsProduct p) -> safeLong(p.getId()), Comparator.reverseOrder());
        }
        products.sort(comparator);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private BigDecimal safeDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private <T> List<T> paginate(List<T> source, int pageNum, int pageSize) {
        int from = (pageNum - 1) * pageSize;
        if (from >= source.size()) {
            return List.of();
        }
        int to = Math.min(from + pageSize, source.size());
        return source.subList(from, to);
    }

    private boolean isAppVisibleByDb(EsProduct esProduct) {
        if (esProduct == null || esProduct.getId() == null) {
            return false;
        }
        PmsProduct product = productService.getById(esProduct.getId());
        return product != null && Integer.valueOf(1).equals(product.getPublishStatus());
    }

    private EsProduct toEsProduct(PmsProduct p) {
        EsProduct es = new EsProduct();
        es.setId(p.getId());
        es.setProductSn(p.getProductSn());
        es.setBrandId(p.getBrandId());
        es.setBrandName(p.getBrandName());
        es.setProductCategoryId(p.getProductCategoryId());
        es.setProductCategoryName(p.getProductCategoryName());
        es.setPic(p.getPic());
        es.setName(p.getName());
        es.setSubTitle(p.getSubTitle());
        es.setKeywords(p.getKeywords());
        es.setPrice(p.getPrice());
        es.setSale(p.getSale());
        es.setNewStatus(p.getNewStatus());
        es.setRecommandStatus(p.getRecommandStatus());
        es.setStock(p.getStock());
        es.setSort(p.getSort());
        return es;
    }
}
