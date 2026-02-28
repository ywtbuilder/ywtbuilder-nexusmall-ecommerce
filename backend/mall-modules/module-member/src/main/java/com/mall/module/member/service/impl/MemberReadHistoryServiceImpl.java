package com.mall.module.member.service.impl;

import com.mall.module.member.entity.MemberReadHistory;
import com.mall.module.member.repository.MemberReadHistoryRepository;
import com.mall.module.member.service.MemberReadHistoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemberReadHistoryServiceImpl implements MemberReadHistoryService {

    private final MemberReadHistoryRepository repository;

    public MemberReadHistoryServiceImpl(MemberReadHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public int create(MemberReadHistory readHistory) {
        readHistory.setId(null);
        readHistory.setCreateTime(LocalDateTime.now());
        repository.save(readHistory);
        return 1;
    }

    @Override
    public int delete(List<String> ids) {
        List<MemberReadHistory> list = repository.findAllById(ids);
        repository.deleteAll(list);
        return list.size();
    }

    @Override
    public int delete(List<String> ids, Long memberId) {
        // 仅删除属于当前用户的记录，防止越权删除他人浏览记录
        List<MemberReadHistory> list = repository.findAllById(ids);
        List<MemberReadHistory> owned = list.stream()
                .filter(h -> memberId.equals(h.getMemberId()))
                .collect(java.util.stream.Collectors.toList());
        repository.deleteAll(owned);
        return owned.size();
    }

    @Override
    public void clear(Long memberId) {
        repository.deleteAllByMemberId(memberId);
    }

    @Override
    public List<MemberReadHistory> list(Long memberId) {
        return repository.findByMemberIdOrderByCreateTimeDesc(memberId);
    }
}
