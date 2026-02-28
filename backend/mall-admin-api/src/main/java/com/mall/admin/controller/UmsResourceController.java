package com.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.entity.UmsResource;
import com.mall.admin.service.UmsResourceService;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资源管理 — 完整业务实现
 */
@Tag(name = "UmsResource", description = "资源管理")
@RestController
@RequestMapping("/resource")
public class UmsResourceController {

    private final UmsResourceService resourceService;

    public UmsResourceController(UmsResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Operation(summary = "创建资源")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody UmsResource resource) {
        int count = resourceService.create(resource);
        return CommonResult.success(count);
    }

    @Operation(summary = "修改资源")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody UmsResource resource) {
        int count = resourceService.update(id, resource);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除资源")
    @PostMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = resourceService.delete(id);
        return CommonResult.success(count);
    }

    @Operation(summary = "资源分页列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<UmsResource>> list(@RequestParam(required = false) Long categoryId,
                                                       @RequestParam(required = false) String nameKeyword,
                                                       @RequestParam(required = false) String urlKeyword,
                                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                                       @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<UmsResource> page = resourceService.list(categoryId, nameKeyword, urlKeyword, pageNum, pageSize);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "获取所有资源")
    @GetMapping("/listAll")
    public CommonResult<List<UmsResource>> listAll() {
        List<UmsResource> resourceList = resourceService.listAll();
        return CommonResult.success(resourceList);
    }

    @Operation(summary = "根据ID获取资源")
    @GetMapping("/{id}")
    public CommonResult<UmsResource> getItem(@PathVariable Long id) {
        UmsResource resource = resourceService.getItem(id);
        return CommonResult.success(resource);
    }
}
