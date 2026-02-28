package com.mall.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.marketing.entity.SmsHomeRecommendProduct;
import com.mall.module.marketing.mapper.SmsHomeRecommendProductMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页推荐商品管理 — 对标 V1 SmsHomeRecommendProductController（V2 缺失）
 */
@Tag(name = "SmsHomeRecommendProduct", description = "首页推荐商品")
@RestController
@RequestMapping("/home/recommendProduct")
public class SmsHomeRecommendProductController {

    private final SmsHomeRecommendProductMapper smsHomeRecommendProductMapper;

    public SmsHomeRecommendProductController(SmsHomeRecommendProductMapper smsHomeRecommendProductMapper) {
        this.smsHomeRecommendProductMapper = smsHomeRecommendProductMapper;
    }

    @Operation(summary = "创建推荐商品")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody List<SmsHomeRecommendProduct> productList) {
        int count = 0;
        for (SmsHomeRecommendProduct item : productList) {
            smsHomeRecommendProductMapper.insert(item);
            count++;
        }
        return CommonResult.success(count);
    }

    @Operation(summary = "修改推荐排序")
    @PostMapping("/update/sort/{id}")
    public CommonResult<Integer> updateSort(@PathVariable Long id, @RequestParam Integer sort) {
        SmsHomeRecommendProduct product = new SmsHomeRecommendProduct();
        product.setId(id);
        product.setSort(sort);
        int count = smsHomeRecommendProductMapper.updateById(product);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除推荐商品")
    @PostMapping("/delete")
    public CommonResult<Integer> delete(@RequestParam List<Long> ids) {
        int count = smsHomeRecommendProductMapper.deleteBatchIds(ids);
        return CommonResult.success(count);
    }

    @Operation(summary = "推荐商品分页列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<SmsHomeRecommendProduct>> list(@RequestParam(required = false) String productName,
                                                                  @RequestParam(required = false) Integer recommendStatus,
                                                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                                                  @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<SmsHomeRecommendProduct> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SmsHomeRecommendProduct> wrapper = new LambdaQueryWrapper<>();
        if (productName != null) {
            wrapper.like(SmsHomeRecommendProduct::getProductName, productName);
        }
        if (recommendStatus != null) {
            wrapper.eq(SmsHomeRecommendProduct::getRecommendStatus, recommendStatus);
        }
        smsHomeRecommendProductMapper.selectPage(page, wrapper);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "批量修改推荐状态")
    @PostMapping("/update/recommendStatus")
    public CommonResult<Integer> updateRecommendStatus(@RequestParam List<Long> ids,
                                                        @RequestParam Integer recommendStatus) {
        int count = 0;
        for (Long id : ids) {
            SmsHomeRecommendProduct product = new SmsHomeRecommendProduct();
            product.setId(id);
            product.setRecommendStatus(recommendStatus);
            smsHomeRecommendProductMapper.updateById(product);
            count++;
        }
        return CommonResult.success(count);
    }
}
