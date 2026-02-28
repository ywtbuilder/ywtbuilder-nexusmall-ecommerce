package com.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.product.dto.PmsProductAttributeCategoryItem;
import com.mall.module.product.entity.PmsProductAttributeCategory;
import com.mall.module.product.service.ProductAttributeCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品属性分类管理 — 对标 V1 PmsProductAttributeCategoryController（V2 已实现）
 */
@Tag(name = "PmsProductAttrCategory", description = "商品属性分类")
@RestController
@RequestMapping("/productAttribute/category")
public class PmsProductAttributeCategoryController {

    private final ProductAttributeCategoryService productAttributeCategoryService;

    public PmsProductAttributeCategoryController(ProductAttributeCategoryService productAttributeCategoryService) {
        this.productAttributeCategoryService = productAttributeCategoryService;
    }

    @Operation(summary = "创建属性分类")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestParam String name) {
        int count = productAttributeCategoryService.create(name);
        return CommonResult.success(count);
    }

    @Operation(summary = "修改属性分类")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestParam String name) {
        int count = productAttributeCategoryService.update(id, name);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除属性分类")
    @PostMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = productAttributeCategoryService.delete(id);
        return CommonResult.success(count);
    }

    @Operation(summary = "属性分类分页列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<PmsProductAttributeCategory>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<PmsProductAttributeCategory> page = productAttributeCategoryService.list(pageNum, pageSize);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "获取所有属性分类及其下属性")
    @GetMapping("/list/withAttr")
    public CommonResult<List<PmsProductAttributeCategoryItem>> listWithAttr() {
        List<PmsProductAttributeCategoryItem> list = productAttributeCategoryService.getListWithAttr();
        return CommonResult.success(list);
    }
}
