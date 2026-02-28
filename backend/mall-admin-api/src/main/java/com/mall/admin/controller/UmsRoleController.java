package com.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.entity.UmsMenu;
import com.mall.admin.entity.UmsResource;
import com.mall.admin.entity.UmsRole;
import com.mall.admin.service.UmsRoleService;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理 — 完整业务实现
 */
@Tag(name = "UmsRole", description = "角色管理")
@RestController
@RequestMapping("/role")
public class UmsRoleController {

    private final UmsRoleService roleService;

    public UmsRoleController(UmsRoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "创建角色")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody UmsRole role) {
        int count = roleService.create(role);
        return CommonResult.success(count);
    }

    @Operation(summary = "修改角色")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody UmsRole role) {
        int count = roleService.update(id, role);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除角色")
    @PostMapping("/delete")
    public CommonResult<Integer> delete(@RequestParam List<Long> ids) {
        int count = roleService.delete(ids);
        return CommonResult.success(count);
    }

    @Operation(summary = "角色列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<UmsRole>> list(@RequestParam(required = false) String keyword,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<UmsRole> page = roleService.list(keyword, pageNum, pageSize);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "获取所有角色")
    @GetMapping("/listAll")
    public CommonResult<List<UmsRole>> listAll() {
        List<UmsRole> roleList = roleService.listAll();
        return CommonResult.success(roleList);
    }

    @Operation(summary = "修改角色状态")
    @PostMapping("/updateStatus/{id}")
    public CommonResult<Integer> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        int count = roleService.updateStatus(id, status);
        return CommonResult.success(count);
    }

    @Operation(summary = "获取角色资源列表")
    @GetMapping("/listResource/{roleId}")
    public CommonResult<List<UmsResource>> listResource(@PathVariable Long roleId) {
        List<UmsResource> resourceList = roleService.listResource(roleId);
        return CommonResult.success(resourceList);
    }

    @Operation(summary = "获取角色菜单列表")
    @GetMapping("/listMenu/{roleId}")
    public CommonResult<List<UmsMenu>> listMenu(@PathVariable Long roleId) {
        List<UmsMenu> menuList = roleService.listMenu(roleId);
        return CommonResult.success(menuList);
    }

    @Operation(summary = "分配资源")
    @PostMapping("/allocResource")
    public CommonResult<Integer> allocResource(@RequestParam Long roleId,
                                                @RequestParam List<Long> resourceIds) {
        int count = roleService.allocResource(roleId, resourceIds);
        return CommonResult.success(count);
    }

    @Operation(summary = "分配菜单")
    @PostMapping("/allocMenu")
    public CommonResult<Integer> allocMenu(@RequestParam Long roleId,
                                            @RequestParam List<Long> menuIds) {
        int count = roleService.allocMenu(roleId, menuIds);
        return CommonResult.success(count);
    }
}
