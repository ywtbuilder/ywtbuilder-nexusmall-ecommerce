package com.mall.app.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.product.dto.PmsProductCategoryWithChildrenItem;
import com.mall.module.product.entity.PmsProduct;
import com.mall.module.product.service.ProductCategoryService;
import com.mall.module.product.service.ProductService;
import com.mall.module.product.service.SkuStockService;
import com.mall.module.product.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品详情/列表
 */
@Tag(name = "Product", description = "商品")
@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final ProductCategoryService productCategoryService;
    private final SkuStockService skuStockService;
    private final AssetService assetService;

    public ProductController(ProductService productService,
                             ProductCategoryService productCategoryService,
                             SkuStockService skuStockService,
                             AssetService assetService) {
        this.productService = productService;
        this.productCategoryService = productCategoryService;
        this.skuStockService = skuStockService;
        this.assetService = assetService;
    }

    @Operation(summary = "商品详情")
    @GetMapping("/detail/{id}")
    public CommonResult<PmsProduct> detail(@PathVariable Long id) {
        PmsProduct product = productService.getById(id);
        if (!isAppVisible(product)) {
            return CommonResult.failed("商品不存在或已下架");
        }
        // 填充SKU列表
        product.setSkuStockList(skuStockService.getList(id, null));
        // 填充规格参数
        product.setSpecList(assetService.getSpecsByProductId(id));
        // 填充简介长图与详情长图
        product.setIntroImageUrls(assetService.getImageUrlsByProductIdAndType(id, 2));
        product.setDetailImageUrls(assetService.getImageUrlsByProductIdAndType(id, 1));
        return CommonResult.success(product);
    }

    @Operation(summary = "分类树列表")
    @GetMapping("/categoryTreeList")
    public CommonResult<List<PmsProductCategoryWithChildrenItem>> categoryTreeList() {
        List<PmsProductCategoryWithChildrenItem> list = productCategoryService.listWithChildren();
        return CommonResult.success(list);
    }

    @Operation(summary = "商品搜索（简单条件）")
    @GetMapping("/search")
    public CommonResult<CommonPage<PmsProduct>> search(@RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) Long brandId,
                                                       @RequestParam(required = false) Long productCategoryId,
                                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                                       @RequestParam(defaultValue = "5") Integer pageSize,
                                                       @RequestParam(defaultValue = "0") Integer sort) {
        int safePageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int safePageSize = (pageSize == null || pageSize < 1) ? 5 : pageSize;
        // 统一可见规则：仅返回已上架商品
        Page<PmsProduct> page = productService.list(keyword, brandId, productCategoryId, 1, null, safePageNum, safePageSize);
        return CommonResult.success(CommonPage.from(page));
    }

    private boolean isAppVisible(PmsProduct product) {
        return product != null && Integer.valueOf(1).equals(product.getPublishStatus());
    }
}
