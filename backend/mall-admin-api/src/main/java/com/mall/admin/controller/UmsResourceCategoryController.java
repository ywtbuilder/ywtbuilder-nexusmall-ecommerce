package com.mall.admin.controller;

import com.mall.admin.entity.UmsResourceCategory;
import com.mall.admin.service.UmsResourceCategoryService;
import com.mall.common.api.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资源分类管理 — 完整业务实现
 */
@Tag(name = "UmsResourceCategory", description = "资源分类")
@RestController
@RequestMapping("/resourceCategory")
public class UmsResourceCategoryController {

    private final UmsResourceCategoryService categoryService;

    public UmsResourceCategoryController(UmsResourceCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "创建资源分类")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody UmsResourceCategory category) {
        int count = categoryService.create(category);
        return CommonResult.success(count);
    }

    @Operation(summary = "修改资源分类")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody UmsResourceCategory category) {
        int count = categoryService.update(id, category);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除资源分类")
    @PostMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = categoryService.delete(id);
        return CommonResult.success(count);
    }

    @Operation(summary = "获取所有资源分类")
    @GetMapping("/listAll")
    public CommonResult<List<UmsResourceCategory>> listAll() {
        List<UmsResourceCategory> categoryList = categoryService.listAll();
        return CommonResult.success(categoryList);
    }
}
