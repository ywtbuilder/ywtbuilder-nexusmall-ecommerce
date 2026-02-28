package com.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品-图片关联表
 * image_type: 0=轮播图 1=详情图 2=规格图 3=SKU图 4=评论图 5=其他
 */
@Data
@TableName("pms_product_image")
public class PmsProductImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long assetId;
    /** 0=轮播图 1=详情图 2=规格图 3=SKU图 4=评论图 5=其他 */
    private Integer imageType;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
