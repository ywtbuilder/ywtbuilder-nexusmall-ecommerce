package com.mall.module.product.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询单个商品修改后的返回数据（包含父分类id）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PmsProductResult extends PmsProductParam {
    /** 商品所选分类的父id */
    private Long cateParentId;
}
