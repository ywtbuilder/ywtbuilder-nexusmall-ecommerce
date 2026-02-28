package com.mall.admin.controller;

import com.mall.common.api.CommonResult;
import com.mall.module.order.entity.OmsOrderSetting;
import com.mall.module.order.mapper.OmsOrderSettingMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 订单设置管理 — 对标 V1 OmsOrderSettingController（V2 缺失）
 */
@Tag(name = "OmsOrderSetting", description = "订单设置")
@RestController
@RequestMapping("/orderSetting")
public class OmsOrderSettingController {

    private final OmsOrderSettingMapper omsOrderSettingMapper;

    public OmsOrderSettingController(OmsOrderSettingMapper omsOrderSettingMapper) {
        this.omsOrderSettingMapper = omsOrderSettingMapper;
    }

    @Operation(summary = "获取订单设置")
    @GetMapping("/{id}")
    public CommonResult<OmsOrderSetting> getItem(@PathVariable Long id) {
        return CommonResult.success(omsOrderSettingMapper.selectById(id));
    }

    @Operation(summary = "修改订单设置")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody OmsOrderSetting orderSetting) {
        orderSetting.setId(id);
        return CommonResult.success(omsOrderSettingMapper.updateById(orderSetting));
    }
}
