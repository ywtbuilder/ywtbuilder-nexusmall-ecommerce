package com.mall.module.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 修改订单收货人信息参数
 */
@Data
public class OmsReceiverInfoParam {
    private Long orderId;
    private String receiverName;
    private String receiverPhone;
    private String receiverPostCode;
    private String receiverProvince;
    private String receiverCity;
    private String receiverRegion;
    private String receiverDetailAddress;
    /** 操作状态 */
    private Integer status;
}
