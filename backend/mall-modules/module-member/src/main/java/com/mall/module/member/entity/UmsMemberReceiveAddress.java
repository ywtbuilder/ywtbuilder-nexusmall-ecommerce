package com.mall.module.member.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 会员收货地址表 ums_member_receive_address
 */
@Data
@TableName("ums_member_receive_address")
public class UmsMemberReceiveAddress implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String name;
    private String phoneNumber;
    private Integer defaultStatus;
    private String postCode;
    private String province;
    private String city;
    private String region;
    private String detailAddress;
}
