package com.mall.module.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.module.marketing.entity.SmsFlashPromotionSession;
import com.mall.module.marketing.mapper.SmsFlashPromotionSessionMapper;
import com.mall.module.marketing.service.FlashPromotionSessionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FlashPromotionSessionServiceImpl implements FlashPromotionSessionService {

    private final SmsFlashPromotionSessionMapper sessionMapper;

    public FlashPromotionSessionServiceImpl(SmsFlashPromotionSessionMapper sessionMapper) {
        this.sessionMapper = sessionMapper;
    }

    @Override
    public int create(SmsFlashPromotionSession session) {
        session.setCreateTime(LocalDateTime.now());
        return sessionMapper.insert(session);
    }

    @Override
    public int update(Long id, SmsFlashPromotionSession session) {
        session.setId(id);
        return sessionMapper.updateById(session);
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        SmsFlashPromotionSession session = new SmsFlashPromotionSession();
        session.setId(id);
        session.setStatus(status);
        return sessionMapper.updateById(session);
    }

    @Override
    public int delete(Long id) {
        return sessionMapper.deleteById(id);
    }

    @Override
    public SmsFlashPromotionSession getItem(Long id) {
        return sessionMapper.selectById(id);
    }

    @Override
    public List<SmsFlashPromotionSession> listAll() {
        return sessionMapper.selectList(null);
    }

    @Override
    public List<SmsFlashPromotionSession> selectList(Long flashPromotionId) {
        // 返回所有启用的场次
        return sessionMapper.selectList(
                new LambdaQueryWrapper<SmsFlashPromotionSession>()
                        .eq(SmsFlashPromotionSession::getStatus, 1));
    }
}
