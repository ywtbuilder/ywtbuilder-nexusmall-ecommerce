package com.mall.module.product.dto;

import com.mall.module.product.entity.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 创建和修改商品时的参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PmsProductParam extends PmsProduct {
    /** 商品阶梯价格设置 */
    private List<PmsProductLadder> productLadderList;
    /** 商品满减价格设置 */
    private List<PmsProductFullReduction> productFullReductionList;
    /** 商品会员价格设置 */
    private List<PmsMemberPrice> memberPriceList;
    /** sku库存信息 */
    private List<PmsSkuStock> skuStockList;
    /** 商品参数及自定义规格属性 */
    private List<PmsProductAttributeValue> productAttributeValueList;
}
