package com.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("pms_member_price")
public class PmsMemberPrice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long memberLevelId;
    /** 会员价格 */
    private BigDecimal memberPrice;
    private String memberLevelName;
}
