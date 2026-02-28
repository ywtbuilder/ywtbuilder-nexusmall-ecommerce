package com.mall.module.product.dto;

import com.mall.module.product.entity.PmsProductCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 包含子分类的商品分类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PmsProductCategoryWithChildrenItem extends PmsProductCategory {
    private List<PmsProductCategory> children;
}
