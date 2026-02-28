package com.mall.admin.controller;

import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.marketing.entity.SmsCoupon;
import com.mall.module.marketing.service.CouponService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 优惠券管理 — 对标 V1 SmsCouponController（V2 已实现）
 */
@Tag(name = "SmsCoupon", description = "优惠券管理")
@RestController
@RequestMapping("/coupon")
public class SmsCouponController {

    private final CouponService couponService;

    public SmsCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @Operation(summary = "创建优惠券")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody SmsCoupon couponParam) {
        int count = couponService.create(couponParam);
        return CommonResult.success(count);
    }

    @Operation(summary = "修改优惠券")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody SmsCoupon couponParam) {
        int count = couponService.update(id, couponParam);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除优惠券")
    @PostMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = couponService.delete(id);
        return CommonResult.success(count);
    }

    @Operation(summary = "优惠券分页列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<SmsCoupon>> list(@RequestParam(required = false) String name,
                                                    @RequestParam(required = false) Integer type,
                                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<SmsCoupon> page = couponService.list(name, type, pageNum, pageSize);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "获取优惠券详情")
    @GetMapping("/{id}")
    public CommonResult<SmsCoupon> getItem(@PathVariable Long id) {
        SmsCoupon coupon = couponService.getItem(id);
        return CommonResult.success(coupon);
    }
}
