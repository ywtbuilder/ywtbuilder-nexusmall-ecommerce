package com.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("pms_product_ladder")
public class PmsProductLadder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    /** 满足的商品数量 */
    private Integer count;
    /** 折扣 */
    private BigDecimal discount;
    /** 折后价格 */
    private BigDecimal price;
}
