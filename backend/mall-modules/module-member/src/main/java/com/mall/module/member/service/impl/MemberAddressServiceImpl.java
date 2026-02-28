package com.mall.module.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.module.member.entity.UmsMemberReceiveAddress;
import com.mall.module.member.mapper.UmsMemberReceiveAddressMapper;
import com.mall.module.member.service.MemberAddressService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberAddressServiceImpl implements MemberAddressService {

    private final UmsMemberReceiveAddressMapper addressMapper;

    public MemberAddressServiceImpl(UmsMemberReceiveAddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @Override
    public List<UmsMemberReceiveAddress> list(Long memberId) {
        return addressMapper.selectList(
                new LambdaQueryWrapper<UmsMemberReceiveAddress>()
                        .eq(UmsMemberReceiveAddress::getMemberId, memberId));
    }

    @Override
    public UmsMemberReceiveAddress getItem(Long id) {
        return addressMapper.selectById(id);
    }

    @Override
    public int add(UmsMemberReceiveAddress address) {
        return addressMapper.insert(address);
    }

    @Override
    public int update(Long id, UmsMemberReceiveAddress address) {
        address.setId(id);
        return addressMapper.updateById(address);
    }

    @Override
    public int delete(Long id, Long memberId) {
        return addressMapper.delete(
                new LambdaQueryWrapper<UmsMemberReceiveAddress>()
                        .eq(UmsMemberReceiveAddress::getId, id)
                        .eq(UmsMemberReceiveAddress::getMemberId, memberId));
    }
}
