package com.mall.module.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.marketing.entity.SmsFlashPromotionProductRelation;
import com.mall.module.marketing.mapper.SmsFlashPromotionProductRelationMapper;
import com.mall.module.marketing.service.FlashPromotionProductRelationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlashPromotionProductRelationServiceImpl implements FlashPromotionProductRelationService {

    private final SmsFlashPromotionProductRelationMapper relationMapper;

    public FlashPromotionProductRelationServiceImpl(SmsFlashPromotionProductRelationMapper relationMapper) {
        this.relationMapper = relationMapper;
    }

    @Override
    public int create(List<SmsFlashPromotionProductRelation> list) {
        int count = 0;
        for (SmsFlashPromotionProductRelation relation : list) {
            relationMapper.insert(relation);
            count++;
        }
        return count;
    }

    @Override
    public int update(Long id, SmsFlashPromotionProductRelation relation) {
        relation.setId(id);
        return relationMapper.updateById(relation);
    }

    @Override
    public int delete(Long id) {
        return relationMapper.deleteById(id);
    }

    @Override
    public SmsFlashPromotionProductRelation getItem(Long id) {
        return relationMapper.selectById(id);
    }

    @Override
    public Page<SmsFlashPromotionProductRelation> list(Long flashPromotionId, Long flashPromotionSessionId,
                                                        Integer pageNum, Integer pageSize) {
        Page<SmsFlashPromotionProductRelation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SmsFlashPromotionProductRelation> wrapper = new LambdaQueryWrapper<>();
        if (flashPromotionId != null) {
            wrapper.eq(SmsFlashPromotionProductRelation::getFlashPromotionId, flashPromotionId);
        }
        if (flashPromotionSessionId != null) {
            wrapper.eq(SmsFlashPromotionProductRelation::getFlashPromotionSessionId, flashPromotionSessionId);
        }
        return relationMapper.selectPage(page, wrapper);
    }
}
