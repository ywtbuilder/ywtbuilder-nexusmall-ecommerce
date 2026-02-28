package com.mall.app.controller;

import com.mall.common.api.CommonResult;
import com.mall.module.member.entity.MemberReadHistory;
import com.mall.module.member.entity.UmsMember;
import com.mall.module.member.service.MemberReadHistoryService;
import com.mall.module.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员浏览记录 — V2 完全缺失，V3 新增
 */
@Tag(name = "MemberReadHistory", description = "浏览记录")
@RestController
@RequestMapping("/member/readHistory")
public class MemberReadHistoryController {

    private final MemberReadHistoryService memberReadHistoryService;
    private final MemberService memberService;

    public MemberReadHistoryController(MemberReadHistoryService memberReadHistoryService, MemberService memberService) {
        this.memberReadHistoryService = memberReadHistoryService;
        this.memberService = memberService;
    }

    private Long getCurrentMemberId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UmsMember member = memberService.getMemberByUsername(username);
        return member.getId();
    }

    @Operation(summary = "创建浏览记录")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody MemberReadHistory readHistory) {
        readHistory.setMemberId(getCurrentMemberId());
        int count = memberReadHistoryService.create(readHistory);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除浏览记录")
    @PostMapping("/delete")
    public CommonResult<Integer> delete(@RequestParam List<String> ids) {
        // B-9: 只删除属于当前用户的记录，防止越权
        int count = memberReadHistoryService.delete(ids, getCurrentMemberId());
        return CommonResult.success(count);
    }

    @Operation(summary = "清空浏览记录")
    @PostMapping("/clear")
    public CommonResult<Void> clear() {
        memberReadHistoryService.clear(getCurrentMemberId());
        return CommonResult.success(null);
    }

    @Operation(summary = "浏览记录列表")
    @GetMapping("/list")
    public CommonResult<List<MemberReadHistory>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "5") Integer pageSize) {
        List<MemberReadHistory> historyList = memberReadHistoryService.list(getCurrentMemberId());
        return CommonResult.success(historyList);
    }
}
