package com.mall.module.marketing.service;

import com.mall.module.marketing.entity.SmsFlashPromotionSession;

import java.util.List;

/**
 * 限时购场次服务
 */
public interface FlashPromotionSessionService {

    /** 创建场次 */
    int create(SmsFlashPromotionSession session);

    /** 修改场次 */
    int update(Long id, SmsFlashPromotionSession session);

    /** 修改启用状态 */
    int updateStatus(Long id, Integer status);

    /** 删除场次 */
    int delete(Long id);

    /** 获取场次详情 */
    SmsFlashPromotionSession getItem(Long id);

    /** 获取全部场次 */
    List<SmsFlashPromotionSession> listAll();

    /** 获取全部可选场次及其数量 */
    List<SmsFlashPromotionSession> selectList(Long flashPromotionId);
}
