package com.mall.app.controller;

import com.mall.common.api.CommonResult;
import com.mall.module.cart.entity.OmsCartItem;
import com.mall.module.cart.service.CartService;
import com.mall.module.marketing.entity.SmsCoupon;
import com.mall.module.marketing.service.CouponService;
import com.mall.module.member.entity.UmsMember;
import com.mall.module.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会员优惠券 — V2 完全缺失，V3 新增
 */
@Tag(name = "MemberCoupon", description = "优惠券")
@RestController
@RequestMapping("/member/coupon")
public class MemberCouponController {

    private final CouponService couponService;
    private final MemberService memberService;
    private final CartService cartService;

    public MemberCouponController(CouponService couponService,
                                  MemberService memberService,
                                  CartService cartService) {
        this.couponService = couponService;
        this.memberService = memberService;
        this.cartService = cartService;
    }

    private Long getCurrentMemberId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UmsMember member = memberService.getMemberByUsername(username);
        return member.getId();
    }

    @Operation(summary = "领取优惠券")
    @PostMapping("/add/{couponId}")
    public CommonResult<Integer> add(@PathVariable Long couponId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UmsMember member = memberService.getMemberByUsername(username);
        int count = couponService.receive(couponId, member.getId(), member.getNickname());
        return CommonResult.success(count);
    }

    @Operation(summary = "我的优惠券列表")
    @GetMapping("/list")
    public CommonResult<List<SmsCoupon>> list(@RequestParam(required = false) Integer useStatus) {
        List<SmsCoupon> couponList = couponService.listByMember(getCurrentMemberId(), useStatus);
        return CommonResult.success(couponList);
    }

    /**
     * 当前购物车可用优惠券列表
     * type: 0->全部可用; 1->不可用
     *
     * 匹配逻辑：获取会员未使用的优惠券，根据购物车商品总金额过滤满足 minPoint 的优惠券，
     * 并校验优惠券有效期。type=0 返回可用，type=1 返回不可用。
     */
    @Operation(summary = "当前商品可用优惠券列表")
    @GetMapping("/list/cart/{type}")
    public CommonResult<List<SmsCoupon>> listCart(@PathVariable Integer type) {
        Long memberId = getCurrentMemberId();
        // 获取会员未使用的优惠券
        List<SmsCoupon> allCoupons = couponService.listByMember(memberId, 0);
        // 获取购物车商品，计算总金额
        List<OmsCartItem> cartItems = cartService.list(memberId);
        BigDecimal cartTotal = cartItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime now = LocalDateTime.now();

        // 筛选可用优惠券：满足最低消费门槛 + 在有效期内
        List<SmsCoupon> usable = allCoupons.stream()
                .filter(c -> {
                    boolean withinTime = (c.getStartTime() == null || !now.isBefore(c.getStartTime()))
                            && (c.getEndTime() == null || !now.isAfter(c.getEndTime()));
                    boolean meetsMin = c.getMinPoint() == null
                            || cartTotal.compareTo(c.getMinPoint()) >= 0;
                    return withinTime && meetsMin;
                })
                .collect(Collectors.toList());

        if (type == 0) {
            // 返回可用
            return CommonResult.success(usable);
        } else {
            // 返回不可用（从全部中排除可用的）
            List<Long> usableIds = usable.stream().map(SmsCoupon::getId).collect(Collectors.toList());
            List<SmsCoupon> unusable = allCoupons.stream()
                    .filter(c -> !usableIds.contains(c.getId()))
                    .collect(Collectors.toList());
            return CommonResult.success(unusable);
        }
    }

    @Operation(summary = "优惠券历史")
    @GetMapping("/listHistory")
    public CommonResult<List<SmsCoupon>> listHistory(@RequestParam(required = false) Integer useStatus) {
        List<SmsCoupon> couponList = couponService.listByMember(getCurrentMemberId(), useStatus);
        return CommonResult.success(couponList);
    }
}
