package com.mall.app.controller;

import com.mall.common.api.CommonResult;
import com.mall.module.order.entity.OmsOrderReturnApply;
import com.mall.module.order.service.OrderReturnApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 退货申请 — V2 完全缺失，V3 新增
 */
@Tag(name = "ReturnApply", description = "退货申请")
@RestController
@RequestMapping("/returnApply")
public class ReturnApplyController {

    private final OrderReturnApplyService orderReturnApplyService;

    public ReturnApplyController(OrderReturnApplyService orderReturnApplyService) {
        this.orderReturnApplyService = orderReturnApplyService;
    }

    @Operation(summary = "申请退货")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody OmsOrderReturnApply returnApply) {
        // 使用当前登录用户信息，不信任前端传入
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        returnApply.setMemberUsername(username);
        int count = orderReturnApplyService.create(returnApply);
        return CommonResult.success(count);
    }
}
