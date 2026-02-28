package com.mall.module.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("oms_order")
public class OmsOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private Long couponId;
    /** 订单编号 */
    private String orderSn;
    private LocalDateTime createTime;
    /** 用户帐号 */
    private String memberUsername;
    /** 订单总金额 */
    private BigDecimal totalAmount;
    /** 应付金额 */
    private BigDecimal payAmount;
    /** 运费金额 */
    private BigDecimal freightAmount;
    /** 促销优化金额 */
    private BigDecimal promotionAmount;
    /** 积分抵扣金额 */
    private BigDecimal integrationAmount;
    /** 优惠券抵扣金额 */
    private BigDecimal couponAmount;
    /** 管理员后台调整订单使用的折扣金额 */
    private BigDecimal discountAmount;
    /** 支付方式：0->未支付；1->支付宝；2->微信 */
    private Integer payType;
    /** 订单来源：0->PC订单；1->app订单 */
    private Integer sourceType;
    /** 订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单 */
    private Integer status;
    /** 订单类型：0->正常订单；1->秒杀订单 */
    private Integer orderType;
    /** 物流公司 */
    private String deliveryCompany;
    /** 物流单号 */
    private String deliverySn;
    /** 自动确认时间（天） */
    private Integer autoConfirmDay;
    /** 可以获得的积分 */
    private Integer integration;
    /** 可以获得的成长值 */
    private Integer growth;
    /** 活动信息 */
    private String promotionInfo;
    /** 发票类型 */
    private Integer billType;
    private String billHeader;
    private String billContent;
    private String billReceiverPhone;
    private String billReceiverEmail;
    /** 收货人 */
    private String receiverName;
    private String receiverPhone;
    private String receiverPostCode;
    private String receiverProvince;
    private String receiverCity;
    private String receiverRegion;
    private String receiverDetailAddress;
    /** 订单备注 */
    private String note;
    /** 确认收货状态：0->未确认；1->已确认 */
    private Integer confirmStatus;
    /** 删除状态：0->未删除；1->已删除 */
    private Integer deleteStatus;
    /** 下单时使用的积分 */
    private Integer useIntegration;
    private LocalDateTime paymentTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime receiveTime;
    private LocalDateTime commentTime;
    private LocalDateTime modifyTime;
    /** V3新增：第三方支付单号 */
    private String paymentId;
}
