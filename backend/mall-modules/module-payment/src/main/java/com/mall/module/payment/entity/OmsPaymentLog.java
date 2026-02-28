package com.mall.module.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付日志（V3新增表 oms_payment_log）
 */
@Data
@TableName("oms_payment_log")
public class OmsPaymentLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderSn;
    /** 支付方式：0->未支付；1->支付宝；2->微信 */
    private Integer payType;
    /** 第三方交易号 */
    private String tradeNo;
    /** 支付金额 */
    private BigDecimal totalAmount;
    /** 支付状态：0->待支付；1->已支付；2->已退款 */
    private Integer payStatus;
    /** 回调内容 */
    private String callbackContent;
    /** 回调时间 */
    private LocalDateTime callbackTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
