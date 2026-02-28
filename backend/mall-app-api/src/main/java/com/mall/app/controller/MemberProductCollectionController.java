package com.mall.app.controller;

import com.mall.common.api.CommonResult;
import com.mall.module.member.entity.MemberProductCollection;
import com.mall.module.member.entity.UmsMember;
import com.mall.module.member.service.MemberProductCollectionService;
import com.mall.module.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员商品收藏 — V2 完全缺失，V3 新增
 */
@Tag(name = "MemberProductCollection", description = "商品收藏")
@RestController
@RequestMapping("/member/productCollection")
public class MemberProductCollectionController {

    private final MemberProductCollectionService memberProductCollectionService;
    private final MemberService memberService;

    public MemberProductCollectionController(MemberProductCollectionService memberProductCollectionService, MemberService memberService) {
        this.memberProductCollectionService = memberProductCollectionService;
        this.memberService = memberService;
    }

    private Long getCurrentMemberId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UmsMember member = memberService.getMemberByUsername(username);
        return member.getId();
    }

    @Operation(summary = "添加收藏")
    @PostMapping("/add")
    public CommonResult<Integer> add(@RequestBody MemberProductCollection productCollection) {
        productCollection.setMemberId(getCurrentMemberId());
        int count = memberProductCollectionService.add(productCollection);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除收藏")
    @PostMapping("/delete")
    public CommonResult<Integer> delete(@RequestParam Long productId) {
        int count = memberProductCollectionService.delete(getCurrentMemberId(), productId);
        return CommonResult.success(count);
    }

    @Operation(summary = "收藏列表")
    @GetMapping("/list")
    public CommonResult<List<MemberProductCollection>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "5") Integer pageSize) {
        List<MemberProductCollection> collectionList = memberProductCollectionService.list(getCurrentMemberId());
        return CommonResult.success(collectionList);
    }

    @Operation(summary = "收藏详情")
    @GetMapping("/detail")
    public CommonResult<MemberProductCollection> detail(@RequestParam Long productId) {
        MemberProductCollection collection = memberProductCollectionService.detail(getCurrentMemberId(), productId);
        return CommonResult.success(collection);
    }

    @Operation(summary = "清空收藏")
    @PostMapping("/clear")
    public CommonResult<Void> clear() {
        memberProductCollectionService.clear(getCurrentMemberId());
        return CommonResult.success(null);
    }
}
