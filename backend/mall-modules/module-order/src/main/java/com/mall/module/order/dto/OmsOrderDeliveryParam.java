package com.mall.module.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单发货参数
 */
@Data
public class OmsOrderDeliveryParam {
    private Long orderId;
    private String deliveryCompany;
    private String deliverySn;
}
