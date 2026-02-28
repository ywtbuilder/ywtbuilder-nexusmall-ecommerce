package com.mall.module.marketing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.marketing.entity.SmsFlashPromotion;

import java.util.List;

/**
 * 限时购活动服务
 */
public interface FlashPromotionService {
    Page<SmsFlashPromotion> list(String keyword, Integer pageNum, Integer pageSize);
    int create(SmsFlashPromotion flashPromotion);
    int update(Long id, SmsFlashPromotion flashPromotion);
    int updateStatus(Long id, Integer status);
    int delete(Long id);
    SmsFlashPromotion getItem(Long id);
}
