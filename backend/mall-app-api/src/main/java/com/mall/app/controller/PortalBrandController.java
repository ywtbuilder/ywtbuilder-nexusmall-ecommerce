package com.mall.app.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.product.entity.PmsBrand;
import com.mall.module.product.entity.PmsProduct;
import com.mall.module.product.service.BrandService;
import com.mall.module.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 品牌管理 — V2 复用 /home/content 拼数据，V3 改为独立接口
 */
@Tag(name = "Brand", description = "品牌")
@RestController
@RequestMapping("/brand")
public class PortalBrandController {

    private final BrandService brandService;
    private final ProductService productService;

    public PortalBrandController(BrandService brandService, ProductService productService) {
        this.brandService = brandService;
        this.productService = productService;
    }

    @Operation(summary = "推荐品牌列表")
    @GetMapping("/recommendList")
    public CommonResult<CommonPage<PmsBrand>> recommendList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                             @RequestParam(defaultValue = "6") Integer pageSize) {
        Page<PmsBrand> page = brandService.list(null, pageNum, pageSize);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "品牌详情")
    @GetMapping("/detail/{brandId}")
    public CommonResult<PmsBrand> detail(@PathVariable Long brandId) {
        PmsBrand brand = brandService.getItem(brandId);
        return CommonResult.success(brand);
    }

    @Operation(summary = "品牌商品列表")
    @GetMapping("/productList")
    public CommonResult<CommonPage<PmsProduct>> productList(@RequestParam Long brandId,
                                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                                             @RequestParam(defaultValue = "6") Integer pageSize) {
        Page<PmsProduct> page = productService.list(null, brandId, null, null, null, pageNum, pageSize);
        return CommonResult.success(CommonPage.from(page));
    }
}
