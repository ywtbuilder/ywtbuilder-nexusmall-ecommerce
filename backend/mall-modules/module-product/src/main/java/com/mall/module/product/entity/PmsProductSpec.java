package com.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商品规格参数（结构化 key-value）
 */
@Data
@TableName("pms_product_spec")
public class PmsProductSpec {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    /** 参数组名（如"基本参数"） */
    private String specGroup;
    /** 参数名 */
    private String specName;
    /** 参数值 */
    private String specValue;
    private Integer sortOrder;
}
