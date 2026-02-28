package com.mall.admin.controller;

import com.mall.common.api.CommonResult;
import com.mall.module.product.entity.PmsSkuStock;
import com.mall.module.product.service.SkuStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SKU 库存管理 — 对标 V1 PmsSkuStockController（V2 已实现）
 */
@Tag(name = "PmsSkuStock", description = "SKU 库存管理")
@RestController
@RequestMapping("/sku")
public class PmsSkuStockController {

    private final SkuStockService skuStockService;

    public PmsSkuStockController(SkuStockService skuStockService) {
        this.skuStockService = skuStockService;
    }

    @Operation(summary = "根据商品ID及SKU编码模糊搜索SKU")
    @GetMapping("/{pid}")
    public CommonResult<List<PmsSkuStock>> getList(@PathVariable Long pid,
                                               @RequestParam(required = false) String keyword) {
        List<PmsSkuStock> list = skuStockService.getList(pid, keyword);
        return CommonResult.success(list);
    }

    @Operation(summary = "批量修改SKU信息")
    @PostMapping("/update/{pid}")
    public CommonResult<Integer> update(@PathVariable Long pid,
                                         @RequestBody List<PmsSkuStock> skuStockList) {
        int count = skuStockService.update(pid, skuStockList);
        return CommonResult.success(count);
    }
}
