package com.mall.admin.controller;

import com.mall.common.api.CommonResult;
import com.mall.module.marketing.entity.SmsFlashPromotionSession;
import com.mall.module.marketing.service.FlashPromotionSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 秒杀时间段管理 — 对标 V1 SmsFlashPromotionSessionController（V2 完全缺失）
 */
@Tag(name = "SmsFlashPromotionSession", description = "秒杀时间段")
@RestController
@RequestMapping("/flashSession")
public class SmsFlashPromotionSessionController {

    private final FlashPromotionSessionService flashPromotionSessionService;

    public SmsFlashPromotionSessionController(FlashPromotionSessionService flashPromotionSessionService) {
        this.flashPromotionSessionService = flashPromotionSessionService;
    }

    @Operation(summary = "创建场次")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody SmsFlashPromotionSession session) {
        int count = flashPromotionSessionService.create(session);
        return CommonResult.success(count);
    }

    @Operation(summary = "修改场次")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody SmsFlashPromotionSession session) {
        int count = flashPromotionSessionService.update(id, session);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除场次")
    @PostMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = flashPromotionSessionService.delete(id);
        return CommonResult.success(count);
    }

    @Operation(summary = "场次列表")
    @GetMapping("/list")
    public CommonResult<List<SmsFlashPromotionSession>> list() {
        List<SmsFlashPromotionSession> sessionList = flashPromotionSessionService.listAll();
        return CommonResult.success(sessionList);
    }

    @Operation(summary = "获取全部可选场次及关联数量")
    @GetMapping("/selectList")
    public CommonResult<List<SmsFlashPromotionSession>> selectList(@RequestParam Long flashPromotionId) {
        List<SmsFlashPromotionSession> sessionList = flashPromotionSessionService.selectList(flashPromotionId);
        return CommonResult.success(sessionList);
    }

    @Operation(summary = "修改场次启用状态")
    @PostMapping("/update/status/{id}")
    public CommonResult<Integer> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        int count = flashPromotionSessionService.updateStatus(id, status);
        return CommonResult.success(count);
    }
}
