package com.mall.module.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("oms_order_return_apply")
public class OmsOrderReturnApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long companyAddressId;
    private Long productId;
    private String orderSn;
    private LocalDateTime createTime;
    private String memberUsername;
    private BigDecimal returnAmount;
    private String returnName;
    private String returnPhone;
    /** 申请状态：0->待处理；1->退货中；2->已完成；3->已拒绝 */
    private Integer status;
    private LocalDateTime handleTime;
    private String productPic;
    private String productName;
    private String productBrand;
    /** 退货商品属性 */
    private String productAttr;
    /** 退货数量 */
    private Integer productCount;
    private BigDecimal productPrice;
    /** 商品实际支付单价 */
    private BigDecimal productRealPrice;
    /** 原因 */
    private String reason;
    /** 描述 */
    private String description;
    /** 凭证图片 */
    private String proofPics;
    /** 处理备注 */
    private String handleNote;
    /** 处理人 */
    private String handleMan;
}
