package com.mall.module.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.marketing.entity.SmsFlashPromotion;
import com.mall.module.marketing.mapper.SmsFlashPromotionMapper;
import com.mall.module.marketing.service.FlashPromotionService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class FlashPromotionServiceImpl implements FlashPromotionService {

    private final SmsFlashPromotionMapper flashPromotionMapper;

    public FlashPromotionServiceImpl(SmsFlashPromotionMapper flashPromotionMapper) {
        this.flashPromotionMapper = flashPromotionMapper;
    }

    @Override
    public Page<SmsFlashPromotion> list(String keyword, Integer pageNum, Integer pageSize) {
        Page<SmsFlashPromotion> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SmsFlashPromotion> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SmsFlashPromotion::getTitle, keyword);
        }
        return flashPromotionMapper.selectPage(page, wrapper);
    }

    @Override
    public int create(SmsFlashPromotion flashPromotion) {
        flashPromotion.setCreateTime(LocalDateTime.now());
        return flashPromotionMapper.insert(flashPromotion);
    }

    @Override
    public int update(Long id, SmsFlashPromotion flashPromotion) {
        flashPromotion.setId(id);
        return flashPromotionMapper.updateById(flashPromotion);
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        SmsFlashPromotion fp = new SmsFlashPromotion();
        fp.setId(id);
        fp.setStatus(status);
        return flashPromotionMapper.updateById(fp);
    }

    @Override
    public int delete(Long id) {
        return flashPromotionMapper.deleteById(id);
    }

    @Override
    public SmsFlashPromotion getItem(Long id) {
        return flashPromotionMapper.selectById(id);
    }
}
