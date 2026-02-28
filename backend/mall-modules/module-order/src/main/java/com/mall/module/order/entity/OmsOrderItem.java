package com.mall.module.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("oms_order_item")
public class OmsOrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderSn;
    private Long productId;
    private String productPic;
    private String productName;
    private String productBrand;
    private String productSn;
    private BigDecimal productPrice;
    /** 购买数量 */
    private Integer productQuantity;
    private Long productSkuId;
    private String productSkuCode;
    private Long productCategoryId;
    /** 商品促销名称 */
    private String promotionName;
    private BigDecimal promotionAmount;
    private BigDecimal couponAmount;
    private BigDecimal integrationAmount;
    /** 该商品经过优惠后的分解金额 */
    private BigDecimal realAmount;
    private Integer giftIntegration;
    private Integer giftGrowth;
    /** 商品销售属性 */
    private String productAttr;
}
