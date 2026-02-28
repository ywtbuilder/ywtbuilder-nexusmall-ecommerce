package com.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.product.dto.PmsProductParam;
import com.mall.module.product.dto.PmsProductResult;
import com.mall.module.product.entity.PmsProduct;
import com.mall.module.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理 — 对标 V1 PmsProductController（V2 已实现 CRUD + 审核/状态）
 * <p>
 * V2 缺失：批量修改推荐/新品/上下架、verifyProduct、simpleList
 */
@Tag(name = "PmsProduct", description = "商品管理")
@RestController
@RequestMapping("/product")
public class PmsProductController {

    private final ProductService productService;

    public PmsProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "创建商品")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody PmsProductParam productParam) {
        int count = productService.create(productParam);
        return CommonResult.success(count);
    }

    @Operation(summary = "修改商品")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody PmsProductParam productParam) {
        int count = productService.update(id, productParam);
        return CommonResult.success(count);
    }

    @Operation(summary = "商品分页列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<PmsProduct>> list(@RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Long brandId,
                                      @RequestParam(required = false) Long productCategoryId,
                                      @RequestParam(required = false) Integer publishStatus,
                                      @RequestParam(required = false) Integer verifyStatus,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<PmsProduct> page = productService.list(keyword, brandId, productCategoryId, publishStatus, verifyStatus, pageNum, pageSize);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "根据ID获取商品编辑信息")
    @GetMapping("/updateInfo/{id}")
    public CommonResult<PmsProductResult> getUpdateInfo(@PathVariable Long id) {
        PmsProductResult result = productService.getUpdateInfo(id);
        return CommonResult.success(result);
    }

    @Operation(summary = "批量修改发布状态")
    @PostMapping("/update/publishStatus")
    public CommonResult<Integer> updatePublishStatus(@RequestParam List<Long> ids,
                                                      @RequestParam Integer publishStatus) {
        int count = productService.updatePublishStatus(ids, publishStatus);
        return CommonResult.success(count);
    }

    @Operation(summary = "批量修改推荐状态")
    @PostMapping("/update/recommendStatus")
    public CommonResult<Integer> updateRecommendStatus(@RequestParam List<Long> ids,
                                                        @RequestParam Integer recommendStatus) {
        int count = productService.updateRecommendStatus(ids, recommendStatus);
        return CommonResult.success(count);
    }

    @Operation(summary = "批量修改新品状态")
    @PostMapping("/update/newStatus")
    public CommonResult<Integer> updateNewStatus(@RequestParam List<Long> ids,
                                                  @RequestParam Integer newStatus) {
        int count = productService.updateNewStatus(ids, newStatus);
        return CommonResult.success(count);
    }

    @Operation(summary = "批量删除商品")
    @PostMapping("/update/deleteStatus")
    public CommonResult<Integer> updateDeleteStatus(@RequestParam List<Long> ids,
                                                     @RequestParam Integer deleteStatus) {
        int count = productService.updateDeleteStatus(ids, deleteStatus);
        return CommonResult.success(count);
    }

    @Operation(summary = "商品审核")
    @PostMapping("/update/verifyStatus")
    public CommonResult<Integer> updateVerifyStatus(@RequestParam List<Long> ids,
                                                     @RequestParam Integer verifyStatus,
                                                     @RequestParam String detail) {
        int count = productService.updateVerifyStatus(ids, verifyStatus, detail);
        return CommonResult.success(count);
    }

    @Operation(summary = "简单商品列表")
    @GetMapping("/simpleList")
    public CommonResult<List<PmsProduct>> simpleList(@RequestParam(required = false) String keyword) {
        Page<PmsProduct> page = productService.simpleList(keyword, 1, 100);
        return CommonResult.success(page.getRecords());
    }
}
