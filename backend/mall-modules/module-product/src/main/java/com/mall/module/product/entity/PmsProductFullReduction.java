package com.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("pms_product_full_reduction")
public class PmsProductFullReduction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
}
