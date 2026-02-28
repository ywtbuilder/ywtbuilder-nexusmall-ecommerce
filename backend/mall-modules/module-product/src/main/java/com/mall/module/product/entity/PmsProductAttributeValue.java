package com.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_product_attribute_value")
public class PmsProductAttributeValue {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long productAttributeId;
    /** 手动添加规格或参数的值，参数单值，规格有多个时以逗号隔开 */
    private String value;
}
