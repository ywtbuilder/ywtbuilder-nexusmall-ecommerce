package com.mall.app.controller;

import com.mall.common.api.CommonResult;
import com.mall.module.cart.entity.OmsCartItem;
import com.mall.module.cart.service.CartService;
import com.mall.module.member.entity.UmsMember;
import com.mall.module.member.service.MemberService;
import com.mall.module.product.entity.PmsProduct;
import com.mall.module.product.entity.PmsSkuStock;
import com.mall.module.product.mapper.PmsSkuStockMapper;
import com.mall.module.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车管理 — 对标 V1 OmsCartItemController 全部 8 端点
 * <p>
 * V2 仅有 add + list，V3 补齐 promotion/quantity/attr/delete/clear/getProduct
 */
@Tag(name = "Cart", description = "购物车")
@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final MemberService memberService;
    private final ProductService productService;
    private final PmsSkuStockMapper skuStockMapper;

    public CartController(CartService cartService, MemberService memberService,
                          ProductService productService, PmsSkuStockMapper skuStockMapper) {
        this.cartService = cartService;
        this.memberService = memberService;
        this.productService = productService;
        this.skuStockMapper = skuStockMapper;
    }

    private Long getCurrentMemberId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UmsMember member = memberService.getMemberByUsername(username);
        return member.getId();
    }

    @Operation(summary = "添加商品到购物车")
    @PostMapping("/add")
    public CommonResult<Integer> add(@RequestBody OmsCartItem cartItem) {
        cartItem.setMemberId(getCurrentMemberId());

        // 从数据库查询商品信息，确保价格/名称/图片准确，避免前端伪造或数据缺失
        if (cartItem.getProductId() == null) {
            return CommonResult.failed("productId 不能为空");
        }
        PmsProduct product = productService.getById(cartItem.getProductId());
        if (product == null || Integer.valueOf(1).equals(product.getDeleteStatus())
                || !Integer.valueOf(1).equals(product.getPublishStatus())) {
            return CommonResult.failed("商品不存在或已下架");
        }
        // 用数据库权威数据覆盖前端传入的商品元数据
        cartItem.setProductName(product.getName());
        cartItem.setProductSubTitle(product.getSubTitle());
        cartItem.setProductSn(product.getProductSn());
        cartItem.setProductCategoryId(product.getProductCategoryId());
        cartItem.setProductBrand(product.getBrandName());
        cartItem.setProductPic(product.getPic());
        cartItem.setPrice(product.getPrice());

        // 如果指定了 SKU，用 SKU 的价格和图片（更精准）
        if (cartItem.getProductSkuId() != null) {
            PmsSkuStock sku = skuStockMapper.selectById(cartItem.getProductSkuId());
            if (sku != null) {
                cartItem.setProductSkuCode(sku.getSkuCode());
                cartItem.setProductAttr(sku.getSpData());
                if (sku.getPrice() != null) {
                    cartItem.setPrice(sku.getPrice());
                }
                if (sku.getPic() != null && !sku.getPic().isEmpty()) {
                    cartItem.setProductPic(sku.getPic());
                }
            }
        }

        int count = cartService.add(cartItem);
        return CommonResult.success(count);
    }

    @Operation(summary = "购物车列表")
    @GetMapping("/list")
    public CommonResult<List<OmsCartItem>> list() {
        List<OmsCartItem> cartItemList = cartService.list(getCurrentMemberId());
        return CommonResult.success(cartItemList);
    }

    @Operation(summary = "购物车列表（含促销信息）")
    @GetMapping("/list/promotion")
    public CommonResult<List<OmsCartItem>> listPromotion(@RequestParam(required = false) List<Long> cartIds) {
        List<OmsCartItem> cartItemList = cartService.listPromotion(getCurrentMemberId(), cartIds);
        return CommonResult.success(cartItemList);
    }

    @Operation(summary = "修改购物车中商品数量")
    @GetMapping("/update/quantity")
    public CommonResult<Integer> updateQuantity(@RequestParam Long id,
                                                 @RequestParam Integer quantity) {
        int count = cartService.updateQuantity(id, getCurrentMemberId(), quantity);
        return CommonResult.success(count);
    }

    @Operation(summary = "获取购物车商品规格（重选规格）")
    @GetMapping("/getProduct/{productId}")
    public CommonResult<OmsCartItem> getCartProduct(@PathVariable Long productId) {
        // getCartProduct 查询的是商品信息用于重选规格，非购物车项，无需 memberId
        OmsCartItem cartItem = cartService.getCartProduct(productId);
        return CommonResult.success(cartItem);
    }

    @Operation(summary = "修改购物车中商品规格")
    @PostMapping("/update/attr")
    public CommonResult<Integer> updateAttr(@RequestBody OmsCartItem cartItem) {
        // 覆写 memberId 为当前登录用户，防止越权修改他人购物车
        cartItem.setMemberId(getCurrentMemberId());
        int count = cartService.updateAttr(cartItem);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除购物车中指定商品")
    @PostMapping("/delete")
    public CommonResult<Integer> delete(@RequestParam List<Long> ids) {
        int count = cartService.delete(getCurrentMemberId(), ids);
        return CommonResult.success(count);
    }

    @Operation(summary = "清空购物车")
    @PostMapping("/clear")
    public CommonResult<Integer> clear() {
        int count = cartService.clear(getCurrentMemberId());
        return CommonResult.success(count);
    }
}
