package com.mall.module.marketing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.marketing.entity.SmsCoupon;

import java.util.List;

/**
 * 优惠券服务
 */
public interface CouponService {
    Page<SmsCoupon> list(String name, Integer type, Integer pageNum, Integer pageSize);
    int create(SmsCoupon coupon);
    int update(Long id, SmsCoupon coupon);
    int delete(Long id);
    SmsCoupon getItem(Long id);
    /** 前台用户领取优惠券 */
    int receive(Long couponId, Long memberId, String memberNickname);
    /** 获取用户优惠券列表 */
    List<SmsCoupon> listByMember(Long memberId, Integer useStatus);
}
