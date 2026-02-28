package com.mall.module.marketing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.marketing.entity.SmsFlashPromotionProductRelation;

import java.util.List;

/**
 * 限时购商品关联服务
 */
public interface FlashPromotionProductRelationService {

    /** 批量添加关联 */
    int create(List<SmsFlashPromotionProductRelation> list);

    /** 修改关联信息 */
    int update(Long id, SmsFlashPromotionProductRelation relation);

    /** 删除关联 */
    int delete(Long id);

    /** 获取关联详情 */
    SmsFlashPromotionProductRelation getItem(Long id);

    /** 分页查询 */
    Page<SmsFlashPromotionProductRelation> list(Long flashPromotionId, Long flashPromotionSessionId,
                                                 Integer pageNum, Integer pageSize);
}
