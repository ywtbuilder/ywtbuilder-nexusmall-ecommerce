package com.mall.app.controller;

import com.mall.common.api.CommonResult;
import com.mall.module.member.entity.UmsMember;
import com.mall.module.member.entity.UmsMemberReceiveAddress;
import com.mall.module.member.service.MemberAddressService;
import com.mall.module.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址管理 — V2 完全缺失，V3 新增
 */
@Tag(name = "MemberAddress", description = "收货地址")
@RestController
@RequestMapping("/member/address")
public class MemberAddressController {

    private final MemberAddressService memberAddressService;
    private final MemberService memberService;

    public MemberAddressController(MemberAddressService memberAddressService, MemberService memberService) {
        this.memberAddressService = memberAddressService;
        this.memberService = memberService;
    }

    private Long getCurrentMemberId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UmsMember member = memberService.getMemberByUsername(username);
        return member.getId();
    }

    @Operation(summary = "新增收货地址")
    @PostMapping("/add")
    public CommonResult<Integer> add(@RequestBody UmsMemberReceiveAddress address) {
        address.setMemberId(getCurrentMemberId());
        int count = memberAddressService.add(address);
        return CommonResult.success(count);
    }

    @Operation(summary = "修改收货地址")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody UmsMemberReceiveAddress address) {
        int count = memberAddressService.update(id, address);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除收货地址")
    @PostMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = memberAddressService.delete(id, getCurrentMemberId());
        return CommonResult.success(count);
    }

    @Operation(summary = "收货地址列表")
    @GetMapping("/list")
    public CommonResult<List<UmsMemberReceiveAddress>> list() {
        List<UmsMemberReceiveAddress> addressList = memberAddressService.list(getCurrentMemberId());
        return CommonResult.success(addressList);
    }

    @Operation(summary = "获取地址详情")
    @GetMapping("/{id}")
    public CommonResult<UmsMemberReceiveAddress> getItem(@PathVariable Long id) {
        UmsMemberReceiveAddress address = memberAddressService.getItem(id);
        return CommonResult.success(address);
    }
}
