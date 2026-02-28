package com.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_product_category_attribute_relation")
public class PmsProductCategoryAttributeRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productCategoryId;
    private Long productAttributeId;
}
