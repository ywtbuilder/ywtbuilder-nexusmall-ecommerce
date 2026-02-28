package com.mall.admin.controller;

import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.product.entity.PmsProduct;
import com.mall.module.product.service.ProductService;
import com.mall.module.search.entity.EsProduct;
import com.mall.module.search.service.EsProductService;
import com.mall.module.search.service.impl.EsProductServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ES 商品搜索管理 — Admin 端对 ES 索引进行导入/重建
 * <p>
 * 由于 module-search 不依赖 module-product，
 * MySQL → ES 的转换在此 BFF 层完成。
 */
@Tag(name = "EsProduct", description = "商品搜索")
@RestController
@RequestMapping("/esProduct")
public class EsProductController {

    private final EsProductService esProductService;
    private final ProductService productService;

    public EsProductController(EsProductService esProductService,
                               ProductService productService) {
        this.esProductService = esProductService;
        this.productService = productService;
    }

    @Operation(summary = "导入所有商品到 ES")
    @PostMapping("/importAll")
    public CommonResult<Integer> importAll() {
        // 从 MySQL 读取所有上架商品，转为 EsProduct 后写入 ES
        List<PmsProduct> allProducts = productService.listAll();
        List<EsProduct> esProducts = allProducts.stream()
                .filter(p -> p.getPublishStatus() != null && p.getPublishStatus() == 1)
                .map(this::toEsProduct)
                .collect(Collectors.toList());
        if (!esProducts.isEmpty() && esProductService instanceof EsProductServiceImpl impl) {
            impl.saveAll(esProducts);
        }
        return CommonResult.success(esProducts.size());
    }

    @Operation(summary = "根据ID删除ES商品")
    @PostMapping("/delete/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        esProductService.delete(id);
        return CommonResult.success(null);
    }

    @Operation(summary = "根据ID创建/更新ES商品")
    @PostMapping("/create/{id}")
    public CommonResult<Void> create(@PathVariable Long id) {
        PmsProduct product = productService.getById(id);
        if (product == null || product.getPublishStatus() == null || product.getPublishStatus() != 1) {
            esProductService.delete(id);
            return CommonResult.success(null);
        }
        if (esProductService instanceof EsProductServiceImpl impl) {
            impl.save(toEsProduct(product));
        }
        return CommonResult.success(null);
    }

    @Operation(summary = "简单搜索")
    @GetMapping("/search/simple")
    public CommonResult<CommonPage<EsProduct>> search(@RequestParam(required = false) String keyword,
                                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                                      @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<EsProduct> page = esProductService.search(keyword, null, null, pageNum, pageSize, 0);
        return CommonResult.success(CommonPage.of(page.getContent(), pageNum, pageSize, page.getTotalElements()));
    }

    /** MySQL PmsProduct → ES EsProduct 映射 */
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
        es.setPromotionPrice(p.getPromotionPrice());
        es.setSort(p.getSort());
        return es;
    }
}
