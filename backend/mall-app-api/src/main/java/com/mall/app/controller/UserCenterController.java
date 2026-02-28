package com.mall.app.controller;

import com.mall.common.api.CommonResult;
import com.mall.module.cart.service.CartService;
import com.mall.module.marketing.service.CouponService;
import com.mall.module.member.entity.UmsMember;
import com.mall.module.member.service.MemberBrandAttentionService;
import com.mall.module.member.service.MemberProductCollectionService;
import com.mall.module.member.service.MemberReadHistoryService;
import com.mall.module.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户中心聚合接口 — 一次返回「我的」页首屏所有需要的数据，
 * 减少前端多次请求造成的加载抖动与布局闪烁。
 */
@Tag(name = "UserCenter", description = "用户中心聚合")
@RestController
@RequestMapping("/user/center")
public class UserCenterController {

    private final MemberService memberService;
    private final CouponService couponService;
    private final MemberBrandAttentionService attentionService;
    private final MemberProductCollectionService collectionService;
    private final MemberReadHistoryService readHistoryService;
    private final CartService cartService;

    public UserCenterController(MemberService memberService,
                                CouponService couponService,
                                MemberBrandAttentionService attentionService,
                                MemberProductCollectionService collectionService,
                                MemberReadHistoryService readHistoryService,
                                CartService cartService) {
        this.memberService = memberService;
        this.couponService = couponService;
        this.attentionService = attentionService;
        this.collectionService = collectionService;
        this.readHistoryService = readHistoryService;
        this.cartService = cartService;
    }

    /**
     * GET /user/center/summary
     * 返回「我的」页首屏所需的聚合数据：
     * - user: 昵称、头像、会员等级
     * - stats: couponCount、followCount、favCount、footprintCount
     * - orderCounts: pendingPay、pendingShip、pendingReceive、pendingReview、afterSale
     * - cartCount: 购物车商品数（Header badge 用）
     */
    @Operation(summary = "「我的」页首屏聚合数据")
    @GetMapping("/summary")
    public CommonResult<Map<String, Object>> summary() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UmsMember member = memberService.getMemberByUsername(username);
        Long memberId = member.getId();

        Map<String, Object> result = new HashMap<>();

        // ── user ──────────────────────────────────────────────
        Map<String, Object> user = new HashMap<>();
        user.put("id", member.getId());
        user.put("nickname", member.getNickname() != null ? member.getNickname() : "用户");
        user.put("icon", member.getIcon() != null ? member.getIcon() : "");
        user.put("phone", member.getPhone() != null ? member.getPhone() : "");
        user.put("memberLevel", "PLUS会员");
        result.put("user", user);

        // ── stats ─────────────────────────────────────────────
        Map<String, Integer> stats = new HashMap<>();
        try { stats.put("couponCount", couponService.listByMember(memberId, null).size()); }
        catch (Exception e) { stats.put("couponCount", 0); }
        try { stats.put("followCount", attentionService.list(memberId).size()); }
        catch (Exception e) { stats.put("followCount", 0); }
        try { stats.put("favCount", collectionService.list(memberId).size()); }
        catch (Exception e) { stats.put("favCount", 0); }
        try { stats.put("footprintCount", readHistoryService.list(memberId).size()); }
        catch (Exception e) { stats.put("footprintCount", 0); }
        result.put("stats", stats);

        // ── orderCounts（预留，扩展后填入真实数据）────────────
        Map<String, Integer> orderCounts = new HashMap<>();
        orderCounts.put("pendingPay", 0);
        orderCounts.put("pendingShip", 0);
        orderCounts.put("pendingReceive", 0);
        orderCounts.put("pendingReview", 0);
        orderCounts.put("afterSale", 0);
        result.put("orderCounts", orderCounts);

        // ── cartCount ─────────────────────────────────────────
        try {
            int cartCount = cartService.list(memberId).size();
            result.put("cartCount", cartCount);
        } catch (Exception e) {
            result.put("cartCount", 0);
        }

        return CommonResult.success(result);
    }
}
