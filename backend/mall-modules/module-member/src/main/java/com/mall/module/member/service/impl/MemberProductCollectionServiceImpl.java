package com.mall.module.member.service.impl;

import com.mall.module.member.entity.MemberProductCollection;
import com.mall.module.member.repository.MemberProductCollectionRepository;
import com.mall.module.member.service.MemberProductCollectionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemberProductCollectionServiceImpl implements MemberProductCollectionService {

    private final MemberProductCollectionRepository repository;

    public MemberProductCollectionServiceImpl(MemberProductCollectionRepository repository) {
        this.repository = repository;
    }

    @Override
    public int add(MemberProductCollection productCollection) {
        productCollection.setId(null);
        productCollection.setCreateTime(LocalDateTime.now());
        repository.save(productCollection);
        return 1;
    }

    @Override
    public int delete(Long memberId, Long productId) {
        repository.deleteByMemberIdAndProductId(memberId, productId);
        return 1;
    }

    @Override
    public List<MemberProductCollection> list(Long memberId) {
        return repository.findByMemberIdOrderByCreateTimeDesc(memberId);
    }

    @Override
    public MemberProductCollection detail(Long memberId, Long productId) {
        return repository.findByMemberIdAndProductId(memberId, productId).orElse(null);
    }

    @Override
    public void clear(Long memberId) {
        repository.deleteAllByMemberId(memberId);
    }
}
