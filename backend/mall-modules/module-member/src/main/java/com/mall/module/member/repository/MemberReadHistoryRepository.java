package com.mall.module.member.repository;

import com.mall.module.member.entity.MemberReadHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MemberReadHistoryRepository extends MongoRepository<MemberReadHistory, String> {
    List<MemberReadHistory> findByMemberIdOrderByCreateTimeDesc(Long memberId);
    void deleteAllByMemberId(Long memberId);
}
