package com.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.product.dto.PmsProductCategoryParam;
import com.mall.module.product.dto.PmsProductCategoryWithChildrenItem;
import com.mall.module.product.entity.PmsProductCategory;
import com.mall.module.product.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类管理 — 对标 V1 PmsProductCategoryController（V2 已实现）
 * <p>
 * V2 缺失：批量修改导航栏显示状态、批量修改显示状态
 */
@Tag(name = "PmsProductCategory", description = "商品分类管理")
@RestController
@RequestMapping("/productCategory")
public class PmsProductCategoryController {

    private final ProductCategoryService productCategoryService;

    public PmsProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @Operation(summary = "创建产品分类")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody PmsProductCategoryParam categoryParam) {
        int count = productCategoryService.create(categoryParam);
        return CommonResult.success(count);
    }

    @Operation(summary = "修改产品分类")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody PmsProductCategoryParam categoryParam) {
        int count = productCategoryService.update(id, categoryParam);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除产品分类")
    @PostMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = productCategoryService.delete(id);
        return CommonResult.success(count);
    }

    @Operation(summary = "分类分页列表")
    @GetMapping("/list/{parentId}")
    public CommonResult<CommonPage<PmsProductCategory>> list(@PathVariable Long parentId,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<PmsProductCategory> page = productCategoryService.list(parentId, pageNum, pageSize);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "根据ID获取分类")
    @GetMapping("/{id}")
    public CommonResult<PmsProductCategory> getItem(@PathVariable Long id) {
        PmsProductCategory category = productCategoryService.getItem(id);
        return CommonResult.success(category);
    }

    @Operation(summary = "查询所有一级分类及子分类")
    @GetMapping("/list/withChildren")
    public CommonResult<List<PmsProductCategoryWithChildrenItem>> listWithChildren() {
        List<PmsProductCategoryWithChildrenItem> list = productCategoryService.listWithChildren();
        return CommonResult.success(list);
    }

    @Operation(summary = "批量修改导航栏显示状态")
    @PostMapping("/update/navStatus")
    public CommonResult<Integer> updateNavStatus(@RequestParam List<Long> ids,
                                                  @RequestParam Integer navStatus) {
        int count = productCategoryService.updateNavStatus(ids, navStatus);
        return CommonResult.success(count);
    }

    @Operation(summary = "批量修改商品分类显示状态")
    @PostMapping("/update/showStatus")
    public CommonResult<Integer> updateShowStatus(@RequestParam List<Long> ids,
                                                   @RequestParam Integer showStatus) {
        int count = productCategoryService.updateShowStatus(ids, showStatus);
        return CommonResult.success(count);
    }
}
