package com.mall.module.member.service;

import com.mall.module.member.entity.MemberReadHistory;

import java.util.List;

/**
 * 会员浏览记录服务（MongoDB）
 */
public interface MemberReadHistoryService {
    int create(MemberReadHistory readHistory);
    int delete(List<String> ids);
    /** 仅删除属于指定会员的记录 */
    int delete(List<String> ids, Long memberId);
    void clear(Long memberId);
    List<MemberReadHistory> list(Long memberId);
}
