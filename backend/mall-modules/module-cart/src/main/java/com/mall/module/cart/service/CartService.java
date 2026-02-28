package com.mall.module.cart.service;

import com.mall.module.cart.entity.OmsCartItem;

import java.util.List;

/**
 * 购物车服务
 */
public interface CartService {

    /** 添加商品到购物车 */
    int add(OmsCartItem cartItem);

    /** 获取购物车列表 */
    List<OmsCartItem> list(Long memberId);

    /** 获取购物车列表（含促销信息） */
    List<OmsCartItem> listPromotion(Long memberId, List<Long> cartIds);

    /** 修改购物车中商品数量 */
    int updateQuantity(Long id, Long memberId, Integer quantity);

    /** 获取购物车商品规格（重选规格） */
    OmsCartItem getCartProduct(Long productId);

    /** 修改购物车中商品规格 */
    int updateAttr(OmsCartItem cartItem);

    /** 删除购物车中指定商品 */
    int delete(Long memberId, List<Long> ids);

    /** 清空购物车 */
    int clear(Long memberId);
}
