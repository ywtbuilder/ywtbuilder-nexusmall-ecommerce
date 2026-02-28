package com.mall.module.member.service.impl;

import com.mall.module.member.entity.MemberBrandAttention;
import com.mall.module.member.repository.MemberBrandAttentionRepository;
import com.mall.module.member.service.MemberBrandAttentionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemberBrandAttentionServiceImpl implements MemberBrandAttentionService {

    private final MemberBrandAttentionRepository repository;

    public MemberBrandAttentionServiceImpl(MemberBrandAttentionRepository repository) {
        this.repository = repository;
    }

    @Override
    public int add(MemberBrandAttention brandAttention) {
        brandAttention.setId(null);
        brandAttention.setCreateTime(LocalDateTime.now());
        repository.save(brandAttention);
        return 1;
    }

    @Override
    public int delete(Long memberId, Long brandId) {
        repository.deleteByMemberIdAndBrandId(memberId, brandId);
        return 1;
    }

    @Override
    public List<MemberBrandAttention> list(Long memberId) {
        return repository.findByMemberIdOrderByCreateTimeDesc(memberId);
    }

    @Override
    public MemberBrandAttention detail(Long memberId, Long brandId) {
        return repository.findByMemberIdAndBrandId(memberId, brandId).orElse(null);
    }

    @Override
    public void clear(Long memberId) {
        repository.deleteAllByMemberId(memberId);
    }
}
