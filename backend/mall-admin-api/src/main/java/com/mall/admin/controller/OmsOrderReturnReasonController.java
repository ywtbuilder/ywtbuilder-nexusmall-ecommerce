package com.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.order.entity.OmsOrderReturnReason;
import com.mall.module.order.mapper.OmsOrderReturnReasonMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 退货原因管理 — 对标 V1 OmsOrderReturnReasonController（V2 缺失）
 */
@Tag(name = "OmsOrderReturnReason", description = "退货原因管理")
@RestController
@RequestMapping("/returnReason")
public class OmsOrderReturnReasonController {

    private final OmsOrderReturnReasonMapper returnReasonMapper;

    public OmsOrderReturnReasonController(OmsOrderReturnReasonMapper returnReasonMapper) {
        this.returnReasonMapper = returnReasonMapper;
    }

    @Operation(summary = "创建退货原因")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody OmsOrderReturnReason reason) {
        return CommonResult.success(returnReasonMapper.insert(reason));
    }

    @Operation(summary = "修改退货原因")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody OmsOrderReturnReason reason) {
        reason.setId(id);
        return CommonResult.success(returnReasonMapper.updateById(reason));
    }

    @Operation(summary = "删除退货原因")
    @PostMapping("/delete")
    public CommonResult<Integer> delete(@RequestParam List<Long> ids) {
        return CommonResult.success(returnReasonMapper.deleteBatchIds(ids));
    }

    @Operation(summary = "退货原因分页列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<OmsOrderReturnReason>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<OmsOrderReturnReason> page = new Page<>(pageNum, pageSize);
        returnReasonMapper.selectPage(page, null);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "获取单个退货原因")
    @GetMapping("/{id}")
    public CommonResult<OmsOrderReturnReason> getItem(@PathVariable Long id) {
        return CommonResult.success(returnReasonMapper.selectById(id));
    }

    @Operation(summary = "批量修改退货原因状态")
    @PostMapping("/update/status")
    public CommonResult<Integer> updateStatus(@RequestParam List<Long> ids,
                                               @RequestParam Integer status) {
        int count = 0;
        for (Long id : ids) {
            OmsOrderReturnReason reason = new OmsOrderReturnReason();
            reason.setId(id);
            reason.setStatus(status);
            count += returnReasonMapper.updateById(reason);
        }
        return CommonResult.success(count);
    }
}
