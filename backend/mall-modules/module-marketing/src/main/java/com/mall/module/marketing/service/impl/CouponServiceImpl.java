package com.mall.module.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.marketing.entity.SmsCoupon;
import com.mall.module.marketing.entity.SmsCouponHistory;
import com.mall.module.marketing.mapper.SmsCouponHistoryMapper;
import com.mall.module.marketing.mapper.SmsCouponMapper;
import com.mall.module.marketing.service.CouponService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CouponServiceImpl implements CouponService {

    private final SmsCouponMapper couponMapper;
    private final SmsCouponHistoryMapper couponHistoryMapper;

    public CouponServiceImpl(SmsCouponMapper couponMapper, SmsCouponHistoryMapper couponHistoryMapper) {
        this.couponMapper = couponMapper;
        this.couponHistoryMapper = couponHistoryMapper;
    }

    @Override
    public Page<SmsCoupon> list(String name, Integer type, Integer pageNum, Integer pageSize) {
        Page<SmsCoupon> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SmsCoupon> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(SmsCoupon::getName, name);
        }
        if (type != null) {
            wrapper.eq(SmsCoupon::getType, type);
        }
        return couponMapper.selectPage(page, wrapper);
    }

    @Override
    public int create(SmsCoupon coupon) {
        return couponMapper.insert(coupon);
    }

    @Override
    public int update(Long id, SmsCoupon coupon) {
        coupon.setId(id);
        return couponMapper.updateById(coupon);
    }

    @Override
    public int delete(Long id) {
        return couponMapper.deleteById(id);
    }

    @Override
    public SmsCoupon getItem(Long id) {
        return couponMapper.selectById(id);
    }

    @Override
    @Transactional
    public int receive(Long couponId, Long memberId, String memberNickname) {
        SmsCoupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getCount() <= 0) {
            return 0;
        }
        // 判断是否超过限领数量
        long receivedCount = couponHistoryMapper.selectCount(
                new LambdaQueryWrapper<SmsCouponHistory>()
                        .eq(SmsCouponHistory::getCouponId, couponId)
                        .eq(SmsCouponHistory::getMemberId, memberId));
        if (coupon.getPerLimit() != null && receivedCount >= coupon.getPerLimit()) {
            return 0;
        }
        // 领取
        SmsCouponHistory history = new SmsCouponHistory();
        history.setCouponId(couponId);
        history.setMemberId(memberId);
        history.setMemberNickname(memberNickname);
        history.setCouponCode(UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        history.setGetType(1);
        history.setCreateTime(LocalDateTime.now());
        history.setUseStatus(0);
        couponHistoryMapper.insert(history);
        // 乐观扣减：防止高并发超领（UPDATE WHERE count > 0）
        int updated = couponMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SmsCoupon>()
                        .eq(SmsCoupon::getId, couponId)
                        .gt(SmsCoupon::getCount, 0)
                        .setSql("count = count - 1")
                        .setSql("receive_count = IFNULL(receive_count, 0) + 1"));
        if (updated == 0) {
            // 扣减失败，回滚领取记录
            throw new RuntimeException("优惠券已被领完");
        }
        return 1;
    }

    @Override
    public List<SmsCoupon> listByMember(Long memberId, Integer useStatus) {
        LambdaQueryWrapper<SmsCouponHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsCouponHistory::getMemberId, memberId);
        if (useStatus != null) {
            wrapper.eq(SmsCouponHistory::getUseStatus, useStatus);
        }
        List<SmsCouponHistory> histories = couponHistoryMapper.selectList(wrapper);
        List<Long> couponIds = histories.stream()
                .map(SmsCouponHistory::getCouponId)
                .distinct()
                .collect(Collectors.toList());
        if (couponIds.isEmpty()) return List.of();
        return couponMapper.selectByIds(couponIds);
    }
}
