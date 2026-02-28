package com.mall.module.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.module.cart.entity.OmsCartItem;
import com.mall.module.cart.mapper.OmsCartItemMapper;
import com.mall.module.cart.service.CartService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final OmsCartItemMapper cartItemMapper;

    public CartServiceImpl(OmsCartItemMapper cartItemMapper) {
        this.cartItemMapper = cartItemMapper;
    }

    @Override
    public int add(OmsCartItem cartItem) {
        cartItem.setDeleteStatus(0);
        cartItem.setCreateDate(LocalDateTime.now());
        cartItem.setModifyDate(LocalDateTime.now());
        // 检查是否已存在相同商品和SKU
        OmsCartItem existing = getExistCartItem(cartItem);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + cartItem.getQuantity());
            existing.setModifyDate(LocalDateTime.now());
            return cartItemMapper.updateById(existing);
        }
        return cartItemMapper.insert(cartItem);
    }

    @Override
    public List<OmsCartItem> list(Long memberId) {
        return cartItemMapper.selectList(
                new LambdaQueryWrapper<OmsCartItem>()
                        .eq(OmsCartItem::getMemberId, memberId)
                        .eq(OmsCartItem::getDeleteStatus, 0));
    }

    @Override
    public List<OmsCartItem> listPromotion(Long memberId, List<Long> cartIds) {
        // 简化：返回指定购物车项（促销计算留到BFF层或后续细化）
        LambdaQueryWrapper<OmsCartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsCartItem::getMemberId, memberId)
                .eq(OmsCartItem::getDeleteStatus, 0);
        if (cartIds != null && !cartIds.isEmpty()) {
            wrapper.in(OmsCartItem::getId, cartIds);
        }
        return cartItemMapper.selectList(wrapper);
    }

    @Override
    public int updateQuantity(Long id, Long memberId, Integer quantity) {
        OmsCartItem cartItem = new OmsCartItem();
        cartItem.setQuantity(quantity);
        cartItem.setModifyDate(LocalDateTime.now());
        return cartItemMapper.update(cartItem,
                new LambdaUpdateWrapper<OmsCartItem>()
                        .eq(OmsCartItem::getId, id)
                        .eq(OmsCartItem::getMemberId, memberId));
    }

    @Override
    public OmsCartItem getCartProduct(Long productId) {
        return cartItemMapper.selectOne(
                new LambdaQueryWrapper<OmsCartItem>()
                        .eq(OmsCartItem::getProductId, productId)
                        .eq(OmsCartItem::getDeleteStatus, 0)
                        .last("LIMIT 1"));
    }

    @Override
    public int updateAttr(OmsCartItem cartItem) {
        cartItem.setModifyDate(LocalDateTime.now());
        return cartItemMapper.updateById(cartItem);
    }

    @Override
    public int delete(Long memberId, List<Long> ids) {
        // 注意：不能用 update(entity, wrapper) 方式——MyBatis-Plus 全局逻辑删除会自动
        // 在 WHERE 里追加 AND delete_status=0，与 LambdaUpdateWrapper 的 WHERE 子句
        // 合并时产生双重 WHERE 语法错误。
        // 解决：用 LambdaUpdateWrapper.set() 直接设置字段值，entity 传 null。
        return cartItemMapper.update(null,
                new LambdaUpdateWrapper<OmsCartItem>()
                        .set(OmsCartItem::getDeleteStatus, 1)
                        .eq(OmsCartItem::getMemberId, memberId)
                        .in(OmsCartItem::getId, ids));
    }

    @Override
    public int clear(Long memberId) {
        return cartItemMapper.update(null,
                new LambdaUpdateWrapper<OmsCartItem>()
                        .set(OmsCartItem::getDeleteStatus, 1)
                        .eq(OmsCartItem::getMemberId, memberId)
                        .eq(OmsCartItem::getDeleteStatus, 0));
    }

    private OmsCartItem getExistCartItem(OmsCartItem cartItem) {
        LambdaQueryWrapper<OmsCartItem> wrapper = new LambdaQueryWrapper<OmsCartItem>()
                .eq(OmsCartItem::getMemberId, cartItem.getMemberId())
                .eq(OmsCartItem::getProductId, cartItem.getProductId())
                .eq(OmsCartItem::getDeleteStatus, 0);
        // 注意：MyBatis-Plus 的 .eq(column, null) 生成 "= NULL"（SQL 中永为 false），
        // 必须显式用 isNull() / eq() 分开处理，才能正确匹配 SKU 为空的行
        if (cartItem.getProductSkuId() != null) {
            wrapper.eq(OmsCartItem::getProductSkuId, cartItem.getProductSkuId());
        } else {
            wrapper.isNull(OmsCartItem::getProductSkuId);
        }
        return cartItemMapper.selectOne(wrapper.last("LIMIT 1"));
    }
}
