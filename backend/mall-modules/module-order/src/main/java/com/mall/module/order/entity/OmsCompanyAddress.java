package com.mall.module.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("oms_company_address")
public class OmsCompanyAddress {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 地址名称 */
    private String addressName;
    /** 默认发货地址：0->否；1->是 */
    private Integer sendStatus;
    /** 是否默认收货地址：0->否；1->是 */
    private Integer receiveStatus;
    /** 收发货人姓名 */
    private String name;
    private String phone;
    private String province;
    private String city;
    private String region;
    private String detailAddress;
}
