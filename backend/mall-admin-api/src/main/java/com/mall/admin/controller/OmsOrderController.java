package com.mall.admin.controller;

import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.order.dto.OmsMoneyInfoParam;
import com.mall.module.order.dto.OmsOrderDeliveryParam;
import com.mall.module.order.dto.OmsOrderDetail;
import com.mall.module.order.dto.OmsOrderQueryParam;
import com.mall.module.order.dto.OmsReceiverInfoParam;
import com.mall.module.order.entity.OmsOrder;
import com.mall.module.order.service.AdminOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单管理 — 对标 V1 OmsOrderController（V2 已部分实现）
 * <p>
 * V2 缺失：批量发货、批量关闭订单、批量删除订单、备注
 */
@Tag(name = "OmsOrder", description = "订单管理")
@RestController
@RequestMapping("/order")
public class OmsOrderController {

    private final AdminOrderService adminOrderService;

    public OmsOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @Operation(summary = "订单分页列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<OmsOrder>> list(@RequestParam(required = false) String orderSn,
                                      @RequestParam(required = false) Integer status,
                                      @RequestParam(required = false) String receiverKeyword,
                                      @RequestParam(required = false) Integer orderType,
                                      @RequestParam(required = false) String createTime,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        OmsOrderQueryParam queryParam = new OmsOrderQueryParam();
        queryParam.setOrderSn(orderSn);
        queryParam.setStatus(status);
        queryParam.setReceiverKeyword(receiverKeyword);
        queryParam.setOrderType(orderType);
        queryParam.setCreateTime(createTime);
        return CommonResult.success(CommonPage.from(adminOrderService.list(queryParam, pageNum, pageSize)));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public CommonResult<OmsOrderDetail> detail(@PathVariable Long id) {
        return CommonResult.success(adminOrderService.detail(id));
    }

    @Operation(summary = "发货")
    @PostMapping("/update/delivery")
    public CommonResult<Integer> delivery(@RequestBody List<OmsOrderDeliveryParam> deliveryParamList) {
        return CommonResult.success(adminOrderService.delivery(deliveryParamList));
    }

    @Operation(summary = "批量关闭订单")
    @PostMapping("/update/close")
    public CommonResult<Integer> close(@RequestParam List<Long> ids, @RequestParam String note) {
        return CommonResult.success(adminOrderService.close(ids, note));
    }

    @Operation(summary = "批量删除订单")
    @PostMapping("/delete")
    public CommonResult<Integer> delete(@RequestParam List<Long> ids) {
        return CommonResult.success(adminOrderService.delete(ids));
    }

    @Operation(summary = "修改收货人信息")
    @PostMapping("/update/receiverInfo")
    public CommonResult<Integer> updateReceiverInfo(@RequestBody OmsReceiverInfoParam receiverInfoParam) {
        return CommonResult.success(adminOrderService.updateReceiverInfo(receiverInfoParam));
    }

    @Operation(summary = "修改订单费用")
    @PostMapping("/update/moneyInfo")
    public CommonResult<Integer> updateMoneyInfo(@RequestBody OmsMoneyInfoParam moneyInfoParam) {
        return CommonResult.success(adminOrderService.updateMoneyInfo(moneyInfoParam));
    }

    @Operation(summary = "备注订单")
    @PostMapping("/update/note")
    public CommonResult<Integer> updateNote(@RequestParam Long id,
                                             @RequestParam String note,
                                             @RequestParam Integer status) {
        return CommonResult.success(adminOrderService.updateNote(id, note, status));
    }
}
