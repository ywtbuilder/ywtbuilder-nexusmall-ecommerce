package com.mall.module.product.dto;

import lombok.Data;

/**
 * 商品查询参数
 */
@Data
public class PmsProductQueryParam {
    private String keyword;
    private String productSn;
    private Long productCategoryId;
    private Long brandId;
    private Integer publishStatus;
    private Integer verifyStatus;
}
