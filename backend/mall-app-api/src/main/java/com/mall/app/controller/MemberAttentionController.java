package com.mall.app.controller;

import com.mall.common.api.CommonResult;
import com.mall.module.member.entity.MemberBrandAttention;
import com.mall.module.member.entity.UmsMember;
import com.mall.module.member.service.MemberBrandAttentionService;
import com.mall.module.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员品牌关注 — V2 完全缺失，V3 新增
 */
@Tag(name = "MemberAttention", description = "品牌关注")
@RestController
@RequestMapping("/member/attention")
public class MemberAttentionController {

    private final MemberBrandAttentionService memberBrandAttentionService;
    private final MemberService memberService;

    public MemberAttentionController(MemberBrandAttentionService memberBrandAttentionService, MemberService memberService) {
        this.memberBrandAttentionService = memberBrandAttentionService;
        this.memberService = memberService;
    }

    private Long getCurrentMemberId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UmsMember member = memberService.getMemberByUsername(username);
        return member.getId();
    }

    @Operation(summary = "添加关注")
    @PostMapping("/add")
    public CommonResult<Integer> add(@RequestBody MemberBrandAttention brandAttention) {
        brandAttention.setMemberId(getCurrentMemberId());
        int count = memberBrandAttentionService.add(brandAttention);
        return CommonResult.success(count);
    }

    @Operation(summary = "取消关注")
    @PostMapping("/delete")
    public CommonResult<Integer> delete(@RequestParam Long brandId) {
        int count = memberBrandAttentionService.delete(getCurrentMemberId(), brandId);
        return CommonResult.success(count);
    }

    @Operation(summary = "关注列表")
    @GetMapping("/list")
    public CommonResult<List<MemberBrandAttention>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "5") Integer pageSize) {
        List<MemberBrandAttention> attentionList = memberBrandAttentionService.list(getCurrentMemberId());
        return CommonResult.success(attentionList);
    }

    @Operation(summary = "关注详情")
    @GetMapping("/detail")
    public CommonResult<MemberBrandAttention> detail(@RequestParam Long brandId) {
        MemberBrandAttention attention = memberBrandAttentionService.detail(getCurrentMemberId(), brandId);
        return CommonResult.success(attention);
    }

    @Operation(summary = "清空关注")
    @PostMapping("/clear")
    public CommonResult<Void> clear() {
        memberBrandAttentionService.clear(getCurrentMemberId());
        return CommonResult.success(null);
    }
}
