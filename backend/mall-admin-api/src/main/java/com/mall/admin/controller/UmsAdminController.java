package com.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.entity.UmsAdmin;
import com.mall.admin.entity.UmsRole;
import com.mall.admin.service.UmsAdminService;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.security.config.AuthProperties;
import com.mall.security.service.JwtService;
import com.mall.security.service.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台用户管理 — 完整业务实现
 */
@Tag(name = "UmsAdmin", description = "后台用户管理")
@RestController
@RequestMapping("/admin")
public class UmsAdminController {

    private final UmsAdminService adminService;
    private final AuthProperties authProperties;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;

    public UmsAdminController(UmsAdminService adminService,
                              AuthProperties authProperties,
                              TokenBlacklistService tokenBlacklistService,
                              JwtService jwtService) {
        this.adminService = adminService;
        this.authProperties = authProperties;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtService = jwtService;
    }

    @Operation(summary = "后台用户登录")
    @PostMapping("/login")
    public CommonResult<Map<String, String>> login(@RequestBody Map<String, String> loginParam) {
        String username = loginParam.get("username");
        String password = loginParam.get("password");
        String token = adminService.login(username, password);
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("tokenHead", authProperties.getTokenHead());
        return CommonResult.success(result);
    }

    @Operation(summary = "注册后台用户")
    @PostMapping("/register")
    public CommonResult<UmsAdmin> register(@RequestBody UmsAdmin admin) {
        UmsAdmin registered = adminService.register(admin);
        return CommonResult.success(registered);
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/info")
    public CommonResult<Map<String, Object>> info() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UmsAdmin admin = adminService.getAdminByUsername(username);
        Map<String, Object> data = new HashMap<>();
        data.put("username", admin.getUsername());
        data.put("menus", List.of()); // 前端菜单由角色决定
        data.put("icon", admin.getIcon());
        data.put("roles", adminService.getRoleList(admin.getId()));
        return CommonResult.success(data);
    }

    @Operation(summary = "后台用户列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<UmsAdmin>> list(@RequestParam(required = false) String keyword,
                                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<UmsAdmin> page = adminService.list(keyword, pageNum, pageSize);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "获取指定用户详情")
    @GetMapping("/{id}")
    public CommonResult<UmsAdmin> getItem(@PathVariable Long id) {
        UmsAdmin admin = adminService.getItem(id);
        return CommonResult.success(admin);
    }

    @Operation(summary = "修改指定用户")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody UmsAdmin admin) {
        int count = adminService.update(id, admin);
        return CommonResult.success(count);
    }

    @Operation(summary = "修改帐号状态")
    @PostMapping("/updateStatus/{id}")
    public CommonResult<Integer> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        int count = adminService.updateStatus(id, status);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除指定用户")
    @PostMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = adminService.delete(id);
        return CommonResult.success(count);
    }

    @Operation(summary = "分配角色")
    @PostMapping("/role/update")
    public CommonResult<Integer> updateRole(@RequestParam Long adminId,
                                             @RequestParam List<Long> roleIds) {
        int count = adminService.updateRole(adminId, roleIds);
        return CommonResult.success(count);
    }

    @Operation(summary = "获取指定用户的角色")
    @GetMapping("/role/{adminId}")
    public CommonResult<List<UmsRole>> getRoleList(@PathVariable Long adminId) {
        List<UmsRole> roleList = adminService.getRoleList(adminId);
        return CommonResult.success(roleList);
    }

    @Operation(summary = "刷新 token")
    @GetMapping("/refreshToken")
    public CommonResult<Map<String, String>> refreshToken(HttpServletRequest request) {
        String header = request.getHeader(authProperties.getTokenHeader());
        String tokenHead = authProperties.getTokenHead();
        if (header == null || !header.startsWith(tokenHead)) {
            return CommonResult.failed("token 为空或格式错误");
        }
        String token = header.substring(tokenHead.length());
        String newToken = adminService.refreshToken(token);
        if (newToken == null) {
            return CommonResult.failed("token 已过期，无法刷新");
        }
        Map<String, String> result = new HashMap<>();
        result.put("token", newToken);
        result.put("tokenHead", tokenHead);
        return CommonResult.success(result);
    }

    @Operation(summary = "登出（token 加入黑名单）")
    @PostMapping("/logout")
    public CommonResult<Void> logout(HttpServletRequest request) {
        String header = request.getHeader(authProperties.getTokenHeader());
        if (header != null && header.startsWith(authProperties.getTokenHead())) {
            String token = header.substring(authProperties.getTokenHead().length());
            try {
                long ttl = jwtService.getExpiration(token).getTime() - System.currentTimeMillis();
                if (ttl > 0) {
                    tokenBlacklistService.blacklist(token, ttl);
                }
            } catch (Exception ignored) {
                // token 已过期或无效，无需加入黑名单
            }
        }
        return CommonResult.success(null);
    }
}
