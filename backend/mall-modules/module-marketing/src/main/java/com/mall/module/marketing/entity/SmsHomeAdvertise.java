package com.mall.module.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sms_home_advertise")
public class SmsHomeAdvertise {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    /** 轮播位置：0->PC首页轮播；1->app首页轮播 */
    private Integer type;
    private String pic;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /** 上下线状态：0->下线；1->上线 */
    private Integer status;
    /** 点击数 */
    private Integer clickCount;
    /** 下单数 */
    private Integer orderCount;
    /** 链接地址 */
    private String url;
    /** 备注 */
    private String note;
    /** 排序 */
    private Integer sort;
}
