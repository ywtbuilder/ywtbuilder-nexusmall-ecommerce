package com.mall.module.member.repository;

import com.mall.module.member.entity.MemberProductCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MemberProductCollectionRepository extends MongoRepository<MemberProductCollection, String> {
    List<MemberProductCollection> findByMemberIdOrderByCreateTimeDesc(Long memberId);
    Optional<MemberProductCollection> findByMemberIdAndProductId(Long memberId, Long productId);
    void deleteByMemberIdAndProductId(Long memberId, Long productId);
    void deleteAllByMemberId(Long memberId);
}
