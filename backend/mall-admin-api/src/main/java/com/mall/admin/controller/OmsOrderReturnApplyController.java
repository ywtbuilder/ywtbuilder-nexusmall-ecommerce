package com.mall.admin.controller;

import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.order.entity.OmsOrderReturnApply;
import com.mall.module.order.service.OrderReturnApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 退货申请管理 — 对标 V1 OmsOrderReturnApplyController（V2 部分实现）
 * <p>
 * V2 缺失：更详细的查询条件、退货处理
 */
@Tag(name = "OmsOrderReturnApply", description = "退货申请管理")
@RestController
@RequestMapping("/returnApply")
public class OmsOrderReturnApplyController {

    private final OrderReturnApplyService orderReturnApplyService;

    public OmsOrderReturnApplyController(OrderReturnApplyService orderReturnApplyService) {
        this.orderReturnApplyService = orderReturnApplyService;
    }

    @Operation(summary = "退货申请分页列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<OmsOrderReturnApply>> list(@RequestParam(required = false) Long id,
                                      @RequestParam(required = false) Integer status,
                                      @RequestParam(required = false) String createTime,
                                      @RequestParam(required = false) String handleMan,
                                      @RequestParam(required = false) String handleTime,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "5") Integer pageSize) {
        return CommonResult.success(CommonPage.from(orderReturnApplyService.list(id, status, createTime, handleMan, handleTime, pageNum, pageSize)));
    }

    @Operation(summary = "退货申请详情")
    @GetMapping("/{id}")
    public CommonResult<OmsOrderReturnApply> getItem(@PathVariable Long id) {
        return CommonResult.success(orderReturnApplyService.detail(id));
    }

    @Operation(summary = "修改退货申请状态")
    @PostMapping("/update/status/{id}")
    public CommonResult<Integer> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> statusParam) {
        Integer status = statusParam.get("status") != null ? ((Number) statusParam.get("status")).intValue() : null;
        String handleNote = (String) statusParam.get("handleNote");
        String handleMan = (String) statusParam.get("handleMan");
        Long companyAddressId = statusParam.get("companyAddressId") != null ? ((Number) statusParam.get("companyAddressId")).longValue() : null;
        return CommonResult.success(orderReturnApplyService.updateStatus(id, status, handleNote, handleMan, companyAddressId));
    }

    @Operation(summary = "批量删除退货申请")
    @PostMapping("/delete")
    public CommonResult<Integer> delete(@RequestParam List<Long> ids) {
        return CommonResult.success(orderReturnApplyService.delete(ids));
    }
}
