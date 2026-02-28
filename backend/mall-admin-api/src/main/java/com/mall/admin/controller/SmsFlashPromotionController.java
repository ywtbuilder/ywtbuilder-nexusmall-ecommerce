package com.mall.admin.controller;

import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.marketing.entity.SmsFlashPromotion;
import com.mall.module.marketing.service.FlashPromotionService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 秒杀活动管理 — 对标 V1 SmsFlashPromotionController（V2 完全缺失）
 */
@Tag(name = "SmsFlashPromotion", description = "秒杀活动管理")
@RestController
@RequestMapping("/flash")
public class SmsFlashPromotionController {

    private final FlashPromotionService flashPromotionService;

    public SmsFlashPromotionController(FlashPromotionService flashPromotionService) {
        this.flashPromotionService = flashPromotionService;
    }

    @Operation(summary = "创建秒杀活动")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody SmsFlashPromotion flashPromotion) {
        int count = flashPromotionService.create(flashPromotion);
        return CommonResult.success(count);
    }

    @Operation(summary = "修改秒杀活动")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody SmsFlashPromotion flashPromotion) {
        int count = flashPromotionService.update(id, flashPromotion);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除秒杀活动")
    @PostMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = flashPromotionService.delete(id);
        return CommonResult.success(count);
    }

    @Operation(summary = "秒杀活动分页列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<SmsFlashPromotion>> list(@RequestParam(required = false) String keyword,
                                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                                            @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<SmsFlashPromotion> page = flashPromotionService.list(keyword, pageNum, pageSize);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "获取秒杀活动详情")
    @GetMapping("/{id}")
    public CommonResult<SmsFlashPromotion> getItem(@PathVariable Long id) {
        SmsFlashPromotion flashPromotion = flashPromotionService.getItem(id);
        return CommonResult.success(flashPromotion);
    }

    @Operation(summary = "修改秒杀活动状态")
    @PostMapping("/update/status/{id}")
    public CommonResult<Integer> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        int count = flashPromotionService.updateStatus(id, status);
        return CommonResult.success(count);
    }
}
