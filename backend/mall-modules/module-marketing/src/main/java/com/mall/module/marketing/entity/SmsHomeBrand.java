package com.mall.module.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sms_home_brand")
public class SmsHomeBrand {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long brandId;
    private String brandName;
    private Integer recommendStatus;
    private Integer sort;
}
