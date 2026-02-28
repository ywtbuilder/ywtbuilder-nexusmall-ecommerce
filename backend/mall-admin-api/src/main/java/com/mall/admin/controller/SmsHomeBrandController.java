package com.mall.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.marketing.entity.SmsHomeBrand;
import com.mall.module.marketing.mapper.SmsHomeBrandMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页推荐品牌管理 — 对标 V1 SmsHomeBrandController（V2 已实现）
 * <p>
 * V2 缺失：批量修改推荐状态
 */
@Tag(name = "SmsHomeBrand", description = "首页推荐品牌")
@RestController
@RequestMapping("/home/brand")
public class SmsHomeBrandController {

    private final SmsHomeBrandMapper smsHomeBrandMapper;

    public SmsHomeBrandController(SmsHomeBrandMapper smsHomeBrandMapper) {
        this.smsHomeBrandMapper = smsHomeBrandMapper;
    }

    @Operation(summary = "创建推荐品牌")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody List<SmsHomeBrand> homeBrandList) {
        int count = 0;
        for (SmsHomeBrand item : homeBrandList) {
            smsHomeBrandMapper.insert(item);
            count++;
        }
        return CommonResult.success(count);
    }

    @Operation(summary = "修改推荐排序")
    @PostMapping("/update/sort/{id}")
    public CommonResult<Integer> updateSort(@PathVariable Long id, @RequestParam Integer sort) {
        SmsHomeBrand homeBrand = new SmsHomeBrand();
        homeBrand.setId(id);
        homeBrand.setSort(sort);
        int count = smsHomeBrandMapper.updateById(homeBrand);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除推荐品牌")
    @PostMapping("/delete")
    public CommonResult<Integer> delete(@RequestParam List<Long> ids) {
        int count = smsHomeBrandMapper.deleteBatchIds(ids);
        return CommonResult.success(count);
    }

    @Operation(summary = "推荐品牌分页列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<SmsHomeBrand>> list(@RequestParam(required = false) String brandName,
                                                       @RequestParam(required = false) Integer recommendStatus,
                                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                                       @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<SmsHomeBrand> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SmsHomeBrand> wrapper = new LambdaQueryWrapper<>();
        if (brandName != null) {
            wrapper.like(SmsHomeBrand::getBrandName, brandName);
        }
        if (recommendStatus != null) {
            wrapper.eq(SmsHomeBrand::getRecommendStatus, recommendStatus);
        }
        smsHomeBrandMapper.selectPage(page, wrapper);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "批量修改推荐状态")
    @PostMapping("/update/recommendStatus")
    public CommonResult<Integer> updateRecommendStatus(@RequestParam List<Long> ids,
                                                        @RequestParam Integer recommendStatus) {
        int count = 0;
        for (Long id : ids) {
            SmsHomeBrand homeBrand = new SmsHomeBrand();
            homeBrand.setId(id);
            homeBrand.setRecommendStatus(recommendStatus);
            smsHomeBrandMapper.updateById(homeBrand);
            count++;
        }
        return CommonResult.success(count);
    }
}
