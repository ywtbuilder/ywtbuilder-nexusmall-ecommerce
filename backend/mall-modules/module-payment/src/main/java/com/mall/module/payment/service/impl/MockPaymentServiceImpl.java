package com.mall.module.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.module.payment.entity.OmsPaymentLog;
import com.mall.module.payment.mapper.OmsPaymentLogMapper;
import com.mall.module.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 模拟支付服务实现（第一阶段使用，后续替换为真实支付网关）
 */
@Service
public class MockPaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentServiceImpl.class);

    private final OmsPaymentLogMapper paymentLogMapper;

    public MockPaymentServiceImpl(OmsPaymentLogMapper paymentLogMapper) {
        this.paymentLogMapper = paymentLogMapper;
    }

    @Override
    public OmsPaymentLog createPayment(Long orderId, Integer payType, BigDecimal totalAmount) {
        OmsPaymentLog paymentLog = new OmsPaymentLog();
        paymentLog.setOrderId(orderId);
        paymentLog.setOrderSn("MOCK_" + orderId);
        paymentLog.setPayType(payType);
        paymentLog.setTradeNo("MOCK_TRADE_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        paymentLog.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
        paymentLog.setPayStatus(0); // 待支付
        paymentLog.setCreateTime(LocalDateTime.now());
        paymentLog.setUpdateTime(LocalDateTime.now());
        paymentLogMapper.insert(paymentLog);
        log.info("[MockPayment] 创建模拟支付单: orderId={}, tradeNo={}", orderId, paymentLog.getTradeNo());

        // 模拟支付成功：自动将状态设为已支付
        paymentLog.setPayStatus(1);
        paymentLog.setCallbackContent("{\"mock\":true,\"result\":\"success\"}");
        paymentLog.setCallbackTime(LocalDateTime.now());
        paymentLog.setUpdateTime(LocalDateTime.now());
        paymentLogMapper.updateById(paymentLog);
        log.info("[MockPayment] 模拟支付成功: orderId={}", orderId);

        return paymentLog;
    }

    @Override
    public int handleNotify(String tradeNo, String callbackContent) {
        OmsPaymentLog paymentLog = paymentLogMapper.selectOne(
                new LambdaQueryWrapper<OmsPaymentLog>()
                        .eq(OmsPaymentLog::getTradeNo, tradeNo));
        if (paymentLog == null) {
            log.warn("[MockPayment] 未找到交易号: {}", tradeNo);
            return 0;
        }
        paymentLog.setPayStatus(1);
        paymentLog.setCallbackContent(callbackContent);
        paymentLog.setCallbackTime(LocalDateTime.now());
        paymentLog.setUpdateTime(LocalDateTime.now());
        paymentLogMapper.updateById(paymentLog);
        return 1;
    }

    @Override
    public OmsPaymentLog queryPaymentStatus(Long orderId) {
        return paymentLogMapper.selectOne(
                new LambdaQueryWrapper<OmsPaymentLog>()
                        .eq(OmsPaymentLog::getOrderId, orderId)
                        .orderByDesc(OmsPaymentLog::getCreateTime)
                        .last("LIMIT 1"));
    }
}
