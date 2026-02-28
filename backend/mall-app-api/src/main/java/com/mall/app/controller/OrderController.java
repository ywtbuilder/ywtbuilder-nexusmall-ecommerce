package com.mall.app.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.cart.entity.OmsCartItem;
import com.mall.module.cart.service.CartService;
import com.mall.module.member.entity.UmsMember;
import com.mall.module.member.entity.UmsMemberReceiveAddress;
import com.mall.module.member.service.MemberAddressService;
import com.mall.module.member.service.MemberService;
import com.mall.module.order.dto.OmsOrderDetail;
import com.mall.module.order.entity.OmsOrder;
import com.mall.module.order.service.PortalOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Tag(name = "Order", description = "订单")
@RestController
@RequestMapping("/order")
public class OrderController {

    private final PortalOrderService portalOrderService;
    private final MemberService memberService;
    private final CartService cartService;
    private final MemberAddressService memberAddressService;

    public OrderController(PortalOrderService portalOrderService,
                           MemberService memberService,
                           CartService cartService,
                           MemberAddressService memberAddressService) {
        this.portalOrderService = portalOrderService;
        this.memberService = memberService;
        this.cartService = cartService;
        this.memberAddressService = memberAddressService;
    }

    private Long getCurrentMemberId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UmsMember member = memberService.getMemberByUsername(username);
        return member.getId();
    }

    @Operation(summary = "确认下单页数据")
    @GetMapping("/confirm")
    public CommonResult<Map<String, Object>> confirm(@RequestParam(required = false) List<Long> cartIds) {
        Long memberId = getCurrentMemberId();
        List<OmsCartItem> cartItems = cartService.list(memberId);
        if (cartIds != null && !cartIds.isEmpty()) {
            cartItems = cartItems.stream()
                    .filter(i -> cartIds.contains(i.getId()))
                    .collect(Collectors.toList());
        }

        List<UmsMemberReceiveAddress> addressList = memberAddressService.list(memberId);

        BigDecimal totalAmount = cartItems.stream()
                .map(i -> {
                    BigDecimal price = i.getPrice() == null ? BigDecimal.ZERO : i.getPrice();
                    int quantity = i.getQuantity() == null ? 0 : i.getQuantity();
                    return price.multiply(BigDecimal.valueOf(quantity));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal freightAmount = BigDecimal.ZERO;
        BigDecimal promotionAmount = BigDecimal.ZERO;
        BigDecimal payAmount = totalAmount.add(freightAmount).subtract(promotionAmount);

        Map<String, Object> calcAmount = new HashMap<>();
        calcAmount.put("totalAmount", totalAmount);
        calcAmount.put("freightAmount", freightAmount);
        calcAmount.put("promotionAmount", promotionAmount);
        calcAmount.put("payAmount", payAmount);

        Map<String, Object> result = new HashMap<>();
        result.put("cartPromotionItemList", cartItems);
        result.put("memberReceiveAddressList", addressList);
        result.put("couponHistoryDetailList", List.of());
        result.put("integrationConsumeSetting", Map.of("useUnit", 100, "couponStatus", 0));
        result.put("memberIntegration", 0);
        result.put("calcAmount", calcAmount);

        // backward compatibility
        result.put("cartList", cartItems);
        result.put("memberId", memberId);

        return CommonResult.success(result);
    }

    @Operation(summary = "生成订单")
    @PostMapping("/generateOrder")
    public CommonResult<OmsOrder> generateOrder(@RequestBody Map<String, Object> orderParam) {
        Long memberId = getCurrentMemberId();
        OmsOrder order = portalOrderService.generateOrder(orderParam, memberId);
        return CommonResult.success(order);
    }

    @Operation(summary = "支付成功回调")
    @PostMapping("/paySuccess")
    public CommonResult<Integer> paySuccess(@RequestParam Long orderId,
                                            @RequestParam Integer payType) {
        Long memberId = getCurrentMemberId();
        OmsOrderDetail orderDetail = portalOrderService.detail(orderId, memberId);
        if (orderDetail == null) {
            return CommonResult.failed("订单不存在或不属于当前用户");
        }
        int count = portalOrderService.paySuccess(orderId, payType);
        return CommonResult.success(count);
    }

    @Operation(summary = "用户取消订单")
    @PostMapping("/cancelUserOrder")
    public CommonResult<Void> cancelUserOrder(@RequestParam Long orderId) {
        Long memberId = getCurrentMemberId();
        portalOrderService.cancelOrder(orderId, memberId);
        return CommonResult.success(null);
    }

    @Operation(summary = "订单列表（含订单项）")
    @GetMapping("/list")
    public CommonResult<CommonPage<OmsOrderDetail>> list(@RequestParam(required = false) Integer status,
                                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "5") Integer pageSize) {
        Long memberId = getCurrentMemberId();
        Page<OmsOrderDetail> page = portalOrderService.list(memberId, status, pageNum, pageSize);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/detail/{orderId}")
    public CommonResult<OmsOrderDetail> detail(@PathVariable Long orderId) {
        Long memberId = getCurrentMemberId();
        OmsOrderDetail orderDetail = portalOrderService.detail(orderId, memberId);
        return CommonResult.success(orderDetail);
    }

    @Operation(summary = "确认收货")
    @PostMapping("/confirmReceiveOrder")
    public CommonResult<Void> confirmReceiveOrder(@RequestParam Long orderId) {
        Long memberId = getCurrentMemberId();
        portalOrderService.confirmReceiveOrder(orderId, memberId);
        return CommonResult.success(null);
    }

    @Operation(summary = "删除订单")
    @PostMapping("/deleteOrder")
    public CommonResult<Void> deleteOrder(@RequestParam Long orderId) {
        Long memberId = getCurrentMemberId();
        portalOrderService.deleteOrder(orderId, memberId);
        return CommonResult.success(null);
    }

    @Operation(summary = "自动取消超时订单（内部调用）")
    @PostMapping("/cancelTimeOutOrder")
    public CommonResult<Integer> cancelTimeOutOrder() {
        int count = portalOrderService.cancelTimeOutOrder();
        return CommonResult.success(count);
    }
}
