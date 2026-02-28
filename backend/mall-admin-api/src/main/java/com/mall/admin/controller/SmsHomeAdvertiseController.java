package com.mall.admin.controller;

import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.marketing.entity.SmsHomeAdvertise;
import com.mall.module.marketing.service.HomeAdvertiseService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页轮播广告管理 — 对标 V1 SmsHomeAdvertiseController（V2 已实现）
 * <p>
 * V2 缺失：批量修改上下线状态
 */
@Tag(name = "SmsHomeAdvertise", description = "首页广告")
@RestController
@RequestMapping("/home/advertise")
public class SmsHomeAdvertiseController {

    private final HomeAdvertiseService homeAdvertiseService;

    public SmsHomeAdvertiseController(HomeAdvertiseService homeAdvertiseService) {
        this.homeAdvertiseService = homeAdvertiseService;
    }

    @Operation(summary = "创建广告")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody SmsHomeAdvertise advertise) {
        int count = homeAdvertiseService.create(advertise);
        return CommonResult.success(count);
    }

    @Operation(summary = "修改广告")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody SmsHomeAdvertise advertise) {
        int count = homeAdvertiseService.update(id, advertise);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除广告")
    @PostMapping("/delete")
    public CommonResult<Integer> delete(@RequestParam List<Long> ids) {
        int count = homeAdvertiseService.delete(ids);
        return CommonResult.success(count);
    }

    @Operation(summary = "广告分页列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<SmsHomeAdvertise>> list(@RequestParam(required = false) String name,
                                                           @RequestParam(required = false) Integer type,
                                                           @RequestParam(required = false) String endTime,
                                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                                           @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<SmsHomeAdvertise> page = homeAdvertiseService.list(name, type, endTime, pageNum, pageSize);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "获取广告详情")
    @GetMapping("/{id}")
    public CommonResult<SmsHomeAdvertise> getItem(@PathVariable Long id) {
        SmsHomeAdvertise advertise = homeAdvertiseService.getItem(id);
        return CommonResult.success(advertise);
    }

    @Operation(summary = "批量修改上下线状态")
    @PostMapping("/update/status/{id}")
    public CommonResult<Integer> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        int count = homeAdvertiseService.updateStatus(id, status);
        return CommonResult.success(count);
    }
}
