package com.mall.module.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 修改订单费用参数
 */
@Data
public class OmsMoneyInfoParam {
    private Long orderId;
    private BigDecimal freightAmount;
    private BigDecimal discountAmount;
    /** 操作状态 */
    private Integer status;
}
