package com.mall.module.member.service;

import com.mall.module.member.entity.MemberProductCollection;

import java.util.List;

/**
 * 会员商品收藏服务（MongoDB）
 */
public interface MemberProductCollectionService {
    int add(MemberProductCollection productCollection);
    int delete(Long memberId, Long productId);
    List<MemberProductCollection> list(Long memberId);
    MemberProductCollection detail(Long memberId, Long productId);
    void clear(Long memberId);
}
