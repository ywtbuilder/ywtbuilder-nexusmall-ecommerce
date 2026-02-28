package com.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.product.entity.PmsBrand;
import com.mall.module.product.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 品牌管理 — 对标 V1 PmsBrandController（V2 已实现大部分）
 * <p>
 * V2 缺失：批量修改推荐状态、批量修改显示状态、批量修改厂家制造商状态
 */
@Tag(name = "PmsBrand", description = "品牌管理")
@RestController
@RequestMapping("/brand")
public class PmsBrandController {

    private final BrandService brandService;

    public PmsBrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @Operation(summary = "创建品牌")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody PmsBrand brand) {
        int count = brandService.create(brand);
        return CommonResult.success(count);
    }

    @Operation(summary = "修改品牌")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody PmsBrand brand) {
        int count = brandService.update(id, brand);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除品牌")
    @PostMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = brandService.delete(id);
        return CommonResult.success(count);
    }

    @Operation(summary = "批量删除品牌")
    @PostMapping("/delete/batch")
    public CommonResult<Integer> deleteBatch(@RequestParam List<Long> ids) {
        int count = brandService.deleteBatch(ids);
        return CommonResult.success(count);
    }

    @Operation(summary = "品牌分页列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<PmsBrand>> list(@RequestParam(required = false) String keyword,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<PmsBrand> page = brandService.list(keyword, pageNum, pageSize);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "获取所有品牌")
    @GetMapping("/listAll")
    public CommonResult<List<PmsBrand>> listAll() {
        List<PmsBrand> list = brandService.listAll();
        return CommonResult.success(list);
    }

    @Operation(summary = "根据ID获取品牌")
    @GetMapping("/{id}")
    public CommonResult<PmsBrand> getItem(@PathVariable Long id) {
        PmsBrand brand = brandService.getItem(id);
        return CommonResult.success(brand);
    }

    @Operation(summary = "批量修改显示状态")
    @PostMapping("/update/showStatus")
    public CommonResult<Integer> updateShowStatus(@RequestParam List<Long> ids,
                                                   @RequestParam Integer showStatus) {
        int count = brandService.updateShowStatus(ids, showStatus);
        return CommonResult.success(count);
    }

    @Operation(summary = "批量修改厂家制造商状态")
    @PostMapping("/update/factoryStatus")
    public CommonResult<Integer> updateFactoryStatus(@RequestParam List<Long> ids,
                                                      @RequestParam Integer factoryStatus) {
        int count = brandService.updateFactoryStatus(ids, factoryStatus);
        return CommonResult.success(count);
    }
}
