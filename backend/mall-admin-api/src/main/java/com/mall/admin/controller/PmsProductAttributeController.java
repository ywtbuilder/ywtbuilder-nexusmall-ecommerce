package com.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.product.entity.PmsProductAttribute;
import com.mall.module.product.service.ProductAttributeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品属性管理 — 对标 V1 PmsProductAttributeController（V2 已实现）
 */
@Tag(name = "PmsProductAttribute", description = "商品属性")
@RestController
@RequestMapping("/productAttribute")
public class PmsProductAttributeController {

    private final ProductAttributeService productAttributeService;

    public PmsProductAttributeController(ProductAttributeService productAttributeService) {
        this.productAttributeService = productAttributeService;
    }

    @Operation(summary = "创建属性")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody PmsProductAttribute attribute) {
        int count = productAttributeService.create(attribute);
        return CommonResult.success(count);
    }

    @Operation(summary = "修改属性")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody PmsProductAttribute attribute) {
        int count = productAttributeService.update(id, attribute);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除属性")
    @PostMapping("/delete")
    public CommonResult<Integer> delete(@RequestParam List<Long> ids) {
        int count = productAttributeService.delete(ids);
        return CommonResult.success(count);
    }

    @Operation(summary = "属性分页列表")
    @GetMapping("/list/{cid}")
    public CommonResult<CommonPage<PmsProductAttribute>> list(@PathVariable Long cid,
                                      @RequestParam(required = false) Integer type,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<PmsProductAttribute> page = productAttributeService.list(cid, type, pageNum, pageSize);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "根据ID获取属性")
    @GetMapping("/{id}")
    public CommonResult<PmsProductAttribute> getItem(@PathVariable Long id) {
        PmsProductAttribute attribute = productAttributeService.getItem(id);
        return CommonResult.success(attribute);
    }

    @Operation(summary = "根据分类获取属性列表/参数列表")
    @GetMapping("/attrInfo/{productCategoryId}")
    public CommonResult<List<PmsProductAttribute>> getAttrInfo(@PathVariable Long productCategoryId) {
        List<PmsProductAttribute> list = productAttributeService.getProductAttrInfo(productCategoryId);
        return CommonResult.success(list);
    }
}
