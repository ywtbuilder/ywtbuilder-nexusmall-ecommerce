package com.mall.module.member.service;

import com.mall.module.member.entity.MemberBrandAttention;

import java.util.List;

/**
 * 会员品牌关注服务（MongoDB）
 */
public interface MemberBrandAttentionService {
    int add(MemberBrandAttention brandAttention);
    int delete(Long memberId, Long brandId);
    List<MemberBrandAttention> list(Long memberId);
    MemberBrandAttention detail(Long memberId, Long brandId);
    void clear(Long memberId);
}
