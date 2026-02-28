package com.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.entity.UmsMenu;
import com.mall.admin.service.UmsMenuService;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理 — 完整业务实现
 */
@Tag(name = "UmsMenu", description = "菜单管理")
@RestController
@RequestMapping("/menu")
public class UmsMenuController {

    private final UmsMenuService menuService;

    public UmsMenuController(UmsMenuService menuService) {
        this.menuService = menuService;
    }

    @Operation(summary = "创建菜单")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody UmsMenu menu) {
        int count = menuService.create(menu);
        return CommonResult.success(count);
    }

    @Operation(summary = "修改菜单")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody UmsMenu menu) {
        int count = menuService.update(id, menu);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除菜单")
    @PostMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = menuService.delete(id);
        return CommonResult.success(count);
    }

    @Operation(summary = "菜单分页列表")
    @GetMapping("/list/{parentId}")
    public CommonResult<CommonPage<UmsMenu>> list(@PathVariable Long parentId,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<UmsMenu> page = menuService.list(parentId, pageNum, pageSize);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "根据ID获取菜单")
    @GetMapping("/{id}")
    public CommonResult<UmsMenu> getItem(@PathVariable Long id) {
        UmsMenu menu = menuService.getItem(id);
        return CommonResult.success(menu);
    }

    @Operation(summary = "菜单树形列表")
    @GetMapping("/treeList")
    public CommonResult<List<UmsMenu>> treeList() {
        List<UmsMenu> menuList = menuService.treeList();
        return CommonResult.success(menuList);
    }

    @Operation(summary = "修改菜单显示状态")
    @PostMapping("/updateHidden/{id}")
    public CommonResult<Integer> updateHidden(@PathVariable Long id, @RequestParam Integer hidden) {
        int count = menuService.updateHidden(id, hidden);
        return CommonResult.success(count);
    }
}
