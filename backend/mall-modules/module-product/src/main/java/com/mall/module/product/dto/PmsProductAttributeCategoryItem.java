package com.mall.module.product.dto;

import com.mall.module.product.entity.PmsProductAttribute;
import com.mall.module.product.entity.PmsProductAttributeCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 包含有属性列表的商品属性分类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PmsProductAttributeCategoryItem extends PmsProductAttributeCategory {
    private List<PmsProductAttribute> productAttributeList;
}
