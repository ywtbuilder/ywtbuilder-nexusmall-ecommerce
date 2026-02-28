package com.mall.module.product.dto;

import lombok.Data;

import java.util.List;

/**
 * 商品分类参数
 */
@Data
public class PmsProductCategoryParam {
    private Long parentId;
    private String name;
    private String productUnit;
    private Integer navStatus;
    private Integer showStatus;
    private Integer sort;
    private String icon;
    private String keywords;
    private String description;
    private List<Long> productAttributeIdList;
}
