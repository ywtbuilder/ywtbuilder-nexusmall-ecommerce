package com.mall.module.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("oms_order_setting")
public class OmsOrderSetting {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 秒杀订单超时关闭时间(分) */
    private Integer flashOrderOvertime;
    /** 正常订单超时时间(分) */
    private Integer normalOrderOvertime;
    /** 发货后自动确认收货时间（天） */
    private Integer confirmOvertime;
    /** 自动完成交易时间（天） */
    private Integer finishOvertime;
    /** 自动好评时间（天） */
    private Integer commentOvertime;
}
