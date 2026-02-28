package com.mall.module.member.service;

import com.mall.module.member.entity.UmsMemberReceiveAddress;

import java.util.List;

/**
 * 会员收货地址服务
 */
public interface MemberAddressService {
    List<UmsMemberReceiveAddress> list(Long memberId);
    UmsMemberReceiveAddress getItem(Long id);
    int add(UmsMemberReceiveAddress address);
    int update(Long id, UmsMemberReceiveAddress address);
    int delete(Long id, Long memberId);
}
