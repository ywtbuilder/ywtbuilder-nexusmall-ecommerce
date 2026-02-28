package com.mall.module.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("sms_flash_promotion_product_relation")
public class SmsFlashPromotionProductRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long flashPromotionId;
    private Long flashPromotionSessionId;
    private Long productId;
    /** 限时购价格 */
    private BigDecimal flashPromotionPrice;
    /** 限时购数量 */
    private Integer flashPromotionCount;
    /** 每人限购数量 */
    private Integer flashPromotionLimit;
    /** 排序 */
    private Integer sort;
}
