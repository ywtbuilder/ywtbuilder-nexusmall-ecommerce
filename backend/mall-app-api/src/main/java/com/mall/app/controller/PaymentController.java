package com.mall.app.controller;

import com.mall.common.api.CommonResult;
import com.mall.module.payment.entity.OmsPaymentLog;
import com.mall.module.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付管理 — V2 完全缺失，V3 新增
 */
@Tag(name = "Payment", description = "支付")
@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final com.mall.module.member.service.MemberService memberService;
    private final com.mall.module.order.service.PortalOrderService portalOrderService;

    public PaymentController(PaymentService paymentService,
                             com.mall.module.member.service.MemberService memberService,
                             com.mall.module.order.service.PortalOrderService portalOrderService) {
        this.paymentService = paymentService;
        this.memberService = memberService;
        this.portalOrderService = portalOrderService;
    }

    private Long getCurrentMemberId() {
        String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        return memberService.getMemberByUsername(username).getId();
    }

    @Operation(summary = "创建支付单")
    @PostMapping("/create")
    public CommonResult<OmsPaymentLog> create(@RequestParam Long orderId,
                                               @RequestParam Integer payType) {
        // 校验订单归属当前用户
        Long memberId = getCurrentMemberId();
        com.mall.module.order.dto.OmsOrderDetail orderDetail = portalOrderService.detail(orderId, memberId);
        if (orderDetail == null) {
            return CommonResult.failed("订单不存在或不属于当前用户");
        }
        OmsPaymentLog paymentLog = paymentService.createPayment(orderId, payType, orderDetail.getTotalAmount());
        return CommonResult.success(paymentLog);
    }

    @Operation(summary = "支付异步回调")
    @PostMapping("/notify")
    public CommonResult<Integer> handleNotify(@RequestBody Map<String, String> notifyData) {
        String tradeNo = notifyData.get("tradeNo");
        String body = notifyData.get("body");
        int result = paymentService.handleNotify(tradeNo, body);
        return CommonResult.success(result);
    }

    @Operation(summary = "查询支付状态")
    @GetMapping("/query")
    public CommonResult<OmsPaymentLog> query(@RequestParam Long orderId) {
        // 校验订单归属当前用户
        Long memberId = getCurrentMemberId();
        if (portalOrderService.detail(orderId, memberId) == null) {
            return CommonResult.failed("订单不存在或不属于当前用户");
        }
        OmsPaymentLog paymentLog = paymentService.queryPaymentStatus(orderId);
        return CommonResult.success(paymentLog);
    }
}
