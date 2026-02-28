package com.mall.admin.controller;

import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.marketing.entity.SmsFlashPromotionProductRelation;
import com.mall.module.marketing.service.FlashPromotionProductRelationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 秒杀商品关联管理 — 对标 V1 SmsFlashPromotionProductRelationController（V2 完全缺失）
 */
@Tag(name = "SmsFlashPromotionProductRelation", description = "秒杀商品关联")
@RestController
@RequestMapping("/flashProductRelation")
public class SmsFlashPromotionProductRelationController {

    private final FlashPromotionProductRelationService flashPromotionProductRelationService;

    public SmsFlashPromotionProductRelationController(FlashPromotionProductRelationService flashPromotionProductRelationService) {
        this.flashPromotionProductRelationService = flashPromotionProductRelationService;
    }

    @Operation(summary = "创建关联")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody List<SmsFlashPromotionProductRelation> relationList) {
        int count = flashPromotionProductRelationService.create(relationList);
        return CommonResult.success(count);
    }

    @Operation(summary = "修改关联")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody SmsFlashPromotionProductRelation relation) {
        int count = flashPromotionProductRelationService.update(id, relation);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除关联")
    @PostMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = flashPromotionProductRelationService.delete(id);
        return CommonResult.success(count);
    }

    @Operation(summary = "分页查询关联列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<SmsFlashPromotionProductRelation>> list(@RequestParam Long flashPromotionId,
                                                                           @RequestParam Long flashPromotionSessionId,
                                                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                                                           @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<SmsFlashPromotionProductRelation> page = flashPromotionProductRelationService.list(flashPromotionId, flashPromotionSessionId, pageNum, pageSize);
        return CommonResult.success(CommonPage.from(page));
    }
}
