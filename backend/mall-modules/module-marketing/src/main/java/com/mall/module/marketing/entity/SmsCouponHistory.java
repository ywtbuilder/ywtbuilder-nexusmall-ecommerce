package com.mall.module.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sms_coupon_history")
public class SmsCouponHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long couponId;
    private Long memberId;
    private String couponCode;
    /** 领取人昵称 */
    private String memberNickname;
    /** 获取类型：0->后台赠送；1->主动获取 */
    private Integer getType;
    private LocalDateTime createTime;
    /** 使用状态：0->未使用；1->已使用；2->已过期 */
    private Integer useStatus;
    /** 使用时间 */
    private LocalDateTime useTime;
    /** 订单编号 */
    private Long orderId;
    private String orderSn;
}
