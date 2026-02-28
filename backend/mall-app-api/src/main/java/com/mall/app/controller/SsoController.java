package com.mall.app.controller;

import com.mall.common.api.CommonResult;
import com.mall.module.member.dto.MemberRegisterParam;
import com.mall.module.member.entity.UmsMember;
import com.mall.module.member.service.MemberService;
import com.mall.security.config.AuthProperties;
import com.mall.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 会员登录注册管理 — 对标 V1 UmsMemberController
 * <p>
 * V2 仅有 login + info，V3 补齐 register / getAuthCode / updatePassword / refreshToken
 */
@Tag(name = "SSO", description = "会员认证")
@RestController
@RequestMapping("/sso")
public class SsoController {

    private final MemberService memberService;
    private final JwtService jwtService;
    private final AuthProperties authProperties;

    public SsoController(MemberService memberService, JwtService jwtService, AuthProperties authProperties) {
        this.memberService = memberService;
        this.jwtService = jwtService;
        this.authProperties = authProperties;
    }

    @Operation(summary = "会员注册")
    @PostMapping("/register")
    public CommonResult<Void> register(@Valid @RequestBody MemberRegisterParam param) {
        memberService.register(param);
        return CommonResult.success(null);
    }

    @Operation(summary = "会员登录")
    @PostMapping("/login")
    public CommonResult<Map<String, String>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        String token = memberService.login(username, password);
        return CommonResult.success(Map.of("token", token, "tokenHead", authProperties.getTokenHead()));
    }

    @Operation(summary = "获取会员信息")
    @GetMapping("/info")
    public CommonResult<UmsMember> info() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UmsMember member = memberService.getMemberByUsername(username);
        return CommonResult.success(member);
    }

    @Operation(summary = "获取验证码")
    @GetMapping("/getAuthCode")
    public CommonResult<String> getAuthCode(@RequestParam String telephone) {
        String authCode = memberService.getAuthCode(telephone);
        return CommonResult.success(authCode);
    }

    @Operation(summary = "修改密码")
    @PostMapping("/updatePassword")
    public CommonResult<Void> updatePassword(@RequestParam String telephone,
                                              @RequestParam String password,
                                              @RequestParam String authCode) {
        memberService.updatePassword(telephone, password, authCode);
        return CommonResult.success(null);
    }

    @Operation(summary = "刷新 token")
    @GetMapping("/refreshToken")
    public CommonResult<String> refreshToken(HttpServletRequest request) {
        String header = request.getHeader(authProperties.getTokenHeader());
        String tokenHead = authProperties.getTokenHead();
        if (header == null || !header.startsWith(tokenHead)) {
            return CommonResult.failed("token 为空或格式错误");
        }
        String token = header.substring(tokenHead.length());
        String newToken = memberService.refreshToken(token);
        if (newToken == null) {
            return CommonResult.failed("token 已过期");
        }
        return CommonResult.success(newToken);
    }
}
