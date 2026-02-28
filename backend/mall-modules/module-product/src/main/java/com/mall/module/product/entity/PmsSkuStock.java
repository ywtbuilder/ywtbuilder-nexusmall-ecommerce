package com.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("pms_sku_stock")
public class PmsSkuStock {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    /** sku编码 */
    private String skuCode;
    private BigDecimal price;
    /** 库存 */
    private Integer stock;
    /** 预警库存 */
    private Integer lowStock;
    /** 展示图片 */
    private String pic;
    /** 销量 */
    private Integer sale;
    /** 单品促销价格 */
    private BigDecimal promotionPrice;
    /** 锁定库存 */
    private Integer lockStock;
    /** 商品销售属性，json格式 */
    private String spData;
}
