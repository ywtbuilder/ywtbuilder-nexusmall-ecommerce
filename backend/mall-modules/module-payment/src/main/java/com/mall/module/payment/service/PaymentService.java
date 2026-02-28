package com.mall.module.payment.service;

import com.mall.module.payment.entity.OmsPaymentLog;

/**
 * 支付服务抽象接口
 * <p>
 * 第一阶段提供 MockPaymentService 实现（模拟支付成功），
 * 后续可替换为 AlipayPaymentService / WechatPaymentService。
 */
public interface PaymentService {

    /**
     * 创建支付单
     *
     * @param orderId     订单 ID
     * @param payType     支付方式 (1-支付宝, 2-微信)
     * @param totalAmount 订单应付金额（由调用方从订单服务获取）
     * @return 支付日志
     */
    OmsPaymentLog createPayment(Long orderId, Integer payType, java.math.BigDecimal totalAmount);

    /**
     * 处理异步回调通知
     *
     * @param tradeNo 交易号
     * @param callbackContent 回调数据
     * @return 处理结果(1-成功)
     */
    int handleNotify(String tradeNo, String callbackContent);

    /**
     * 查询支付状态
     *
     * @param orderId 订单 ID
     * @return 支付日志(含状态)
     */
    OmsPaymentLog queryPaymentStatus(Long orderId);
}
