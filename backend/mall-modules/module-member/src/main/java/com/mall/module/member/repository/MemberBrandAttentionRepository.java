package com.mall.module.member.repository;

import com.mall.module.member.entity.MemberBrandAttention;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MemberBrandAttentionRepository extends MongoRepository<MemberBrandAttention, String> {
    List<MemberBrandAttention> findByMemberIdOrderByCreateTimeDesc(Long memberId);
    Optional<MemberBrandAttention> findByMemberIdAndBrandId(Long memberId, Long brandId);
    void deleteByMemberIdAndBrandId(Long memberId, Long brandId);
    void deleteAllByMemberId(Long memberId);
}
