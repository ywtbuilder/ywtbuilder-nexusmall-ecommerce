package com.mall.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.marketing.entity.SmsHomeNewProduct;
import com.mall.module.marketing.mapper.SmsHomeNewProductMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页新品推荐管理 — 对标 V1 SmsHomeNewProductController（V2 缺失）
 */
@Tag(name = "SmsHomeNewProduct", description = "首页新品推荐")
@RestController
@RequestMapping("/home/newProduct")
public class SmsHomeNewProductController {

    private final SmsHomeNewProductMapper smsHomeNewProductMapper;

    public SmsHomeNewProductController(SmsHomeNewProductMapper smsHomeNewProductMapper) {
        this.smsHomeNewProductMapper = smsHomeNewProductMapper;
    }

    @Operation(summary = "创建新品推荐")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody List<SmsHomeNewProduct> productList) {
        int count = 0;
        for (SmsHomeNewProduct item : productList) {
            smsHomeNewProductMapper.insert(item);
            count++;
        }
        return CommonResult.success(count);
    }

    @Operation(summary = "修改推荐排序")
    @PostMapping("/update/sort/{id}")
    public CommonResult<Integer> updateSort(@PathVariable Long id, @RequestParam Integer sort) {
        SmsHomeNewProduct product = new SmsHomeNewProduct();
        product.setId(id);
        product.setSort(sort);
        int count = smsHomeNewProductMapper.updateById(product);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除新品推荐")
    @PostMapping("/delete")
    public CommonResult<Integer> delete(@RequestParam List<Long> ids) {
        int count = smsHomeNewProductMapper.deleteBatchIds(ids);
        return CommonResult.success(count);
    }

    @Operation(summary = "新品推荐分页列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<SmsHomeNewProduct>> list(@RequestParam(required = false) String productName,
                                                            @RequestParam(required = false) Integer recommendStatus,
                                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                                            @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<SmsHomeNewProduct> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SmsHomeNewProduct> wrapper = new LambdaQueryWrapper<>();
        if (productName != null) {
            wrapper.like(SmsHomeNewProduct::getProductName, productName);
        }
        if (recommendStatus != null) {
            wrapper.eq(SmsHomeNewProduct::getRecommendStatus, recommendStatus);
        }
        smsHomeNewProductMapper.selectPage(page, wrapper);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "批量修改推荐状态")
    @PostMapping("/update/recommendStatus")
    public CommonResult<Integer> updateRecommendStatus(@RequestParam List<Long> ids,
                                                        @RequestParam Integer recommendStatus) {
        int count = 0;
        for (Long id : ids) {
            SmsHomeNewProduct product = new SmsHomeNewProduct();
            product.setId(id);
            product.setRecommendStatus(recommendStatus);
            smsHomeNewProductMapper.updateById(product);
            count++;
        }
        return CommonResult.success(count);
    }
}
